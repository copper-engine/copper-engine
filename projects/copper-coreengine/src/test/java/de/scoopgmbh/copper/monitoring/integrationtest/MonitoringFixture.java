package de.scoopgmbh.copper.monitoring.integrationtest;

import de.scoopgmbh.copper.monitoring.MonitoringDataAwareCallable;
import de.scoopgmbh.copper.monitoring.MonitoringEventQueue;

public class MonitoringFixture {
	
	public MonitoringFixture waitUntilMonitoringDataProcessed(MonitoringEventQueue monitoringEventQueue){

		monitoringEventQueue.callAndWait(new MonitoringDataAwareCallable<Object>() {
			@Override
			public Object call() throws Exception {
				return new Object();
			}
		});
		
		return this;
	}

}
