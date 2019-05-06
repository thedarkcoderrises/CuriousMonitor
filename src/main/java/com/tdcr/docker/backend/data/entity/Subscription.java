package com.tdcr.docker.backend.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity(name="Subscription")
public class Subscription implements Serializable {

    @Id
    @NotEmpty
    @Size(max = 255)
    @Column(unique = true)
    private String containerId;

    @NotNull
    private boolean isSubscribed;

    private boolean locked = false;

    public Subscription(){}

    public Subscription(String containerId, boolean subscription) {
        this.containerId = containerId;
        this.isSubscribed = subscription;
    }

    public String getContainerId() {
        return containerId;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public boolean isLocked() {
        return locked;
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
        Subscription that = (Subscription) o;
        return locked == that.locked &&
                Objects.equals(containerId, that.containerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), containerId,isSubscribed, locked);
    }
}
