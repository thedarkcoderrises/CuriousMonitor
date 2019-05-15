package com.tdcr.docker.backend.service.impl;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.backend.data.entity.AgentFeed;
import com.tdcr.docker.backend.data.entity.ContainerDetails;
import com.tdcr.docker.backend.data.entity.ImageDetails;
import com.tdcr.docker.backend.data.entity.IncidentDetails;
import com.tdcr.docker.backend.repositories.ContainerRepository;
import com.tdcr.docker.backend.repositories.ImageRepository;
import com.tdcr.docker.backend.service.AgentService;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

@Service
public class AgentServiceImpl implements AgentService, HasLogger {

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ContainerRepository containerRepository;

    @Autowired
    DockerService dockerService;

    @Autowired
    KafkaTemplate kafkaTemplate;

    private void raiseInc(IncidentDetails incidentDetails) {
        kafkaTemplate.send("RaiseIncTopic",""+System.currentTimeMillis(), incidentDetails);
        getLogger().info("RaiseIncident Event sent");
    }

    @Override
    public void saveImageFeed(AgentFeed feed) {
        dockerService.updateDockerClient(feed.getDockerDeamon());
        ImageDetails imgDtl = imageRepository.findById(
                feed.getImageId().replace(AppConst.SHA_256,AppConst.EMPTY_STR)).get();
        int totalErrorCnt =0;
        for (String errorType :
                feed.getErrorMap().keySet()) {
            int temp = null != imgDtl.getErrorMap().get(errorType)? imgDtl.getErrorMap().get(errorType):0;
            int feedCount = null!=feed.getErrorMap().get(errorType)?feed.getErrorMap().get(errorType):0;
            int sum = temp +feedCount;
            imgDtl.getErrorMap().put(errorType,feedCount);
             totalErrorCnt += sum;
        }

        if(totalErrorCnt >= imgDtl.getThresholdErrCnt()){
            raiseInc(new IncidentDetails(
                    imgDtl.getImageId(),
                    feed.getContainerName(),imgDtl.getErrorMap()));
        }
        imageRepository.save(imgDtl);
    }
}