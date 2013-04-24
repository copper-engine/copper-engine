package de.scoopgmbh.copper.monitoring.server.workaround;

import org.junit.Test;

import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitoring.server.workaround.DBProcessingStateWorkaround;
import de.scoopgmbh.copper.persistent.DBProcessingState;


public class DBProcessingStateWorkaroundTest {
	
	final DBProcessingStateWorkaround[] states = new DBProcessingStateWorkaround[DBProcessingStateWorkaround.values().length];

	@Test
	public void test_1_to_1_mapping_between_DBProcessingStateWorkaround_and_WorkflowInstanceState_(){
		for (DBProcessingStateWorkaround s : DBProcessingStateWorkaround.values()) {
			if (states[s.key()] != null)
				throw new RuntimeException("Inconsistent key mapping found for "+s);
			states[s.key()] = s;
			WorkflowInstanceState.valueOf(s.name());
		}
	}
	
	@Test
	public void test_1_to_1_mapping_between_WorkflowInstanceState_and_DBProcessingStateWorkaround(){
		for (WorkflowInstanceState s : WorkflowInstanceState.values()) {
			DBProcessingStateWorkaround.valueOf(s.name());
		}
	}
	
	@Test
	public void test_1_to_1_mapping_between_DBProcessingStateWorkaround_and_DBProcessingState(){
		for (DBProcessingStateWorkaround s : DBProcessingStateWorkaround.values()) {
			if (states[s.key()] != null)
				throw new RuntimeException("Inconsistent key mapping found for "+s);
			states[s.key()] = s;
			DBProcessingState.valueOf(s.name());
		}
	}
	
	@Test
	public void test_1_to_1_mapping_between_DBProcessingState_DBProcessingStateWorkaround(){
		for (DBProcessingState s : DBProcessingState.values()) {
			DBProcessingStateWorkaround.valueOf(s.name());
		}
	}
}
