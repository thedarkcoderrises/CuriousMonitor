package com.tdcr.docker.backend.service;

import com.tdcr.docker.backend.data.entity.DocContainer;

import java.util.List;
import java.util.Map;

public interface DockerService {

    String listRunningContainers();

    String listStoppedContainers();

    List<DocContainer> listAllContainers(String status);

    String inspectOnContainerId(String containerId);

    Map<String,String> getContainerStats(String containerId) throws Exception;

    String updateContainerStatus(String containerId, boolean status);

    void updateDockerClient(String dockerDaemonName);

    String getLogs(String containerId) throws InterruptedException;

    String removeContainer(String containerId);
}
