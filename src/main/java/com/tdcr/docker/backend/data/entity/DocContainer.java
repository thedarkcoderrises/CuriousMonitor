package com.tdcr.docker.backend.data.entity;

import com.github.dockerjava.api.model.ContainerPort;
import org.springframework.util.StringUtils;

public class DocContainer extends AbstractEntity{

    String containerId;
    String containerName;
    String status;
    long memorySizeInMB;
    String port;
    String runningSince;
    String imageId;
    String imageName;

    public DocContainer(com.github.dockerjava.api.model.Container container) {
        super();
        this.setContainerId(container.getId());
        this.setContainerName(container.getNames()[0]);
        this.setMemorySizeInMB((container.getSizeRootFs()/1024)/1024);
        this.setStatus(container.getStatus().startsWith("Up")? "Running":"Stopped");
        this.setPort(container.getPorts().length==0?"":getPublicPort(container.getPorts()[0]));
        this.setRunningSince(container.getStatus());
        this.setImageId(container.getImageId());
        this.setImageName(container.getImage());
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
        return imageId;
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
}
