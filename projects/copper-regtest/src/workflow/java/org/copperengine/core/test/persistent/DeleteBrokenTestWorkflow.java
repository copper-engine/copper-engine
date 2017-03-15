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

package org.copperengine.core.test.persistent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Response;
import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.test.MockAdapter;
import org.copperengine.core.test.backchannel.BackChannelQueue;
import org.copperengine.core.test.backchannel.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class DeleteBrokenTestWorkflow extends PersistentWorkflow<Integer> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DeleteBrokenTestWorkflow.class);

    private transient MockAdapter mockAdapter;

    private transient BackChannelQueue backChannelQueue;

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @AutoWire
    public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
        this.backChannelQueue = backChannelQueue;
    }

    @Override
    public void main() throws Interrupt {
        assert (getData() >= 0);
        assert (getData() < 5);

        // Send a response, wait for it and create a unique ID which should not be responded but waited to.
        String cid = getEngine().createUUID();
        final String adapterString = "abc";
        mockAdapter.foo(adapterString, cid, 50);
        final String unusedCid = getEngine().createUUID();
        wait(WaitMode.FIRST, 1, TimeUnit.SECONDS, cid, unusedCid);

        // Now there should be two Responses which can be queried. One timed out, one "real".
        Response<String> knownResponse = getAndRemoveResponse(cid);
        assertNotNull(knownResponse);
        assertNotNull(knownResponse.getResponse());
        assertTrue(knownResponse.getResponse().equals(adapterString));

        // Let all workflow finish but bring one to error state...
        if(getData().intValue() == 1) {
            throw new RuntimeException("Bring me to ERROR state");
        } else {
            backChannelQueue.enqueue(new WorkflowResult(new Integer(1), null));
        }
    }

}
