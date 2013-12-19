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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.TimeUnit;

import org.copperengine.core.AutoWire;
import org.copperengine.core.InterruptException;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.test.backchannel.BackChannelQueue;
import org.copperengine.core.test.backchannel.WorkflowResult;

public class SimpleTestParentWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;

    private transient BackChannelQueue backChannelQueue;

    @AutoWire
    public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
        this.backChannelQueue = backChannelQueue;
    }

    @Override
    public void main() throws InterruptException {
        try {
            // create and launch the children
            final String id = getEngine().createUUID();

            getEngine().run(new WorkflowInstanceDescr<String>(SimpleTestChildWorkflow.class.getName(), "12345", id, null, null));

            // wait for the child to finish
            wait(WaitMode.ALL, 10000, TimeUnit.MILLISECONDS, id);

            // collect the response
            Response<String> r = getAndRemoveResponse(id);
            assertNotNull(r);
            assertNotNull(r.getResponse());
            assertNull(r.getException());
            assertFalse(r.isTimeout());
            assertEquals("54321", r.getResponse());

            backChannelQueue.enqueue(new WorkflowResult(null, null));
        } catch (Exception e) {
            e.printStackTrace();
            backChannelQueue.enqueue(new WorkflowResult(null, e));
        }
    }

}
