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
package de.scoopgmbh.copper.test;

import junit.framework.TestCase;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.scoopgmbh.copper.EngineState;
import de.scoopgmbh.copper.WorkflowInstanceDescr;
import de.scoopgmbh.copper.management.WorkflowInfo;
import de.scoopgmbh.copper.tranzient.TransientScottyEngine;

public class ExceptionHandlingTest extends TestCase {
	
	public void testExceptionHandlingTestWF() throws Exception {
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"transient-engine-application-context.xml", "SimpleTransientEngineTest-application-context.xml"});
		TransientScottyEngine engine = (TransientScottyEngine) context.getBean("transientEngine");
		assertEquals(EngineState.STARTED,engine.getEngineState());
		
		try {
			String data = "data";

			
			final WorkflowInstanceDescr<String> descr = new WorkflowInstanceDescr<String>("de.scoopgmbh.copper.test.ExceptionHandlingTestWF");
			descr.setId("1234456");
			descr.setData(data);
			
			engine.run(descr);
			
			Thread.sleep(1000L);
			
			WorkflowInfo info = engine.queryWorkflowInstance(descr.getId());
			
			assertNull(info);
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		
	}	
	public void testIssueClassCastExceptionWorkflow3() throws Exception {
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"transient-engine-application-context.xml", "SimpleTransientEngineTest-application-context.xml"});
		TransientScottyEngine engine = (TransientScottyEngine) context.getBean("transientEngine");
		assertEquals(EngineState.STARTED,engine.getEngineState());
		
		try {
			String data = "data";

			
			final WorkflowInstanceDescr<String> descr = new WorkflowInstanceDescr<String>("de.scoopgmbh.copper.test.IssueClassCastExceptionWorkflow3");
			descr.setId("1234456");
			descr.setData(data);
			
			engine.run(descr);
			
			Thread.sleep(1000L);
			
			WorkflowInfo info = engine.queryWorkflowInstance(descr.getId());
			
			assertNull(info);
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		
	}
	
}
