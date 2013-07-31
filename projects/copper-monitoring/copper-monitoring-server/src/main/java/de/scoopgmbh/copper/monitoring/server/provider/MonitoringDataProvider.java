package de.scoopgmbh.copper.monitoring.server.provider;

import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataProviderInfo;

public interface MonitoringDataProvider {
	
	public static enum Status{
		CREATED,STARTED,STOPPED
	}
	
	public void startProvider();
	public void stopProvider();
	public Status getProviderStatus();
	public String getProviderName();
	public MonitoringDataProviderInfo createInfo();
}
