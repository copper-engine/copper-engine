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
package org.copperengine.core.test.persistent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.test.backchannel.BackChannelQueue;
import org.copperengine.core.test.backchannel.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimingOutPersistentUnitTestWorkflow extends PersistentWorkflow<Serializable> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(PersistentUnitTestWorkflow.class);

    private transient BackChannelQueue backChannelQueue;

    @AutoWire
    public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
        this.backChannelQueue = backChannelQueue;
    }

    @Override
    public void main() throws Interrupt {
        try {
            String cid = getEngine().createUUID();
            wait(WaitMode.ALL, 500, cid);
            try {
                Response<?> res = getAndRemoveResponse(cid);
                logger.info(res.toString());
                assertNotNull(res);
                assertTrue(res.isTimeout());
                assertNull(res.getResponse());
                assertNull(res.getException());
            } catch (RuntimeException e) {
                logger.error("just for testing - runtime exception caught", e);
                throw e;
            }
            backChannelQueue.enqueue(new WorkflowResult(null, null));
        } catch (Exception e) {
            logger.error("execution failed", e);
            backChannelQueue.enqueue(new WorkflowResult(null, e));
        }
    }

}
