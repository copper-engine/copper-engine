/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.copperengine.core.EngineState;
import org.copperengine.core.test.backchannel.BackChannelQueue;
import org.copperengine.core.test.backchannel.WorkflowResult;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SubWorkflowTransientEngineTest {

    @Test
    public void testWorkflow() throws Exception {
        final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "transient-engine-application-context.xml", "SimpleTransientEngineTest-application-context.xml" });
        final TransientScottyEngine engine = (TransientScottyEngine) context.getBean("transientEngine");
        final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);

        assertEquals(EngineState.STARTED, engine.getEngineState());

        try {
            engine.run("org.copperengine.core.test.tranzient.simple.SimpleTestParentWorkflow", "testData");
            WorkflowResult r = backChannelQueue.dequeue(2000, TimeUnit.MILLISECONDS);
            assertNotNull(r);
        } finally {
            context.close();
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());

    }

}
