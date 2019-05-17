package com.tdcr.docker.backend.data.entity;

import com.tdcr.docker.backend.utils.AppConst;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;

@Entity(name="ImageDetails")
public class ImageDetails implements Serializable {

    @Id
    @NotEmpty
    @Size(max = 255)
    @Column(unique = true)
    private String imageId;

    @NotNull
    private boolean isSubscribed;

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<String> dockerDaemonList;

    private boolean locked = false;

    private int totalOpenIncidents;

    private int totalCloseIncidents;

    private int thresholdErrCnt;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String,Integer> errorMap;



    /*@OneToMany(fetch = FetchType.EAGER,mappedBy = "imageDetails", cascade = CascadeType.ALL)
    private List<ContainerDetails> containerDetails;*/

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "imageDetails", cascade = CascadeType.ALL)
    private List<Incident> incidents;

    public ImageDetails(){}

    public ImageDetails(String imageId, boolean subscription,ContainerDetails cd,int thresholdErrCnt,String dockerDaemon) {
        this.imageId = imageId;
        this.isSubscribed = subscription;
        this.thresholdErrCnt = thresholdErrCnt;
        this.getDockerDaemonList().add(dockerDaemon);
        //if(cd != null)this.containerDetails = Arrays.asList(cd);
    }



    public String getImageId() {
        return imageId;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getTotalIncidents() {
        return  (this.totalOpenIncidents+ this.totalCloseIncidents);
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

    /*public List<ContainerDetails> getContainerDetails() {
        return containerDetails;
    }

    public void setContainerDetails(List<ContainerDetails> containerDetails) {
        this.containerDetails = containerDetails;
    }*/

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

    public List<String> getDockerDaemonList() {
        if(dockerDaemonList == null){
            dockerDaemonList = new ArrayList<>();
        }
        return dockerDaemonList;
    }

    public List<Incident> getIncidents() {
        return incidents;
    }

    public void setIncidents(List<Incident> incidents) {
        this.incidents = incidents;
    }

    /*@Override
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
    }*/
}
