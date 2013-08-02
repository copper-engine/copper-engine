package de.scoopgmbh.copper.monitoring.server.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Optional;

import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataProviderInfo;

public class MonitoringDataProviderManager {
	
	List<MonitoringDataProvider> provider = new ArrayList<MonitoringDataProvider>();

	public MonitoringDataProviderManager(MonitoringDataProvider... provider) {
		this(Arrays.asList(provider));
	}
	
	
	public MonitoringDataProviderManager(List<MonitoringDataProvider> provider) {
		super();
		this.provider = provider;
	}
	
	public void addAll(){
		for (MonitoringDataProvider monitoringDataProvider: provider){
			monitoringDataProvider.startProvider();
		}
	}
	
	public void startAll(){
		for (MonitoringDataProvider monitoringDataProvider: provider){
			monitoringDataProvider.startProvider();
		}
	}
	
	public List<MonitoringDataProviderInfo> getInfos(){
		ArrayList<MonitoringDataProviderInfo> result = new ArrayList<MonitoringDataProviderInfo>();
		for (MonitoringDataProvider monitoringDataProvider: provider){
			result.add(monitoringDataProvider.createInfo());
		}
		return result;
	}


	public Optional<MonitoringDataProvider> getProvider(String name) {
		for (MonitoringDataProvider monitoringDataProvider: provider){
			if (monitoringDataProvider.getProviderName().equals(name)){
				return Optional.of(monitoringDataProvider);
			}
		}
		return Optional.absent();
	}
	

}
