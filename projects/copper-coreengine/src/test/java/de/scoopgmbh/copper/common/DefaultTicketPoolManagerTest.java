package de.scoopgmbh.copper.common;

import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.wfrepo.FileBasedWorkflowRepository;
import junit.framework.TestCase;

public class DefaultTicketPoolManagerTest extends TestCase {

	public void testObtain() throws Exception {
		
		final String T_POOL_ID = "testTicketPoolId";
		DefaultTicketPoolManager ticketPoolManager = new DefaultTicketPoolManager();
		ticketPoolManager.add(new TicketPool(DefaultTicketPoolManager.DEFAULT_POOL_ID, 50));
		ticketPoolManager.add(new TicketPool(T_POOL_ID, 50));
		ticketPoolManager.addMapping(de.scoopgmbh.copper.test.tranzient.simple.SimpleTransientWorkflow.class, T_POOL_ID);

		FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
		repo.setSourceDir("src/workflow/java");
		repo.setTargetDir("target/compiled_workflow");
		
		repo.start();
		ticketPoolManager.startup();
		try {
			Workflow<?> wf = repo.createWorkflowFactory("de.scoopgmbh.copper.test.tranzient.simple.SimpleTransientWorkflow").newInstance();
			String tpId = ticketPoolManager.obtainAndReturnTicketPoolId(wf);
			assertEquals(T_POOL_ID, tpId);
		}
		finally {
			repo.shutdown();
			ticketPoolManager.shutdown();
		}
	}

}
