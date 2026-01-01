/*
 * Copyright 2002-2019 SCOOP Software GmbH
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
package org.copperengine.regtest.test.analyse;

import org.copperengine.core.CopperException;
import org.copperengine.core.EngineState;
import org.copperengine.regtest.test.backchannel.WorkflowResult;
import org.copperengine.regtest.test.tranzient.TransientEngineTestContext;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * These tests should test, that no side effects are visible for internal core engine changed. The private workflow
 * member "__stack" might be used.
 *
 * A deep look into STACK_RESULT
 * * shows, the information that is given to the copper engine core
 * ** "stack" with relevant method call parameters incl. workflow instance
 * ** "locals" relevant local variables incl. workflow instance
 * ** "jumpNo", that point to the current "Interruptable" method call
 * ** "__stackPosition", that points to the current position in stack
 * * gives an idea of the copper engine concept, used inside the instrumentation (Hint: goto in bytecode is used ;-).
 * * shows, that the current position in stack is not visible, so it is added when throwing an "Interrupt" (a debugger breakpoint in the workflow "finally" block show it as a proof ;-).
 *
 * @author wsluyterman
 */
public class AnalyseTest {

    public static final String STACK_RESULT = "" +
            "Before resubmit (jumpNo = 0): __stackPosition=0\n" +
            "\t__stack=[]\n" +
            "Before resubmit (jumpNo=1): __stackPosition=0\n" +
            "\t__stack=[]\n" +
            "Before wait (jumpNo=2): __stackPosition=0\n" +
            "\t__stack=[]\n" +
            "Before localWait (jumpNo=3): __stackPosition=0\n" +
            "\t__stack=[]\n" +
            "Before wait in localWait (depth=2) :__stackPosition=1\n" +
            "\t__stack=[[\n" +
            "\tjumpNo=3\n" +
            "\tlocals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]\n" +
            "\tstack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]]]\n" +
            "After  wait in localWait (depth=2): __stackPosition=1\n" +
            "\t__stack=[[\n" +
            "\tjumpNo=3\n" +
            "\tlocals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]\n" +
            "\tstack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]]]\n" +
            "Before wait in localWait (depth=1) :__stackPosition=2\n" +
            "\t__stack=[[\n" +
            "\tjumpNo=3\n" +
            "\tlocals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]\n" +
            "\tstack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]][\n" +
            "\tjumpNo=1\n" +
            "\tlocals=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]\n" +
            "\tstack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]]]\n" +
            "After  wait in localWait (depth=1): __stackPosition=2\n" +
            "\t__stack=[[\n" +
            "\tjumpNo=3\n" +
            "\tlocals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]\n" +
            "\tstack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]][\n" +
            "\tjumpNo=1\n" +
            "\tlocals=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]\n" +
            "\tstack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]]]\n";


    @Test
    public void testWorkflow() throws Exception {
        doTest("org.copperengine.regtest.test.analyse.AnalyseWorkflow1");

    }

    private void doTest(String wfClassname) throws CopperException, InterruptedException {

        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            ctx.getEngine().run(wfClassname, null);

            WorkflowResult response = ctx.getBackChannelQueue().dequeue(3000, TimeUnit.MILLISECONDS);
            //
            assertEquals(
                    STACK_RESULT.replaceAll("Workflow \\[id=[^,]*, ", "Workflow ["),
                    ((String) response.getResult()).replaceAll("Workflow \\[id=[^,]*, ", "Workflow ["),
                    "Analyse string should only be changed by core impl or changed fomatting in workflow."
            );
        }
    }
}
