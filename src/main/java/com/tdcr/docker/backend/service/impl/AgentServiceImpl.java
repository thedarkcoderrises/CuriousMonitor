package com.tdcr.docker.backend.service.impl;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Statistics;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.State;
import com.orbitz.consul.model.health.HealthCheck;
import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.backend.data.entity.AgentFeed;
import com.tdcr.docker.backend.data.entity.DockContainer;
import com.tdcr.docker.backend.data.entity.ErrorDetails;
import com.tdcr.docker.backend.data.entity.ImageDetails;
import com.tdcr.docker.backend.repositories.ImageRepository;
import com.tdcr.docker.backend.repositories.IncidentRepository;
import com.tdcr.docker.backend.service.AgentService;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AgentServiceImpl implements AgentService, HasLogger {

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    IncidentRepository incidentRepository;

    @Autowired
    DockerService dockerService;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Value("${thresholdErrCnt:4}")
    int thresholdErrCnt;

    @Value("${checkInterval:30000}")
    int checkInterval;

    @Autowired
    Map<String,Consul> consulClienttMap;

    @PostConstruct
    void initializeHealthCheck(){
        dockerService.init();
        for (String dockerDaemonName : consulClienttMap.keySet()){
            Consul consul = consulClienttMap.get(dockerDaemonName);
            if(consul == null) {
                getLogger().info("Consul not reachable for running docker daemon on host {}",dockerDaemonName);
                continue;
            }
            HealthClient healthClient = consul.healthClient();
            AgentClient agentClient = consul.agentClient();
            Thread checkContainerStatusThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try {
                            ConsulResponse<List<HealthCheck>> response =
                                    healthClient.getChecksByState(State.ANY);
                            for (HealthCheck hc:
                                    response.getResponse()) {
                                String svcName = hc.getServiceName().get();
                                if("serfHealth".equals(hc.getCheckId())) continue;
                                getLogger().info("{} with id {} is {}",hc.getServiceName().get(),
                                        hc.getCheckId(),hc.getStatus());

                                com.orbitz.consul.model.health.Service service =
                                        agentClient.getServices().get(
                                                hc.getCheckId().replace("service:",
                                                        AppConst.EMPTY_STR)); // get Service

                                String containerId = service.getAddress();
                                dockerService.updateDockerClient(dockerDaemonName);
                                InspectContainerResponse containerResponse =
                                        dockerService.inspectOnContainerId(containerId);
                               ImageDetails imageDetails = dockerService.getImageDetails(containerResponse.getImageId().
                                       replace(AppConst.SHA_256,AppConst.EMPTY_STR));
                               if(response.getResponse().size()-1 < imageDetails.getTotalContainersList().size()){
                                   for (String containerIdStr:
                                        imageDetails.getTotalContainersList()) {
                                       if(!containerIdStr.startsWith(containerId)){
                                           DockContainer dc = new DockContainer();
                                           dc.setImageId(imageDetails.getImageId());
                                           dc.setContainerId(containerIdStr);
                                           dc.setContainerName(containerIdStr);
                                           dc.setImageName(imageDetails.getImageId());
                                           dockerService.updateContainerStatus(dc,true);
                                           getLogger().info("Starting container {}",containerId);
                                       }else{
                                           getLogger().info("Passing containerID {}",containerId);
                                       }
                                   }
                               }
                            }
                        } catch (Exception e) {
                            getLogger().error(e.getMessage());
                        }finally {
                            try {
                                Thread.sleep(checkInterval);
                            } catch (InterruptedException e) {
                                getLogger().error(e.getMessage());
                            }
                        }
                    }
                }
            });
            checkContainerStatusThread.start();

           /* Thread checkLoadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean memUsageIncrease=false;//update flag after last 5 check;
                    Number previousMemUsage = null;
                    int iterationCheck = 0;
                    while(true){
                        try {
                            ConsulResponse<List<HealthCheck>> response =
                                    healthClient.getChecksByState(State.PASS);
                            for (HealthCheck hc:
                                    response.getResponse()) {
                                String svcName = hc.getServiceName().get();
                                if(!"myMicroSvc".equals(svcName)) continue;
                                getLogger().info("{} with id {} is {}",hc.getServiceName().get(),
                                        hc.getCheckId(),hc.getStatus());

                                com.orbitz.consul.model.health.Service service =
                                        agentClient.getServices().get(
                                                hc.getCheckId().replace("service:",
                                                        AppConst.EMPTY_STR)); // get Service
                                String containerId = service.getAddress();
                                dockerService.updateDockerClient(dockerDaemonName);
                                Statistics stats =dockerService.getContainerRawStats(containerId);
                                Number currentMemUsage = ((Number)stats.getMemoryStats().get("usage"));
                                if(previousMemUsage == null){
                                    previousMemUsage = currentMemUsage;
                                    continue;
                                }else if (currentMemUsage.longValue() > previousMemUsage.longValue()){
                                    memUsageIncrease = true;
                                    iterationCheck+=1;
                                }else if(currentMemUsage.longValue() < previousMemUsage.longValue()){
                                    memUsageIncrease = false;
                                    iterationCheck-=1;
                                }else if(currentMemUsage.longValue() == previousMemUsage.longValue()){
                                    getLogger().info("memUsageIncrease: {}, iterationCheck: {}",memUsageIncrease,iterationCheck);
                                    continue;
                                }
                                getLogger().info("memUsageIncrease: {}, iterationCheck: {}",memUsageIncrease,iterationCheck);

                                InspectContainerResponse containerResponse =
                                        dockerService.inspectOnContainerId(containerId);
                                ImageDetails imageDetails = dockerService.getImageDetails(containerResponse.getImageId().
                                        replace(AppConst.SHA_256,AppConst.EMPTY_STR));
                                if(memUsageIncrease && iterationCheck ==5){
                                    CreateContainerResponse createContainerResponse =
                                            dockerService.cloneContainer(containerResponse,svcName+"loadTest");
                                    imageDetails.addContainerToList(
                                            dockerService.createContainer(createContainerResponse,(svcName+"loadTest"),containerResponse.getImageId()));
                                    dockerService.saveImageDetails(imageDetails);
                                    previousMemUsage =  null;
                                    iterationCheck = -1;
                                }else if(iterationCheck ==0){
                                    if(imageDetails.getTotalContainersList().size() ==1) continue;
                                    previousMemUsage =  null;
                                    iterationCheck = -1;
                                }
                            }
                        } catch (Exception e) {
                            getLogger().error(e.getMessage());
                        }finally {
                            try {
                                Thread.sleep(checkInterval);
                            } catch (InterruptedException e) {
                                getLogger().error(e.getMessage());
                            }
                        }
                    }
                }
            });

            checkLoadThread.start();*/
        }

    }


    @Override
    public void saveImageFeed(AgentFeed feed) {
        dockerService.updateDockerClient(feed.getDockerDaemon());
        Optional<ImageDetails> opt = imageRepository.findById(
                feed.getImageId().replace(AppConst.SHA_256,AppConst.EMPTY_STR));
        ImageDetails imgDtl = null;
        if(opt.isPresent()){
            imgDtl = opt.get();
            int totalErrorCnt =0;
            for (String errorType :
                    feed.getErrorMap().keySet()) {
                int temp = (null != imgDtl.getErrorMap().get(errorType))? imgDtl.getErrorMap().get(errorType):0;// previous feed
                int feedCount = (null!=feed.getErrorMap().get(errorType))?feed.getErrorMap().get(errorType):0;// current feed
                int sum = temp +feedCount;// total
                imgDtl.getErrorMap().put(errorType,feedCount);// save current feed
                totalErrorCnt += sum;// accumulate
            }
            if(!imgDtl.getDockerDaemonList().contains(feed.getDockerDaemon())){
                imgDtl.getDockerDaemonList().add(feed.getDockerDaemon());
            }
            if(totalErrorCnt >= imgDtl.getThresholdErrCnt()){
                raiseInc(new ErrorDetails(
                        imgDtl.getImageId(),
                        imgDtl.getErrorMap()));
//                imgDtl.getErrorMap().clear();// clear error map as INC is raised
            }
        }
        imageRepository.save(imgDtl);
    }

    private void raiseInc(ErrorDetails errorDetails) {
        kafkaTemplate.send("RaiseIncTopic",AppConst.EMPTY_STR+System.currentTimeMillis(), errorDetails);
        getLogger().info("RaiseIncident Event sent");
    }


    @Override
    public void saveImageIncFeed(AgentFeed feed) {
        incidentRepository.save(feed.getIncident());
        getLogger().info("IncidentId {} for imageId {}",feed.getIncident().getIncNumber(),feed.getImageId());
    }



}