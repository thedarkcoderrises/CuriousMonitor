package com.tdcr.docker.backend.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.backend.data.EventState;
import com.tdcr.docker.backend.data.entity.DockContainer;
import com.tdcr.docker.backend.data.entity.DockImage;
import com.tdcr.docker.backend.data.entity.Event;
import com.tdcr.docker.backend.data.entity.ImageDetails;
import com.tdcr.docker.backend.repositories.EventsRepository;
import com.tdcr.docker.backend.repositories.ImageRepository;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.backend.utils.ComputeStats;
import com.tdcr.docker.backend.utils.FirstObjectResultCallback;
import com.tdcr.docker.backend.utils.LogContainerCallback;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DockerServiceImpl implements DockerService, HasLogger {

    private static Logger LOG = LoggerFactory.getLogger(DockerServiceImpl.class);
    private static final DecimalFormat df2 = new DecimalFormat("#");

    @Autowired
    Map<String,DockerClient> dockerClientMap;
    DockerClient dockerClient;

    @Autowired
    ImageRepository imageRepository;

    @Value("${elk.url:\"\"}")
    String elkURL;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    EventsRepository eventsRepository;

    @Value("${thresholdErrCnt:4}")
    int thresholdErrCnt;

    @Override
    public void init(){
        for (String dd:
             dockerClientMap.keySet()) {
            updateDockerClient(dd);
            listAllImages(dd);
        }
    }

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
         lst= getContainer(status); //  docker ps -a -s -f status=${status}
        return covertContainerData(lst);
    }

    private List<Container> getContainer(String status) {
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
            Link[] links = inspectOnContainerId(container.getId()).getHostConfig().getLinks();
            list.add(new DockContainer(container, subscription.isPresent()?subscription.get().isSubscribed():false,elkURL,links));
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
        notifyContainerStatus(container,cmdStr);
        return container.getContainerId();
    }

    private void notifyContainerStatus(DockContainer container, String cmdStr) {

        ImageDetails imageDetails = getImageDetails(container.getImageId());
        if(imageDetails == null) return;
        if(imageDetails.isSubscribed()){
            String shortDesc = String.format("Container %s status now: %s",container.getContainerName(),cmdStr);
            kafkaTemplate.send("CuriousNotifyTopic",""+System.currentTimeMillis(),shortDesc);
            EventState state;
            if("Running".equals(cmdStr)){
                state = EventState.STARTED;
            }else{
                state = EventState.STOPPED;
            }
            eventsRepository.save(new Event(LocalDate.now(), LocalTime.now(), state,shortDesc,container.getImageName(),container.getContainerName()));
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
    public Set<String> getDockerDaemons() {
        return dockerClientMap.keySet();
    }

    @Override
    public void setSubscriptionToContainer(String imageId, boolean subscription, String dockerDaemon) {
        Optional<ImageDetails> opt = imageRepository.findById(imageId);
        ImageDetails imageDetails;
      /*  if (!opt.isPresent()){
            imageDetails = new ImageDetails(imageId,subscription,thresholdErrCnt,dockerDaemon,0);
        }else{*/
            imageDetails = opt.get();
//        }
        imageDetails.setSubscribed(subscription);
        imageRepository.save(imageDetails);
    }

    @Override
    public Statistics getContainerRawStats(String containerId) {
        return getContainerStatistics(containerId);
    }

    @Override
    public List<DockImage> listAllImages(String dockerDaemon) {
        ListImagesCmd cmd = dockerClient.listImagesCmd();
        return covertImageData(cmd.exec(),dockerDaemon);
    }

    private List<DockImage> covertImageData(List<Image> images, String dockerDaemon) {
        List<DockImage> list = new ArrayList<>();
        List<Container> containerList = getContainer(null);
        for (Image image :
                images) {
            Optional<ImageDetails> imageDetailsOptional = imageRepository.findById(
                    image.getId().replace(AppConst.SHA_256,AppConst.EMPTY_STR));
            ImageDetails imageDetails = imageDetailsOptional.isPresent()?imageDetailsOptional.get():null;
            DockImage dockImage =new DockImage(image, imageDetails);
            list.add(dockImage);
            int stopContainerCnt =0;
            int runningContainerCnt = stopContainerCnt;
            Collections.sort(containerList, new Comparator<Container>() {
                @Override
                public int compare(Container o1, Container o2) {
                    return o1.getCreated().compareTo(o2.getCreated());
                }
            });
            for (Container ctnr :containerList) {
                if(ctnr.getImageId().equalsIgnoreCase(AppConst.SHA_256+dockImage.getImageId())){
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
                    dockImage.getContainerList().add(ctnr.getId());
                    if(imageDetails == null){
                        imageDetails   = new ImageDetails(image.getId().replace(AppConst.SHA_256,AppConst.EMPTY_STR),true,4,dockerDaemon,ctnr.getId());
                    }else{
                        imageDetails.getTotalContainersList().add(ctnr.getId());
                    }
                }
            }

            dockImage.setRunningContainerCount(runningContainerCnt);
            dockImage.setTotalContainerCount((runningContainerCnt+stopContainerCnt));
            if(imageDetails == null){
                imageDetails   = new ImageDetails(image.getId().replace(AppConst.SHA_256,AppConst.EMPTY_STR),true,4,dockerDaemon,null);
            }
            imageRepository.save(imageDetails);
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
    public ImageDetails getImageDetails(String imageId) {
        Optional<ImageDetails> opt = imageRepository.findById(imageId);
        return opt.isPresent()? opt.get():null;
    }

    @Override
    public Info getDockerInfo() {
        InfoCmd cmd= dockerClient.infoCmd();
       return cmd.exec();
    }

    @Override
    public void cloneContainerOnImage(DockImage image, boolean status) {
        /*CreateContainerResponse container =dockerClient.createContainerCmd("my-sd-svc:1").
                withName("mysd3").withEnv("CONSUL=consul").withBinds(Bind.parse("/var/run/docker.sock:/var/run/docker.sock:ro")).
                withLinks(Link.parse("consul:consul")).withNetworkMode("bunit").exec();*/
        InspectContainerResponse inspectResponse =inspectOnContainerId(image.getContainerList().get(0));
        DockContainer dc = new DockContainer();
        if(status){
            CreateContainerResponse container = cloneContainer(inspectResponse);;
            dc.setContainerId(container.getId());
        }else{
            dc.setContainerId(image.getContainerList().get(image.getContainerList().size()-1));
        }
        dc.setImageId(image.getImageId());
        updateContainerStatus(dc,status );

    }

    @Override
    public CreateContainerResponse cloneContainer(InspectContainerResponse response) {
        CreateContainerCmd cmd =dockerClient.createContainerCmd(response.getConfig().getImage()).
                withName(response.getName().replace(AppConst.FWD_SLASH,AppConst.EMPTY_STR)+df2.format(Math.random()*9+1));
        for (Bind bind:
                response.getHostConfig().getBinds()) {
            cmd.withBinds(bind);
        }
        if(!StringUtils.isEmpty(response.getHostConfig().getNetworkMode())){
//            cmd.withNetworkMode(response.getHostConfig().getNetworkMode());
            cmd.withNetworkMode("bunit");
        }
        if(response.getConfig().getEnv().length >0)
            cmd.withEnv(response.getConfig().getEnv());
        return cmd.exec();
    }

}
