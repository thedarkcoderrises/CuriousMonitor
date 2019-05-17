package com.tdcr.docker.backend.service.impl;

import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.backend.data.entity.AgentFeed;
import com.tdcr.docker.backend.data.entity.ImageDetails;
import com.tdcr.docker.backend.data.entity.ErrorDetails;
import com.tdcr.docker.backend.repositories.ContainerRepository;
import com.tdcr.docker.backend.repositories.ImageRepository;
import com.tdcr.docker.backend.repositories.IncidentRepository;
import com.tdcr.docker.backend.service.AgentService;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AgentServiceImpl implements AgentService, HasLogger {

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    IncidentRepository incidentRepository;

//    @Autowired
//    ContainerRepository containerRepository;

    @Autowired
    DockerService dockerService;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Value("${thresholdErrCnt:4}")
    int thresholdErrCnt;

    private void raiseInc(ErrorDetails errorDetails) {
        kafkaTemplate.send("RaiseIncTopic",""+System.currentTimeMillis(), errorDetails);
        getLogger().info("RaiseIncident Event sent");
    }

    @Override
    public void saveImageFeed(AgentFeed feed) {
        dockerService.updateDockerClient(feed.getDockerDaemon());
        Optional<ImageDetails> opt = imageRepository.findById(
                feed.getImageId().replace(AppConst.SHA_256,AppConst.EMPTY_STR));
        ImageDetails imgDtl;
        if(opt.isPresent()){
            imgDtl = opt.get();
            int totalErrorCnt =0;
            for (String errorType :
                    feed.getErrorMap().keySet()) {
                int temp = null != imgDtl.getErrorMap().get(errorType)? imgDtl.getErrorMap().get(errorType):0;
                int feedCount = null!=feed.getErrorMap().get(errorType)?feed.getErrorMap().get(errorType):0;
                int sum = temp +feedCount;
                imgDtl.getErrorMap().put(errorType,feedCount);
                totalErrorCnt += sum;
            }
            if(!imgDtl.getDockerDaemonList().contains(feed.getDockerDaemon())){
                imgDtl.getDockerDaemonList().add(feed.getDockerDaemon());
            }
            if(totalErrorCnt >= imgDtl.getThresholdErrCnt()){
                raiseInc(new ErrorDetails(
                        imgDtl.getImageId(),
                        feed.getContainerName(),imgDtl.getErrorMap()));
            }
        }else{
            imgDtl = new ImageDetails(feed.getImageId(),false,null,thresholdErrCnt,feed.getDockerDaemon());
        }
        imageRepository.save(imgDtl);
    }


    @Override
    public void saveImageIncFeed(AgentFeed feed) {
        incidentRepository.save(feed.getIncident());
        getLogger().info("IncidentId {} for imageId {}",feed.getIncident().getIncNumber(),feed.getImageId());
    }
}