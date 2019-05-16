package com.tdcr.docker.backend.service;

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

    Set<String> getDockerDeamons();

    void setSubscriptionToContainer(String imageId ,boolean subscription);

    List<DockImage> listAllImages();

    String pullImageUsingCmd(String cmd);

    String removeImage(String imageId);

    ImageDetails getImageDetailsStats(String imageId);

    Info getDockerInfo();

}
