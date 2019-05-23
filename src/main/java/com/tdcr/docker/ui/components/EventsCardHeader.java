package com.tdcr.docker.ui.components;

public class EventsCardHeader {

	private String main;
	private String secondary;

	public EventsCardHeader(String main, String secondary) {
		this.main = main;
		this.secondary = secondary;
	}

	public String getMain() {
		return main;
	}

	public void setMain(String main) {
		this.main = main;
	}

	public String getSecondary() {
		return secondary;
	}

	public void setSecondary(String secondary) {
		this.secondary = secondary;
	}
}
