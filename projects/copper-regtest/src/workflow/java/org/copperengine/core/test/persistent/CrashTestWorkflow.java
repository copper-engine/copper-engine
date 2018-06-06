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
package org.copperengine.regtest.test.persistent;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.regtest.test.MockAdapter;
import org.copperengine.regtest.test.backchannel.BackChannelQueue;
import org.copperengine.regtest.test.backchannel.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrashTestWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(CrashTestWorkflow.class);

    private transient BackChannelQueue backChannelQueue;
    private transient MockAdapter mockAdapter;

    @AutoWire
    public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
        this.backChannelQueue = backChannelQueue;
    }

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @Override
    public void main() throws Interrupt {
        logger.info("Started!");
        try {
            for (int i = 0; i < 5;) {
                final String cid = getEngine().createUUID();
                mockAdapter.foo("NA", cid, 500);
                wait(WaitMode.ALL, 2000, cid);
                Response<Object> response = getAndRemoveResponse(cid);
                if (response.isTimeout()) {
                    System.out.println("Timeout - trying again...");
                }
                else {
                    i++;
                }
            }

            backChannelQueue.enqueue(new WorkflowResult(getData(), null));
        } catch (Exception e) {
            logger.error("execution failed", e);
            backChannelQueue.enqueue(new WorkflowResult(null, e));
        }
    }
}
