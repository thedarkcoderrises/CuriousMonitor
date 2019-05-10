package com.tdcr.docker.backend.data.entity;

import com.tdcr.docker.backend.utils.AppConst;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
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

    LocalDate nextReleaseDate ;

    public ImageDetails(){}

    public ImageDetails(String imageId, boolean subscription) {
        this.imageId = imageId;
        this.isSubscribed = subscription;
        Date today = new Date();
        this.nextReleaseDate = LocalDate.of(today.getYear(),today.getMonth(),today.getDay());
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

    public LocalDate getNextReleaseDate() {
        return nextReleaseDate;
    }
}
