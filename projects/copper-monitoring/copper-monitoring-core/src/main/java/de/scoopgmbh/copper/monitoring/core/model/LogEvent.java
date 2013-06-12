package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.Date;

public class LogEvent implements Serializable{
	private static final long serialVersionUID = -3392518179689121117L;
	
	private Date time;
	private String message;
	private String level;
	private String locationInformation;
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public LogEvent(Date time, String message, String locationInformation, String level) {
		super();
		this.time = time;
		this.message = message;
		this.level = level;
		this.locationInformation = locationInformation;
	}
	public LogEvent() {
		super();
	}
	public String getLocationInformation() {
		return locationInformation;
	}
	public void setLocationInformation(String locationInformation) {
		this.locationInformation = locationInformation;
	}
	
	
	
}
