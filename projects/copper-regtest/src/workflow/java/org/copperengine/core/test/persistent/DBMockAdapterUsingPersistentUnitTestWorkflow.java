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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.audit.AuditTrail;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.regtest.test.DBMockAdapter;
import org.copperengine.regtest.test.backchannel.BackChannelQueue;
import org.copperengine.regtest.test.backchannel.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBMockAdapterUsingPersistentUnitTestWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(PersistentUnitTestWorkflow.class);

    private transient BackChannelQueue backChannelQueue;
    private transient DBMockAdapter dbMockAdapter;
    private transient AuditTrail auditTrail;

    @AutoWire
    public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
        this.backChannelQueue = backChannelQueue;
    }

    @AutoWire
    public void setDbMockAdapter(DBMockAdapter dbMockAdapter) {
        this.dbMockAdapter = dbMockAdapter;
    }

    @AutoWire
    public void setAuditTrail(AuditTrail auditTrail) {
        this.auditTrail = auditTrail;
    }

    @Override
    public void main() throws Interrupt {
        try {
            for (int i = 0; i < 10; i++) {
                callFoo();
                assertNotNull(this.getCreationTS());
            }
            auditTrail.asynchLog(0, new Date(), "unittest", "-", this.getId(), null, null, "finished", null);
            backChannelQueue.enqueue(new WorkflowResult(getData(), null));
        } catch (Exception e) {
            logger.error("execution failed", e);
            backChannelQueue.enqueue(new WorkflowResult(null, e));
        }
        // finally {
        // System.out.println("xxx");
        // }
    }

    private void callFoo() throws Interrupt {
        String cid = getEngine().createUUID();
        dbMockAdapter.foo(getData(), cid);
        wait(WaitMode.ALL, 10000, TimeUnit.MILLISECONDS, cid);
        Response<?> res = getAndRemoveResponse(cid);
        logger.info(res.toString());
        assertNotNull(res);
        assertFalse(res.isTimeout());
        assertEquals(getData(), res.getResponse());
        assertNull(res.getException());
        auditTrail.asynchLog(0, new Date(), "unittest", "-", this.getId(), null, null, "foo successfully called", "TEXT");
    }

}
