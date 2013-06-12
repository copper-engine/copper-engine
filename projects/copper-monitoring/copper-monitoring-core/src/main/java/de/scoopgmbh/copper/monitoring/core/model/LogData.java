package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LogData implements Serializable{
	private static final long serialVersionUID = -7676407159766000087L;

	List<LogEvent> logEvents = new ArrayList<LogEvent>();
	
	String logConfig;

	public List<LogEvent> getLogEvents() {
		return logEvents;
	}

	public void setLogEvents(List<LogEvent> logEvents) {
		this.logEvents = logEvents;
	}

	public String getLogConfig() {
		return logConfig;
	}

	public void setLogConfig(String logConfig) {
		this.logConfig = logConfig;
	}

	public LogData(List<LogEvent> logEvents, String logConfig) {
		super();
		this.logEvents = logEvents;
		this.logConfig = logConfig;
	}

	public LogData() {
		super();
	}
	
	
	

}
