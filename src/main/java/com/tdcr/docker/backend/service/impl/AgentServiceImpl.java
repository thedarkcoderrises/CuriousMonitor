package com.tdcr.docker.backend.service.impl;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.tdcr.docker.backend.data.entity.ContainerDetails;
import com.tdcr.docker.backend.data.entity.ImageDetails;
import com.tdcr.docker.backend.repositories.ContainerRepository;
import com.tdcr.docker.backend.repositories.ImageRepository;
import com.tdcr.docker.backend.service.AgentService;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentServiceImpl implements AgentService {

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ContainerRepository containerRepository;

    @Autowired
    DockerService dockerService;


    @Override
    public void saveImageFeed(String dockerDeamon, String containerId, int errorCount, String errorType) {
        dockerService.updateDockerClient(dockerDeamon);
        InspectContainerResponse response =dockerService.inspectOnContainerId(containerId);
        ImageDetails imgDtl = imageRepository.findById(
                response.getImageId().replace(AppConst.SHA_256,AppConst.EMPTY_STR)).get();
        List<ContainerDetails> containerDtlList =imgDtl.getContainerDetails();
        if(containerDtlList == null || containerDtlList.isEmpty()){
            containerRepository.save(new ContainerDetails(containerId,response.getImageId().replace(AppConst.SHA_256,AppConst.EMPTY_STR),response.getName(),errorCount,errorType));
        }else {
            boolean updateFlag = false;
            for (ContainerDetails cd :
                    containerDtlList) {
                if(cd.getContainerId().equals(containerId)){
                    cd.setErrorCount(errorCount);
                    cd.setErrorType(errorType);
                    updateFlag = true;
                }
            }
            if(!updateFlag){
                containerRepository.save(new ContainerDetails(containerId,response.getImageId().replace(AppConst.SHA_256,AppConst.EMPTY_STR),response.getName(),errorCount,errorType));
            }else{
                imageRepository.save(imgDtl);
            }
        }
    }
}