package com.tdcr.docker.backend.data.entity;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.api.model.Link;
import com.tdcr.docker.backend.utils.AppConst;
import com.vaadin.server.ExternalResource;
import org.springframework.util.StringUtils;

import javax.persistence.Id;
import java.util.List;

public class DockContainer{

    @Id
    String containerId;
    String containerName;
    String status;
    long memorySizeInMB;
    String port;
    String runningSince;
    String imageId;
    String imageName;
    boolean subscription;
    long created;
    String elkURL;
    String links;

    public DockContainer(){}

    public DockContainer(Container container, boolean subscribed,String elkURL, Link[] links) {
        super();
        this.setContainerId(container.getId());
        this.setContainerName(container.getNames()[0]);
        this.setMemorySizeInMB((container.getSizeRootFs()/1024)/1024);
        this.setStatus(container.getStatus().startsWith("Up")? "Running":"Stopped");
        this.setPort(container.getPorts().length==0?"":getPublicPort(container.getPorts()[0]));
        this.setRunningSince(container.getStatus());
        this.setImageId(container.getImageId());
        this.setImageName(container.getImage());
        this.setSubscription(subscribed);
        this.setCreated(container.getCreated());
        this.setElkURL(elkURL.replace("%s",getContainerName()));
        this.setLinks(links);
    }

    private String getPublicPort(ContainerPort port) {
        return (StringUtils.isEmpty(port.getIp())?"":port.getIp()) +
                (StringUtils.isEmpty(port.getIp())?"":":")+
                (StringUtils.isEmpty(port.getPublicPort())?"":port.getPublicPort());
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageId() {
        if(!StringUtils.isEmpty(imageId)){
            return imageId.replace(AppConst.SHA_256,AppConst.EMPTY_STR);
        }else{
            return imageId;
        }
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRunningSince() {
        return runningSince;
    }

    public void setRunningSince(String runningSince) {
        this.runningSince = runningSince;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerName() {
        containerName = containerName.replace("/","");
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public long getMemorySizeInMB() {
        return memorySizeInMB;
    }

    public void setMemorySizeInMB(long memorySize) {
        this.memorySizeInMB = memorySize;
    }

    public boolean isSubscription() {
        return subscription;
    }

    public void setSubscription(boolean subscription) {
        this.subscription = subscription;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getElkURL() {
        return elkURL;
    }

    public void setElkURL(String elkURL) {
        this.elkURL = elkURL;
    }

    public static String getURL(DockContainer container) {
      return container.getElkURL();
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(Link[] links) {
        this.links =AppConst.EMPTY_STR;
        for (Link link:links
             ) {
            if(StringUtils.isEmpty(this.links)){
                this.links = link.getName();
            }else{
                this.links +=","+link.getName();
            }
        }
    }
}
