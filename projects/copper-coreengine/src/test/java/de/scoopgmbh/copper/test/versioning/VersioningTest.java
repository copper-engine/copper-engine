/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.test.versioning;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.scoopgmbh.copper.EngineState;
import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.WorkflowInstanceDescr;
import de.scoopgmbh.copper.WorkflowVersion;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.tranzient.TransientScottyEngine;
import de.scoopgmbh.copper.util.BlockingResponseReceiver;

public class VersioningTest extends TestCase {

	public void testFindLatest() throws Exception {
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"transient-engine-application-context.xml", "SimpleTransientEngineTest-application-context.xml"});
		try {
			final WorkflowRepository repo = context.getBean(WorkflowRepository.class);
			WorkflowVersion v = repo.findLatestMajorVersion(VersionTestWorkflowDef.NAME, 9);
			Assert.assertEquals(new WorkflowVersion(9, 3, 1), v);
			
			v = repo.findLatestMinorVersion(VersionTestWorkflowDef.NAME, 9, 1);
			Assert.assertEquals(new WorkflowVersion(9, 1, 1), v);
		}
		finally {
			context.close();
		}
	}

	public void testLatest() throws Exception {
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"transient-engine-application-context.xml", "SimpleTransientEngineTest-application-context.xml"});
		TransientScottyEngine _engine = (TransientScottyEngine) context.getBean("transientEngine");
		assertEquals(EngineState.STARTED,_engine.getEngineState());
		ProcessingEngine engine = _engine;

		try {
			final BlockingResponseReceiver<String> brr = new BlockingResponseReceiver<String>();
			final WorkflowInstanceDescr<BlockingResponseReceiver<String>> descr = new WorkflowInstanceDescr<BlockingResponseReceiver<String>>(VersionTestWorkflowDef.NAME);
			descr.setData(brr);

			engine.run(descr);

			brr.wait4response(5000);
			final String workflowClassname = brr.getResponse();

			assertEquals("de.scoopgmbh.copper.test.versioning.VersionTestWorkflow_14_5_67", workflowClassname);

		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());

	}

	public void testVersion() throws Exception {
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"transient-engine-application-context.xml", "SimpleTransientEngineTest-application-context.xml"});
		TransientScottyEngine _engine = (TransientScottyEngine) context.getBean("transientEngine");
		assertEquals(EngineState.STARTED,_engine.getEngineState());
		ProcessingEngine engine = _engine;

		try {
			final BlockingResponseReceiver<String> brr = new BlockingResponseReceiver<String>();
			final WorkflowInstanceDescr<BlockingResponseReceiver<String>> descr = new WorkflowInstanceDescr<BlockingResponseReceiver<String>>(VersionTestWorkflowDef.NAME);
			descr.setVersion(new WorkflowVersion(1, 0, 1));
			descr.setData(brr);

			engine.run(descr);

			brr.wait4response(5000);
			final String workflowClassname = brr.getResponse();

			assertEquals("de.scoopgmbh.copper.test.versioning.VersionTestWorkflow_1_0_1", workflowClassname);

		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());

	}	

}
