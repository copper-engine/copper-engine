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
package org.copperengine.core.test.tranzient.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.copperengine.core.EngineState;
import org.copperengine.core.tranzient.TransientProcessorPool;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.copperengine.core.util.BlockingResponseReceiver;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SupendPoolTransientEngineTest {

	@Test
	public void testWorkflow() throws Exception {
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"transient-engine-application-context.xml", "SimpleTransientEngineTest-application-context.xml"});
		TransientScottyEngine engine = (TransientScottyEngine) context.getBean("transientEngine");
		TransientProcessorPool processorPool = (TransientProcessorPool) context.getBean("T_ProcessorPool_DEFAULT");
		assertEquals(EngineState.STARTED,engine.getEngineState());
		
		try {
			final BlockingResponseReceiver<Integer> brr = new BlockingResponseReceiver<Integer>();
			Thread.sleep(10);
			assertFalse(brr.isResponseReceived());
			
			processorPool.suspend();
			
			engine.run("org.copperengine.core.test.tranzient.simple.NopTransientWorkflow", brr);

			brr.wait4response(100L);
			
			assertFalse(brr.isResponseReceived());

			processorPool.resume();
			
			brr.wait4response(100L);
			
			assertEquals(1,brr.getResponse().intValue());
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		
	}
	
}
