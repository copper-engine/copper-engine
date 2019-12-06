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

import org.copperengine.core.*;
import org.copperengine.regtest.test.backchannel.WorkflowResult;

import org.copperengine.regtest.test.backchannel.BackChannelQueue;

import static org.junit.Assert.assertTrue;

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
        appendInfo("Before wait in localWait (depth=" + depth + ") :");
        wait(WaitMode.FIRST, delay, "corrlationId");
        appendInfo("After  wait in localWait (depth=" + depth + "): ");
        if (depth > 1) {
            localWait(100, --depth);
        }
    }
    public void main() throws Interrupt {
        try {
            int i = 0;
            String s = "Hello in main()!";
            appendInfo("Before resubmit (jumpNo = 0): ");
            resubmit();
            i = i + 10;
            appendInfo("Before resubmit (jumpNo=1): ");
            resubmit();
            i = i + 10;
            appendInfo("Before wait (jumpNo=2): ");
            wait(WaitMode.FIRST, 100, "corrlationId");
            i = i + 10;
            appendInfo("Before localWait (jumpNo=3): ");
            localWait(100, 2);
            i = i + 10;
        } finally {
            reply();
        }
    }


    private void appendInfo(String message) {
        analyseString.append(message).append("__stackPosition=").append(__stackPosition).append("\n\t__stack=");
        appendStack();
        analyseString.append("\n");
    }

    private void appendStack() {
        analyseString.append("[");
        for (int i = 0; i < __stack.size(); i++) {
            analyseString.append("[");
            StackEntry entry = __stack.get(i);
            analyseString.append("\n\tjumpNo=").append(entry.jumpNo).append("\n\tlocals=[");
            for (int j = 0; j < entry.locals.length; j++) {
                if (j > 0) {
                    analyseString.append(",");
                }
                analyseString.append(entry.locals[j]);
            }
            analyseString.append("]\n\tstack=[");
            for (int j = 0; j < entry.stack.length; j++) {
                if (j > 0) {
                    analyseString.append(",");
                }
                analyseString.append(entry.stack[j]);
            }
            analyseString.append("]");
            analyseString.append("]");
        }
        analyseString.append("]");
    }

    private void reply() {
        backChannelQueue.enqueue(new WorkflowResult(analyseString.toString(), null));
    }

}