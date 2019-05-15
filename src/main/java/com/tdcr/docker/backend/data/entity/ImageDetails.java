package com.tdcr.docker.backend.data.entity;

import com.tdcr.docker.backend.utils.AppConst;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity(name="ImageDetails")
public class ImageDetails implements Serializable {

    @Id
    @NotEmpty
    @Size(max = 255)
    @Column(unique = true)
    private String imageId;

    @NotNull
    private boolean isSubscribed;

    private boolean locked = false;

    private int totalIncidents;

    private int totalOpenIncidents;

    private int totalCloseIncidents;

    private int thresholdErrCnt;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String,Integer> errorMap;



    @OneToMany(fetch = FetchType.EAGER,mappedBy = "imageDetails", cascade = CascadeType.ALL)
    private List<ContainerDetails> containerDetails;

    public ImageDetails(){}

    public ImageDetails(String imageId, boolean subscription,ContainerDetails cd,int thresholdErrCnt) {
        this.imageId = imageId;
        this.isSubscribed = subscription;
        this.thresholdErrCnt = thresholdErrCnt;
        if(cd != null)this.containerDetails = Arrays.asList(cd);
    }



    public String getImageId() {
        return imageId;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getTotalIncidents() {
        return  (totalIncidents = this.totalOpenIncidents+ this.totalCloseIncidents)+ AppConst.EMPTY_STR;
    }

    public void setTotalIncidents(int totalIncidents) {
        this.totalIncidents = totalIncidents;
    }

    public String getTotalOpenIncidents() {
        return totalOpenIncidents+ AppConst.EMPTY_STR;
    }

    public void setTotalOpenIncidents(int totalOpenIncidents) {
        this.totalOpenIncidents = totalOpenIncidents;
    }

    public String getTotalCloseIncidents() {
        return totalCloseIncidents+ AppConst.EMPTY_STR;
    }

    public void setTotalCloseIncidents(int totalCloseIncidents) {
        this.totalCloseIncidents = totalCloseIncidents;
    }

    public List<ContainerDetails> getContainerDetails() {
        return containerDetails;
    }

    public void setContainerDetails(List<ContainerDetails> containerDetails) {
        this.containerDetails = containerDetails;
    }

    public int getThresholdErrCnt() {
        return thresholdErrCnt;
    }

    public void setThresholdErrCnt(int thresholdErrCnt) {
        this.thresholdErrCnt = thresholdErrCnt;
    }

    public Map<String, Integer> getErrorMap() {
        return errorMap;
    }

    public void setErrorMap(Map<String, Integer> errorMap) {
        this.errorMap = errorMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ImageDetails that = (ImageDetails) o;
        return locked == that.locked &&
                Objects.equals(imageId, that.imageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), imageId,isSubscribed, locked);
    }
}
