package com.tdcr.docker.backend.data.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity(name="Incident")
public class Incident {

    @NotEmpty
    @Size(max = 255)
    @Column(unique = true)
    private String imageId;

    @ManyToOne
    @JoinColumn(name = "imageId",updatable = false, insertable = false)
    ImageDetails imageDetails;

    @Id
    @NotEmpty
    @Size(max = 255)
    @Column(unique = true)
    private String incNumber;
    private String assignedTo;
    private String incState;
    private String caller;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd-MM-yyyy")
    private Date openedOn;
    private String shortDescription;
    private String category;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getIncNumber() {
        return incNumber;
    }

    public void setIncNumber(String incNumber) {
        this.incNumber = incNumber;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getIncState() {
        return incState;
    }

    public void setIncState(String incState) {
        this.incState = incState;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public Date getOpenedOn() {
        return openedOn;
    }

    public void setOpenedOn(Date openedOn) {
        this.openedOn = openedOn;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
