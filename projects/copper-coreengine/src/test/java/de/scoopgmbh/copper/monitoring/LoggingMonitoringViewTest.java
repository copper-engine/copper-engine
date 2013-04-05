package de.scoopgmbh.copper.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;
import de.scoopgmbh.copper.monitoring.integrationtest.LogFixture;
import de.scoopgmbh.copper.monitoring.integrationtest.LogFixture.LogContentAssertion;


public class LoggingMonitoringViewTest {

	@Test
	public final void testMesurePointLogging() {
		final LoggingMonitoringView loggingMonitoringOutput = new LoggingMonitoringView(new MonitoringEventQueue(),1);
		final MonitoringData monitoringOutput = new MonitoringData();
		{
			MeasurePointData stat = new MeasurePointData("insertIntoA");
			stat.setCount(101);
			stat.setElapsedTimeMicros(21);
			monitoringOutput.measurePoints.put("insertIntoA", stat);
		}
		{
			MeasurePointData stat = new MeasurePointData("insertIntoB45");
			stat.setCount(102);
			stat.setElapsedTimeMicros(22);
			monitoringOutput.measurePoints.put("insertIntoB45", stat);
		}
		
		new LogFixture().assertLogContent(new LogContentAssertion() {
			
			@Override
			public void executeLogCreatingAction() {
				loggingMonitoringOutput.log(monitoringOutput);
			}
			
			@Override
			public void assertLogContent(List<String> logContent) {
				assertEquals(1, logContent.size());
				assertTrue(logContent.get(0).contains("insertIntoA"));
				assertTrue(logContent.get(0).contains("insertIntoB45"));
			}
		});
			
	}

}
