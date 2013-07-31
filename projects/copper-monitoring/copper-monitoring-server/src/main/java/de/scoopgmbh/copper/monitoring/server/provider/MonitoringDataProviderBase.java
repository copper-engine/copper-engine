package de.scoopgmbh.copper.monitoring.server.provider;

import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataProviderInfo;

public class MonitoringDataProviderBase implements MonitoringDataProvider{

	protected Status status=Status.CREATED;
	
	@Override
	public void startProvider() {
		status=Status.STARTED;
	}

	@Override
	public void stopProvider() {
		status=Status.STOPPED;
	}

	@Override
	public Status getProviderStatus() {
		return status;
	}

	@Override
	public String getProviderName() {
		return getClass().getSimpleName();
	}

	@Override
	public MonitoringDataProviderInfo createInfo() {
		return new MonitoringDataProviderInfo(getProviderName(),getProviderStatus().toString());
	}

}
