package de.scoopgmbh.copper.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import de.scoopgmbh.copper.monitoring.integrationtest.LogFixture;
import de.scoopgmbh.copper.monitoring.integrationtest.LogFixture.LogContentAssertion;
import de.scoopgmbh.copper.monitoring.integrationtest.MonitoringFixture;


public class DefaultMonitoringDataCollectorTest {

	@Test
	public void testSubmitMeasurePoint(){
		MonitoringData monitoringData = new MonitoringData();
		MonitoringEventQueue monitoringQueue = new MonitoringEventQueue(1000,monitoringData);
		DefaultMonitoringDataCollector monitoringDataCollector = new DefaultMonitoringDataCollector(monitoringQueue);
		monitoringDataCollector.submitMeasurePoint("measurePointId42", 2, 100, TimeUnit.SECONDS);
		monitoringDataCollector.submitMeasurePoint("measurePointId42", 3, 200, TimeUnit.SECONDS);
		
		new MonitoringFixture().waitUntilMonitoringDataProcessed(monitoringQueue);
		
		assertEquals(1, monitoringData.measurePoints.size());
		assertEquals(2, monitoringData.measurePoints.get("measurePointId42").getCount());
		assertEquals(5, monitoringData.measurePoints.get("measurePointId42").getElementCount());
		assertEquals(TimeUnit.SECONDS.toMicros(300), monitoringData.measurePoints.get("measurePointId42").getElapsedTimeMicros());
	}
	
	
	@Test
	public void test_too_many_data_adds_schould_be_ignored_and_not_block_the_submitting_thread(){
		MonitoringEventQueue monitoringQueue = new MonitoringEventQueue(10,new MonitoringData());
		final DefaultMonitoringDataCollector monitoringDataCollector = new DefaultMonitoringDataCollector(monitoringQueue);
		
		new LogFixture().assertLogContent(new LogContentAssertion() {
			
			@Override
			public void executeLogCreatingAction() {
				for (int i=0;i<100000;i++){
					monitoringDataCollector.submitMeasurePoint("measurePointId42", 2, 100, TimeUnit.SECONDS);
				}
			}
			
			@Override
			public void assertLogContent(List<String> logContent) {
				assertTrue(logContent.size()>0);
				assertTrue(logContent.get(0).contains(MonitoringEventQueue.IGNORE_WARN_TEXT));
			}
		});

		
		new MonitoringFixture().waitUntilMonitoringDataProcessed(monitoringQueue);
		assertTrue(monitoringQueue.ignored.get()>0);
	}
	
	
}
