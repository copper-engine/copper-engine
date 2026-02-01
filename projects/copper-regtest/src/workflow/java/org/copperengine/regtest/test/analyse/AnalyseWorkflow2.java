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

import java.util.List;

import org.copperengine.core.Auditor;
import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.regtest.test.backchannel.BackChannelQueue;
import org.copperengine.regtest.test.backchannel.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyseWorkflow2 extends Workflow<Integer> implements Auditor {

    private static final Logger log = LoggerFactory.getLogger(AnalyseWorkflow2.class);
    @AutoWire
    public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
        this.backChannelQueue = backChannelQueue;
    }

    private BackChannelQueue backChannelQueue;

    // not used as a local, so that the content is not coupled on itself
    private StringBuffer analyseString = new StringBuffer();

    @Override
    public void start() {
        log.info("START");
        appendInfo("start: ", __stack, __stackPosition, analyseString);
    }

    @Override
    public void interrupt(List<Integer> jumpNos) {
        log.info("INTERRUPT {}", jumpNos);
        appendInfo("interrupt %s: ".formatted(jumpNos), __stack, __stackPosition, analyseString);
    }

    @Override
    public void resume(List<Integer> jumpNos) {
        log.info("RESUME {}", jumpNos);
        appendInfo("resume %s: ".formatted(jumpNos), __stack, __stackPosition, analyseString);
    }

    @Override
    public void end() {
        log.info("END");
        appendInfo("end: ", __stack, __stackPosition, analyseString); // after reply
    }

    @Override
    public void error(List<Integer> jumpNos) {
        log.info("ERROR");
        appendInfo("error %s: ".formatted(jumpNos), __stack, __stackPosition, analyseString);
    }

    private void localWait(int delay, int depth) throws Interrupt {
        wait(WaitMode.FIRST, delay, "corrlationId");
        if (depth > 1) {
            localWait(100, --depth);
        }
    }

    public void main() throws Interrupt {
        try {
            int i = 0;
            String s = "Hello in main()!";
            resubmit();
            i = i + 10;
            resubmit();
            i = i + 10;
            wait(WaitMode.FIRST, 100, "corrlationId");
            i = i + 10;
            localWait(100, 2);
            i = i + 10;
            if (i > 0) {
                throw new RuntimeException("i > 0");
            }
        } catch (Exception e) {
            resubmit();
        } finally {
            resubmit();
        }

        try {
            int i = 666;
            String s = "Hello 2in main()!";
            resubmit();
        } catch (Exception e) {
            resubmit();
        } finally {
            resubmit();
            reply();
        }
    }

    private void reply() {
        backChannelQueue.enqueue(new WorkflowResult(analyseString.toString(), null));
    }

}