package com.tdcr.docker.backend.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Statistics;
import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.backend.data.entity.DockContainer;
import com.tdcr.docker.backend.data.entity.DockImage;
import com.tdcr.docker.backend.data.entity.ImageDetails;
import com.tdcr.docker.backend.repositories.ImageRepository;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.backend.utils.ComputeStats;
import com.tdcr.docker.backend.utils.FirstObjectResultCallback;
import com.tdcr.docker.backend.utils.LogContainerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DockerServiceImpl implements DockerService, HasLogger {

    private static Logger LOG = LoggerFactory.getLogger(DockerServiceImpl.class);

    @Autowired
    Map<String,DockerClient> dockerClientMap;
    DockerClient dockerClient;

    @Autowired
    ImageRepository imageRepository;

    @Value("${elk.url:\"\"}")
    String elkURL;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Override
    public String listRunningContainers() {
        return listAllContainers("running").toString();
    }

    @Override
    public String listStoppedContainers() {
        return listAllContainers("exited").toString();
    }

    @Override
    public List<DockContainer> listAllContainers(String status) {
        List<Container> lst = new ArrayList<>();
         lst= getConatainer(status); //  docker ps -a -s -f status=${status}
        return covertContainerData(lst);
    }

    private List<Container> getConatainer(String status) {
        ListContainersCmd cmd = null;
        if(StringUtils.isEmpty(status)){
            cmd = dockerClient.listContainersCmd().withShowSize(true)
                    .withShowAll(true);
        }else{
            cmd = dockerClient.listContainersCmd().withShowSize(true)
                    .withShowAll(true).withStatusFilter(status);
        }
        return cmd.exec();
    }

    private List<DockContainer> covertContainerData(List<Container> lst) {
        List<DockContainer> list = new ArrayList<>();
        for (Container container :
                lst) {
           Optional<ImageDetails> subscription = imageRepository.findById(container.getImageId());
            list.add(new DockContainer(container, subscription.isPresent()?subscription.get().isSubscribed():false,elkURL));
        }
        return list;
    }


    @Override
    public InspectContainerResponse inspectOnContainerId(String containerId) {
         InspectContainerResponse container = dockerClient.inspectContainerCmd(containerId).exec();
         return container;
    }

    @Override
    public Map<String,String> getContainerStats(String containerId){
        return ComputeStats.computeStats(getContainerStatistics(containerId));
    }

    private Statistics getContainerStatistics(String containerId){
         Statistics stats = null;
        try {
            StatsCmd statsCmd =dockerClient.statsCmd(containerId);
            FirstObjectResultCallback<Statistics> resultCallback = new FirstObjectResultCallback<>();
            stats = statsCmd.exec(resultCallback).waitForObject();
        } catch(InterruptedException e){
           e.printStackTrace();
        }
        return stats;
    }

    @Override
    public String updateContainerStatus(DockContainer container , boolean status) {

        String cmdStr = "Stopped";
        try{
            if(status){
                StartContainerCmd cmd = dockerClient.startContainerCmd(container.getContainerId());
                cmd.exec();
                cmdStr = "Running";
            }else{
                StopContainerCmd cmd = dockerClient.stopContainerCmd(container.getContainerId());
                cmd.exec();
            }
        }catch (Exception e){
            //TODO
            return null;
        }
        notifyConttainerStatus(container,cmdStr);
        return container.getContainerId();
    }

    private void notifyConttainerStatus(DockContainer container, String cmdStr) {

        ImageDetails imageDetails = getImageDetailsStats(container.getImageId());
        if(imageDetails == null) return;
        if(imageDetails.isSubscribed()){
            kafkaTemplate.send("CuriousNotifyTopic",""+System.currentTimeMillis(),
                    String.format("Container %s status now: %s",container.getContainerName(),cmdStr));
            getLogger().info("RaiseIncident Event sent");
        }
    }

    @Override
    public void updateDockerClient(String dockerDaemonName) {
        this.dockerClient = dockerClientMap.get(dockerDaemonName);
    }

    @Override
    public String getLogs(String containerId) throws InterruptedException {
        LogContainerCallback loggingCallback = new
                LogContainerCallback();

        LogContainerCmd cmd = dockerClient.logContainerCmd(containerId);
        cmd.withStdOut(true).withTimestamps(true).withTailAll().exec(loggingCallback);
        loggingCallback.awaitCompletion(3, TimeUnit.SECONDS);
        return loggingCallback.getEntries().toString();
    }

    @Override
    public String removeContainer(String containerId) {
        RemoveContainerCmd cmd = dockerClient.removeContainerCmd(containerId);
        try{
            cmd.exec();
        }catch(Exception e){
            return null;
        }
        return containerId;
    }

    @Override
    public Set<String> getDockerDeamons() {
        return dockerClientMap.keySet();
    }

    @Override
    public void setSubscriptionToContainer(String imageId , boolean subscription) {
        imageRepository.save(new ImageDetails(imageId,subscription,null,4));
    }

    @Override
    public Statistics getContainerRawStats(String containerId) {
        return getContainerStatistics(containerId);
    }

    @Override
    public List<DockImage> listAllImages() {
        ListImagesCmd cmd = dockerClient.listImagesCmd();
        return covertImageData(cmd.exec());
    }

    private List<DockImage> covertImageData(List<Image> images) {
        List<DockImage> list = new ArrayList<>();
        List<Container> containerList = getConatainer(null);
        for (Image image :
                images) {
            Optional<ImageDetails> subscription = imageRepository.findById(
                    image.getId().replace(AppConst.SHA_256,AppConst.EMPTY_STR));
            DockImage dockImage =new DockImage(image, subscription.isPresent()?subscription.get():null);
            list.add(dockImage);
            int stopContainerCnt =0;
            int runningContainerCnt = stopContainerCnt;
            for (Container ctnr :containerList) {
                if(ctnr.getImageId().equalsIgnoreCase(AppConst.SHA_256+dockImage.getImageId())){
                    dockImage.setImageName(ctnr.getImage());
                    if(ctnr.getStatus().startsWith("Up")){
                        runningContainerCnt+=1;
                    }else{
                        stopContainerCnt+=1;
                    }
                    Date date = new Date(ctnr.getCreated()*1000);
                    if(dockImage.getContainerEntry().containsKey(date.getMonth())){
                        int count = dockImage.getContainerEntry().get(date.getMonth());
                        dockImage.getContainerEntry().put(date.getMonth(),count+1);
                    }else{
                        dockImage.getContainerEntry().put(date.getMonth(),1);
                    }
                }else if(StringUtils.isEmpty(dockImage.getImageName())){
                    dockImage.setImageName(AppConst.EMPTY_STR);
                }
            }

            dockImage.setRunningContainerCount(runningContainerCnt);
            dockImage.setTotalContainerCount((runningContainerCnt+stopContainerCnt));
        }
        list.sort(new Comparator<DockImage>() {
            @Override
            public int compare(DockImage o1, DockImage o2) {
                return
                        o2.getImageName().compareTo(o1.getImageName());
            }
        });
        return list;
    }

    @Override
    public String pullImageUsingCmd(String cmd) {
        return null;
    }

    @Override
    public String removeImage(String imageId) {
        return null;
    }

    @Override
    public InspectImageResponse inspectOnImageId(String imageId) {
        InspectImageResponse image = dockerClient.inspectImageCmd(imageId).exec();
        return image;
    }

    @Override
    public ImageDetails getImageDetailsStats(String imageId) {
        Optional<ImageDetails> opt = imageRepository.findById(imageId);
        return opt.isPresent()? opt.get():null;
    }

    @Override
    public Info getDockerInfo() {
        InfoCmd cmd= dockerClient.infoCmd();
       return cmd.exec();
    }
}
