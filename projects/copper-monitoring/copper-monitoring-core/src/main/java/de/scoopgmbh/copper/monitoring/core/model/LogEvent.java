package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.Date;

public class LogEvent implements Serializable{
	private static final long serialVersionUID = -3392518179689121117L;
	
	private Date time;
	private String message;
	private String level;
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
	public LogEvent(Date time, String message, String level) {
		super();
		this.time = time;
		this.message = message;
		this.level = level;
	}
	public LogEvent() {
		super();
	}
	
	
}
