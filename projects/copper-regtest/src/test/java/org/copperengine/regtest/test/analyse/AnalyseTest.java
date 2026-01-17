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
 * * shows the information given to the copper engine core
 * ** "stack" with relevant method call parameters incl. workflow instance
 * ** "locals" relevant local variables incl. workflow instance
 * ** "jumpNo", that points to the current "Interruptable" method call
 * ** "__stackPosition", that points to the current position in stack
 * * gives an idea of the copper engine concept, used inside the instrumentation (Hint: goto in bytecode is used ;-).
 * * shows that the current position in stack is not visible, so it is added when throwing an "Interrupt" (a debugger breakpoint in the workflow "finally" block show it as a proof ;-).
 *
 * @author wsluyterman
 */
public class AnalyseTest {

    private static final String STACK_RESULT1 = """
            Before resubmit (jumpNo = 0): __stackPosition=0
            	__stack=[]
            Before resubmit (jumpNo=1): __stackPosition=0
            	__stack=[]
            Before wait (jumpNo=2): __stackPosition=0
            	__stack=[]
            Before localWait (jumpNo=3): __stackPosition=0
            	__stack=[]
            Before wait in localWait (depth=2) :__stackPosition=1
            	__stack=[[
            	jumpNo=3
            	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]
            	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]]]
            After  wait in localWait (depth=2): __stackPosition=1
            	__stack=[[
            	jumpNo=3
            	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]
            	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]]]
            Before wait in localWait (depth=1) :__stackPosition=2
            	__stack=[[
            	jumpNo=3
            	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]
            	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]][
            	jumpNo=1
            	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]
            	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]]]
            After  wait in localWait (depth=1): __stackPosition=2
            	__stack=[[
            	jumpNo=3
            	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]
            	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]][
            	jumpNo=1
            	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]
            	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]]]
            """;

    private static  String STACK_RESULT2 = """
start: __stackPosition=0
	__stack=[]
interrupt [0]: __stackPosition=1
	__stack=[[
	jumpNo=0
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],0,Hello in main()!]
	stack=[]]]
resume [0]: __stackPosition=0
	__stack=[[
	jumpNo=0
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],0,Hello in main()!]
	stack=[]]]
interrupt [1]: __stackPosition=1
	__stack=[[
	jumpNo=1
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],10,Hello in main()!]
	stack=[]]]
resume [1]: __stackPosition=0
	__stack=[[
	jumpNo=1
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],10,Hello in main()!]
	stack=[]]]
interrupt [2]: __stackPosition=1
	__stack=[[
	jumpNo=2
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],20,Hello in main()!]
	stack=[]]]
resume [2]: __stackPosition=0
	__stack=[[
	jumpNo=2
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],20,Hello in main()!]
	stack=[]]]
interrupt [3, 0]: __stackPosition=2
	__stack=[[
	jumpNo=3
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]
	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]][
	jumpNo=0
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]
	stack=[]]]
resume [3, 0]: __stackPosition=0
	__stack=[[
	jumpNo=3
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]
	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]][
	jumpNo=0
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]
	stack=[]]]
interrupt [3, 1, 0]: __stackPosition=3
	__stack=[[
	jumpNo=3
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]
	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]][
	jumpNo=1
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]
	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]][
	jumpNo=0
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]
	stack=[]]]
resume [3, 1, 0]: __stackPosition=0
	__stack=[[
	jumpNo=3
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],30,Hello in main()!]
	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,2]][
	jumpNo=1
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]
	stack=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]][
	jumpNo=0
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],100,1]
	stack=[]]]
interrupt [5]: __stackPosition=1
	__stack=[[
	jumpNo=5
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],java.lang.RuntimeException: i > 0]
	stack=[]]]
resume [5]: __stackPosition=0
	__stack=[[
	jumpNo=5
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],java.lang.RuntimeException: i > 0]
	stack=[]]]
interrupt [6]: __stackPosition=1
	__stack=[[
	jumpNo=6
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],java.lang.RuntimeException: i > 0]
	stack=[]]]
resume [6]: __stackPosition=0
	__stack=[[
	jumpNo=6
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],java.lang.RuntimeException: i > 0]
	stack=[]]]
interrupt [8]: __stackPosition=1
	__stack=[[
	jumpNo=8
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],666,Hello 2in main()!]
	stack=[]]]
resume [8]: __stackPosition=0
	__stack=[[
	jumpNo=8
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],666,Hello 2in main()!]
	stack=[]]]
interrupt [9]: __stackPosition=1
	__stack=[[
	jumpNo=9
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],666,Hello 2in main()!]
	stack=[]]]
resume [9]: __stackPosition=0
	__stack=[[
	jumpNo=9
	locals=[Workflow [priority=5, processorPoolId=T#DEFAULT],666,Hello 2in main()!]
	stack=[]]]
            """;

    @Test
    public void testWorkflow() throws Exception {
        doTest("org.copperengine.regtest.test.analyse.AnalyseWorkflow1", STACK_RESULT1);
    }

    @Test
    public void testWorkflow2() throws Exception {
        doTest("org.copperengine.regtest.test.analyse.AnalyseWorkflow2", STACK_RESULT2);
    }

    private void doTest(String wfClassname, final String expectedResult) throws CopperException, InterruptedException {

        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            ctx.getEngine().run(wfClassname, null);

            WorkflowResult response = ctx.getBackChannelQueue().dequeue(3000, TimeUnit.MILLISECONDS);
            //
            assertEquals(
                    expectedResult.replaceAll("Workflow \\[id=[^,]*, ", "Workflow ["),
                    ((String) response.getResult()).replaceAll("Workflow \\[id=[^,]*, ", "Workflow ["),
                    "Analyse string should only be changed by core impl or changed formatting in workflow."
            );
        }
    }
}
