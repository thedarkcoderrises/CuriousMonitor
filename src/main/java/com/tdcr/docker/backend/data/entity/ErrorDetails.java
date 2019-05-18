package com.tdcr.docker.backend.data.entity;

import java.util.Map;

public class ErrorDetails {

    String imageId;
    Map<String, Integer> incDescription;


    public ErrorDetails(String imageId, Map<String, Integer> incDescription) {
        this.imageId = imageId;
        this.incDescription = incDescription;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public Map<String, Integer> getIncDescription() {
        return incDescription;
    }

    public void setIncDescription(Map<String, Integer> incDescription) {
        this.incDescription = incDescription;
    }

}
