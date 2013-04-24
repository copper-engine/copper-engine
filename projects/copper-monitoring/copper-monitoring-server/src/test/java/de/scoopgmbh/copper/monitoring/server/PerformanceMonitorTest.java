package de.scoopgmbh.copper.monitoring.server;

import org.junit.Test;


public class PerformanceMonitorTest {

	@Test
	public void test_noexception(){
		new PerformanceMonitor().getRessourcenInfo();
	}
}
