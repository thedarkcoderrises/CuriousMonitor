package com.tdcr.docker.backend.service;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Statistics;
import com.tdcr.docker.backend.data.entity.DockContainer;
import com.tdcr.docker.backend.data.entity.DockImage;
import com.tdcr.docker.backend.data.entity.ImageDetails;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DockerService {

    void init();

    String listRunningContainers();

    String listStoppedContainers();

    List<DockContainer> listAllContainers(String status);

    InspectContainerResponse inspectOnContainerId(String containerId);

    InspectImageResponse inspectOnImageId(String imageId);

    Statistics getContainerRawStats(String containerId);

    Map<String,String> getContainerStats(String containerId);

    String updateContainerStatus(DockContainer container, boolean status);

    void updateDockerClient(String dockerDaemonName);

    String getLogs(String containerId) throws InterruptedException;

    String removeContainer(String containerId);

    Set<String> getDockerDaemons();

    void setSubscriptionToContainer(String imageId, boolean subscription, String dockerDaemon);

    List<DockImage> listAllImages(String dockerDaemon);

    String pullImageUsingCmd(String cmd);

    String removeImage(String imageId);

    ImageDetails getImageDetails(String imageId);

    Info getDockerInfo();

    void cloneContainerOnImage(DockImage image, boolean added);

    public CreateContainerResponse cloneContainer(InspectContainerResponse response, String containerName);

    String createContainer(CreateContainerResponse container,String containerName,String imageId);

    void saveImageDetails(ImageDetails imageDetails);

}
