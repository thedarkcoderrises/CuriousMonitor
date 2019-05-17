package com.tdcr.docker.backend.data.entity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

//@Entity(name="ContainerDetails")
public class ContainerDetails implements Serializable {/*

    @Id
    @NotEmpty
    @Size(max = 255)
    @Column(unique = true)
    private String containerId;

    @NotEmpty
    @Size(max = 255)
    @Column(unique = true)
    private String imageId;

    @NotEmpty
    @Size(max = 255)
    @Column(unique = true)
    private String name;

    @NotEmpty
    @Size(max = 255)
    @Column(unique = true)
    private String errorType;

    @ManyToOne
    @JoinColumn(name = "imageId",updatable = false, insertable = false)
    ImageDetails imageDetails;


    private int errorCount;

    public ContainerDetails(){}

    public ContainerDetails(String contnrId,String imgeId,String cntnrName, int errorCnt,String errorType) {
        this.setContainerId(contnrId);
        this.setImageId(imgeId);
        this.setName(cntnrName);
        this.setErrorType(errorType);
        this.setErrorCount(errorCnt);
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }*/
}

