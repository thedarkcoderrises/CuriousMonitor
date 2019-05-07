package com.tdcr.docker.backend.service;

import com.github.dockerjava.api.model.Statistics;
import com.tdcr.docker.backend.data.entity.DockContainer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DockerService {

    String listRunningContainers();

    String listStoppedContainers();

    List<DockContainer> listAllContainers(String status);

    String inspectOnContainerId(String containerId);

    Statistics getContainerRawStats(String containerId);

    Map<String,String> getContainerStats(String containerId);

    String updateContainerStatus(String containerId, boolean status);

    void updateDockerClient(String dockerDaemonName);

    String getLogs(String containerId) throws InterruptedException;

    String removeContainer(String containerId);

    Set<String> getDockerDeamons();

    void setSubscriptionToContainer(String containerId,boolean subscription);
}
