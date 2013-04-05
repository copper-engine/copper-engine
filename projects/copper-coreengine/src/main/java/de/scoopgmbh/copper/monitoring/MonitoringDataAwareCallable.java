package de.scoopgmbh.copper.monitoring;

import java.util.concurrent.Callable;


public abstract class MonitoringDataAwareCallable<T> implements Callable<T>{
	protected MonitoringData monitoringData;
	
	public MonitoringData getMonitoringData() {
		return monitoringData;
	}

	public void setMonitoringData(MonitoringData monitoringData){
		this.monitoringData = monitoringData;
	}
}
