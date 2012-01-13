/*
 * Copyright 2002-2011 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.common;

import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.wfrepo.FileBasedWorkflowRepository;
import junit.framework.TestCase;

public class DefaultTicketPoolManagerTest extends TestCase {

	private static final String WF_CLASSNAME = "de.scoopgmbh.copper.test.tranzient.simple.SimpleTransientWorkflow";

	public void testObtain() throws Exception {
		
		final String T_POOL_ID = "testTicketPoolId";
		DefaultTicketPoolManager ticketPoolManager = new DefaultTicketPoolManager();
		ticketPoolManager.add(new TicketPool(DefaultTicketPoolManager.DEFAULT_POOL_ID, 50));
		ticketPoolManager.add(new TicketPool(T_POOL_ID, 50));
		ticketPoolManager.addMapping(WF_CLASSNAME, T_POOL_ID);

		FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
		repo.setSourceDir("src/workflow/java");
		repo.setTargetDir("target/compiled_workflow");
		
		repo.start();
		ticketPoolManager.startup();
		try {
			Workflow<?> wf = repo.createWorkflowFactory(WF_CLASSNAME).newInstance();
			String tpId = ticketPoolManager.obtainAndReturnTicketPoolId(wf);
			assertEquals(T_POOL_ID, tpId);
		}
		finally {
			repo.shutdown();
			ticketPoolManager.shutdown();
		}
	}

}
