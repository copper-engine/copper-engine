package de.scoopgmbh.copper.monitoring;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceHistory;


public class MonitoringDataTest {
	
	@Test
	public void test_addWorkflowInstanceHistorywitdhLimit(){
		MonitoringData monitoringData = new MonitoringData(10);
		for (int i=0;i<20;i++){
			monitoringData.addWorkflowInstanceHistorywitdhLimit(new WorkflowInstanceHistory());
		}
		assertEquals(10, monitoringData.workflowInstanceHistorys.size());
	}

	
}
