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
package org.copperengine.core.test.tranzient.lang;

import static org.junit.Assert.assertEquals;

import org.copperengine.core.CopperException;
import org.copperengine.core.EngineState;
import org.copperengine.core.Workflow;
import org.copperengine.core.test.TestResponseReceiver;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.copperengine.core.util.BlockingResponseReceiver;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LocalVarTest {

    private final int[] response = { -1 };

    @Test
    public void testWorkflow() throws Exception {
        doTest("org.copperengine.core.test.tranzient.lang.LocalVarTransientWorkflow1", 15);
    }

    @Test
    public void testWorkflow2() throws Exception {
        doTest("org.copperengine.core.test.tranzient.lang.LocalVarTransientWorkflow2", 5);

    }

    private void doTest(String wfClassname, int expectedResult) throws CopperException, InterruptedException {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "transient-engine-application-context.xml", "SimpleTransientEngineTest-application-context.xml" });
        TransientScottyEngine engine = context.getBean("transientEngine", TransientScottyEngine.class);
        context.getBeanFactory().registerSingleton("OutputChannel4711", new TestResponseReceiver<String, Integer>() {
            @Override
            public void setResponse(Workflow<String> wf, Integer r) {
                synchronized (response) {
                    response[0] = r.intValue();
                    response.notifyAll();
                }
            }
        });

        assertEquals(EngineState.STARTED, engine.getEngineState());

        try {
            BlockingResponseReceiver<Integer> brr = new BlockingResponseReceiver<Integer>();
            engine.run(wfClassname, brr);
            synchronized (response) {
                if (response[0] == -1) {
                    response.wait(30000);
                }
            }
            assertEquals(expectedResult, response[0]);
        } finally {
            context.close();
        }
        assertEquals(EngineState.STOPPED, engine.getEngineState());
    }

}
