package com.tdcr.docker.backend.data.entity;

import com.github.dockerjava.api.model.Image;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.backend.utils.ComputeStats;

import javax.persistence.Id;

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

    public DockImage(){}

    public DockImage(Image image, boolean subscribed) {
        this.imageId =image.getId().replace(AppConst.SHA_256,AppConst.EMPTY_STR);
        this.size = ComputeStats.calculateSize(image.getSize());
        this.virtualSize = ComputeStats.calculateSize(image.getVirtualSize());
        this.versions =image.getRepoTags();
        this.setSubscription(subscribed);
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

    public void setSubscription(boolean subscription) {
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
}
