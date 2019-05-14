package com.tdcr.docker.backend.data.entity;

public class IncidentDetails {

    String imageId;

    String containerId;

    String containerName;

    String incDescription;

    public IncidentDetails(String imageId, String containerId, String containerName, String incDescription) {
        this.imageId = imageId;
        this.containerId = containerId;
        this.containerName = containerName;
        this.incDescription = incDescription;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getIncDescription() {
        return incDescription;
    }

    public void setIncDescription(String incDescription) {
        this.incDescription = incDescription;
    }
}
