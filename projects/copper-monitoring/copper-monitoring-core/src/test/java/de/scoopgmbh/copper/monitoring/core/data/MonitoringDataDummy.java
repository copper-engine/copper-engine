package de.scoopgmbh.copper.monitoring.core.data;

import java.util.Date;

import de.scoopgmbh.copper.monitoring.core.model.MonitoringData;

class MonitoringDataDummy implements MonitoringData{
	public Date date;
	public String value;
	
	public MonitoringDataDummy(Date date, String value) {
		super();
		this.date = date;
		this.value = value;
	}
	
	public MonitoringDataDummy() {
		super();
	}
	
	public MonitoringDataDummy(String value) {
		this(new Date(),value);
	}

	@Override
	public Date getTimeStamp() {
		return date;
	}
	
	
}