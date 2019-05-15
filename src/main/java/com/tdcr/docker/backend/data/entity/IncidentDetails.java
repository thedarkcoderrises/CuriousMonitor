package com.tdcr.docker.backend.data.entity;

import java.util.Map;

public class IncidentDetails {

    String imageId;

    String containerName;

    Map<String, Integer> incDescription;

    public IncidentDetails(String imageId, String containerName, Map<String, Integer> incDescription) {
        this.imageId = imageId;
        this.containerName = containerName;
        this.incDescription = incDescription;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Map<String, Integer> getIncDescription() {
        return incDescription;
    }

    public void setIncDescription(Map<String, Integer> incDescription) {
        this.incDescription = incDescription;
    }
}
