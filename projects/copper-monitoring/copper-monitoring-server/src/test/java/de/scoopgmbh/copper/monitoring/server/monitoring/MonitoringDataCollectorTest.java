package de.scoopgmbh.copper.monitoring.server.monitoring;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import de.scoopgmbh.copper.monitoring.server.testfixture.LogFixture;
import de.scoopgmbh.copper.monitoring.server.testfixture.LogFixture.LogContentAssertion;
import de.scoopgmbh.copper.monitoring.server.testfixture.LogFixture.MessageAndLogLevel;
import de.scoopgmbh.copper.monitoring.server.testfixture.MonitoringFixture;


public class MonitoringDataCollectorTest {
	@Test
	public void test_too_many_data_adds_schould_be_ignored_and_not_block_the_submitting_thread(){
		MonitoringDataAccessQueue monitoringQueue = new MonitoringDataAccessQueue(10,new MonitoringData());
		final MonitoringDataCollector monitoringDataCollector = new MonitoringDataCollector(monitoringQueue);
		
		new LogFixture().assertLogContent(new LogContentAssertion() {
			
			@Override
			public void executeLogCreatingAction() {
				for (int i=0;i<100000;i++){
					monitoringDataCollector.submitAdapterWfLaunch("", new Object());
				}
			}

			@Override
			public void assertLogContent(List<MessageAndLogLevel> logContent) {
				assertTrue(logContent.size()>0);
				assertTrue(logContent.get(0).message.contains(MonitoringDataAccessQueue.IGNORE_WARN_TEXT));
			}
		});

		
		new MonitoringFixture().waitUntilMonitoringDataProcessed(monitoringQueue);
		assertTrue(monitoringQueue.ignored.get()>0);
	}
}
