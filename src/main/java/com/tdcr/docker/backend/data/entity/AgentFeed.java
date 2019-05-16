package com.tdcr.docker.backend.data.entity;

import java.util.Map;

public class AgentFeed {

    private String imageId;
    private String dockerDaemon;
    private Map<String,Integer> errorMap;
    private String containerName;
    private ErrorDetails errorDetails;
    private Incident incident;


    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getDockerDaemon() {
        return dockerDaemon;
    }

    public void setDockerDaemon(String dockerDaemon) {
        this.dockerDaemon = dockerDaemon;
    }

    public Map<String, Integer> getErrorMap() {
        return errorMap;
    }

    public void setErrorMap(Map<String, Integer> errorMap) {
        this.errorMap = errorMap;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public ErrorDetails getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(ErrorDetails errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }
}
