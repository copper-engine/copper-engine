package de.scoopgmbh.copper.monitoring;


public abstract class MonitoringDataAwareRunnable implements Runnable{
	protected MonitoringData monitoringData;
	
	public MonitoringData getMonitoringData() {
		return monitoringData;
	}

	public void setMonitoringData(MonitoringData monitoringData){
		this.monitoringData = monitoringData;
	}
}
