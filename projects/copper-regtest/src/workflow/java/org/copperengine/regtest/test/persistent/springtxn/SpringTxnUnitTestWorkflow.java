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
package org.copperengine.regtest.test.persistent.springtxn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.audit.AuditTrail;
import org.copperengine.core.audit.AuditTrailEvent;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.regtest.test.MockAdapter;
import org.copperengine.regtest.test.backchannel.BackChannelQueue;
import org.copperengine.regtest.test.backchannel.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringTxnUnitTestWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SpringTxnUnitTestWorkflow.class);

    private transient BackChannelQueue backChannelQueue;
    private transient MockAdapter mockAdapter;
    private transient AuditTrail auditTrail;

    @AutoWire
    public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
        this.backChannelQueue = backChannelQueue;
    }

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @AutoWire
    public void setAuditTrail(AuditTrail auditTrail) {
        this.auditTrail = auditTrail;
    }

    @Override
    public void main() throws Interrupt {
        try {
            for (int i = 0; i < 3; i++) {
                callFoo();
                assertNotNull(this.getCreationTS());
            }
            backChannelQueue.enqueue(new WorkflowResult(getData(), null));
            backChannelQueue = null;
            throw new RuntimeException("test exception to abort execution!!!");
        } catch (RuntimeException e) {
            logger.error("execution failed", e);
            if (backChannelQueue != null)
                backChannelQueue.enqueue(new WorkflowResult(null, e));
            throw e;
        }
    }

    private void callFoo() throws Interrupt {
        String cid = getEngine().createUUID();
        // This is running within the current DB transaction
        auditTrail.synchLog(new AuditTrailEvent(1, new Date(), cid, "beforeFoo", getId(), cid, cid, "beforeFoo", "String", null));

        mockAdapter.foo(getData(), cid);

        // current Txn ends here
        wait(WaitMode.ALL, 10000, TimeUnit.MILLISECONDS, cid);
        // new Txn starts here

        Response<?> res = getAndRemoveResponse(cid);
        logger.info(res.toString());

        auditTrail.synchLog(new AuditTrailEvent(1, new Date(), cid, "afterFoo", getId(), cid, cid, "afterFoo - result = " + res.toString(), "String", null));

        assertNotNull(res);
        assertFalse(res.isTimeout());
        assertEquals(getData(), res.getResponse());
        assertNull(res.getException());

        // This is also running within the current DB transaction
        auditTrail.synchLog(new AuditTrailEvent(1, new Date(), cid, "Assertions checked", getId(), cid, cid, "Assertions checked", "String", null));
    }

}
