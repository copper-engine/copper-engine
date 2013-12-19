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
package org.copperengine.core.test.versioning;

import static org.junit.Assert.assertEquals;

import org.copperengine.core.EngineState;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.WorkflowVersion;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.copperengine.core.util.BlockingResponseReceiver;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class VersioningTest {

	@Test
	public void testFindLatest() throws Exception {
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"transient-engine-application-context.xml", "SimpleTransientEngineTest-application-context.xml"});
		try {
			final WorkflowRepository repo = context.getBean(WorkflowRepository.class);
			WorkflowVersion v = repo.findLatestMajorVersion(VersionTestWorkflowDef.NAME, 9);
			assertEquals(new WorkflowVersion(9, 3, 1), v);
			
			v = repo.findLatestMinorVersion(VersionTestWorkflowDef.NAME, 9, 1);
			assertEquals(new WorkflowVersion(9, 1, 1), v);
		}
		finally {
			context.close();
		}
	}

	@Test
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

			assertEquals("org.copperengine.core.test.versioning.VersionTestWorkflow_14_5_67", workflowClassname);

		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());

	}

	@Test
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

			assertEquals("org.copperengine.core.test.versioning.VersionTestWorkflow_1_0_1", workflowClassname);

		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());

	}	

}
