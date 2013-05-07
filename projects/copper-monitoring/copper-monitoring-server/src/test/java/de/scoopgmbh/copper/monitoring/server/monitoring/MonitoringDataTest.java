package de.scoopgmbh.copper.monitoring.server.monitoring;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;


public class MonitoringDataTest {
	
	@Test
	public void test_addAdapterWflaunchWitdhLimit(){
		MonitoringData monitoringData = new MonitoringData(10,10,10);
		for (int i=0;i<20;i++){
			monitoringData.addAdapterWflaunchWitdhLimit(new AdapterWfLaunchInfo());
		}
		assertEquals(10, monitoringData.getAdapterWfLaunches().size());
	}

}
