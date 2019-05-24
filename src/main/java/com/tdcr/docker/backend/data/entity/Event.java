package com.tdcr.docker.backend.data.entity;

import com.tdcr.docker.backend.data.EventState;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity(name = "Events")
public class Event extends AbstractEntity{

	public Event(){}

	public Event(LocalDate dueDate, LocalTime dueTime, EventState state, String shortDesc,String imageName, String containerName){
		this.dueDate = dueDate;
		this.dueTime = dueTime;
		this.state = state;
		this.shortDesc = shortDesc;
		this.imageName = imageName;
		this.containerName = containerName;
	}

	@NotNull
	private LocalDate dueDate;

	@NotNull
	private LocalTime dueTime;

	private EventState state;

	private String shortDesc;

	private String imageName;

	private String containerName;


	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public LocalTime getDueTime() {
		return dueTime;
	}

	public void setDueTime(LocalTime dueTime) {
		this.dueTime = dueTime;
	}

	public EventState getState() {
		return state;
	}

	public void setState(EventState state) {
		this.state = state;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}
}
