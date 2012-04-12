/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
import de.scoopgmbh.copper.tranzient.TransientScottyEngine;
import de.scoopgmbh.copper.util.BlockingResponseReceiver;

public class SwitchCaseTest extends TestCase {
	
	public void testWorkflow() throws Exception {
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"transient-engine-application-context.xml", "SimpleTransientEngineTest-application-context.xml"});
		TransientScottyEngine engine = (TransientScottyEngine) context.getBean("transientEngine");
		assertEquals(EngineState.STARTED,engine.getEngineState());
		
		try {
			SwitchCaseTestData data = new SwitchCaseTestData();
			data.testEnumValue = TestEnum.C;
			data.asyncResponseReceiver = new BlockingResponseReceiver<Integer>();
			engine.run("de.scoopgmbh.copper.test.SwitchCaseTestWF", data);
			data.asyncResponseReceiver.wait4response(5000L);
			assertEquals(0,data.asyncResponseReceiver.getResponse().intValue());
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		
	}
	
}
