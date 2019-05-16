package com.tdcr.docker.backend.data.entity;

import com.github.dockerjava.api.model.Image;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.backend.utils.ComputeStats;

import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;

public class DockImage{

    @Id
    String imageId;
    String imageName;
    String size;
    String virtualSize;
    String[] versions;
    boolean subscription;
    int runningContainerCount;
    int totalContainerCount;
    int errorIndex;
    ImageDetails imageDetails;
    Map<Integer,Integer> containerEntry = new HashMap<>();

    public DockImage(){}

    public DockImage(Image image, ImageDetails imageDetails) {
        this.imageId =image.getId().replace(AppConst.SHA_256,AppConst.EMPTY_STR);
        this.size = ComputeStats.calculateSize(image.getSize(),true);
        this.virtualSize = ComputeStats.calculateSize(image.getVirtualSize(),true);
        this.versions =image.getRepoTags();
        this.imageDetails = imageDetails;
        if(imageDetails != null) setSubscription(imageDetails.isSubscribed());

    }

    public String getImageId() {
        return imageId;
    }

    public String getSize() {
        return size;
    }

    public String getVirtualSize() {
        return virtualSize;
    }

    public String[] getVersions() {
        return versions;
    }

    public String getImageName() {
        return imageName;
    }

    public boolean isSubscription() {
        return subscription;
    }

    private void setSubscription(boolean subscription) {
        this.subscription = subscription;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getRunningContainerCount() {
        return runningContainerCount;
    }

    public void setRunningContainerCount(int runningContainerCount) {
        this.runningContainerCount = runningContainerCount;
    }

    public int getTotalContainerCount() {
        return totalContainerCount;
    }

    public void setTotalContainerCount(int totalContainerCount) {
        this.totalContainerCount = totalContainerCount;
    }

    public ImageDetails getImageDetails() {
        return imageDetails;
    }

    public Map<Integer, Integer> getContainerEntry() {
        return containerEntry;
    }

    public void setContainerEntry(Map<Integer, Integer> containerEntry) {
        this.containerEntry = containerEntry;
    }

    public int getErrorIndex() {
        return errorIndex;
    }

    public void setErrorIndex(int errorIndex) {
        this.errorIndex = errorIndex;
    }
}
