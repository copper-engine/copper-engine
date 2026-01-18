/*
 * Copyright 2002-2017 SCOOP Software GmbH
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

import static org.copperengine.regtest.test.analyse.Info.appendInfo;

import org.copperengine.core.*;
import org.copperengine.regtest.test.backchannel.WorkflowResult;

import org.copperengine.regtest.test.backchannel.BackChannelQueue;

import org.copperengine.core.AutoWire;

public class AnalyseWorkflow1 extends Workflow<Integer> {

    @AutoWire
    public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
        this.backChannelQueue = backChannelQueue;
    }

    private BackChannelQueue backChannelQueue;

    // not used as a local, so that the content is not coupled on itself
    private StringBuffer analyseString = new StringBuffer();


    private void localWait(int delay, int depth) throws Interrupt {
        appendInfo("Before wait in localWait (depth=" + depth + ") :", __stack, __stackPosition, analyseString);
        wait(WaitMode.FIRST, delay, "corrlationId");
        appendInfo("After  wait in localWait (depth=" + depth + "): ", __stack, __stackPosition, analyseString);
        if (depth > 1) {
            localWait(100, --depth);
        }
    }

    public void main() throws Interrupt {
        try {
            int i = 0;
            String s = "Hello in main()!";
            appendInfo("Before resubmit (jumpNo = 0): ", __stack, __stackPosition, analyseString);
            resubmit();
            i = i + 10;
            appendInfo("Before resubmit (jumpNo=1): ", __stack, __stackPosition, analyseString);
            resubmit();
            i = i + 10;
            appendInfo("Before wait (jumpNo=2): ", __stack, __stackPosition, analyseString);
            wait(WaitMode.FIRST, 100, "corrlationId");
            i = i + 10;
            appendInfo("Before localWait (jumpNo=3): ", __stack, __stackPosition, analyseString);
            localWait(100, 2);
            i = i + 10;
        } finally {
            reply();
        }
    }

    private void reply() {
        backChannelQueue.enqueue(new WorkflowResult(analyseString.toString(), null));
    }

}