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
    private String imageName;

    @NotNull
    private boolean isSubscribed;

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<String> dockerDaemonList;

    private boolean locked = false;

    private int totalOpenIncidents;

    private int totalCloseIncidents;

    private int thresholdErrCnt;

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<String> totalContainersList = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String,Integer> errorMap;

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "imageDetails", cascade = CascadeType.ALL)
    private List<Incident> incidents;

    public ImageDetails(){}

    public ImageDetails(String imageId, boolean subscription,int thresholdErrCnt,String dockerDaemon,String containerId,String imageName) {
        this.imageId = imageId;
        this.isSubscribed = subscription;
        this.thresholdErrCnt = thresholdErrCnt;
        addContainerToList(containerId);
        this.imageName = imageName;
        this.getDockerDaemonList().add(dockerDaemon);
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
        int open = 0;
        int close = 0;
        if(getIncidents() != null && !getIncidents().isEmpty()){
            for (Incident inc: getIncidents()) {
                if(Incident.OPEN.equalsIgnoreCase(inc.getIncState())){
                    open++;
                }else{
                    close++;
                }
            }
        }
        setTotalOpenIncidents(open);
        setTotalCloseIncidents(close);
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

    public List<String> getTotalContainersList() {
        return totalContainersList;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void addContainerToList(String containersId) {
        if(containersId != null && !this.totalContainersList.contains(containersId)){
            this.totalContainersList.add(containersId);
        }
    }
}
