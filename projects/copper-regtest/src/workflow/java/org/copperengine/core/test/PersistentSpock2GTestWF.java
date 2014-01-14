/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.core.test;

import java.util.Date;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Callback;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.audit.AuditTrail;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.util.AssertException;

public class PersistentSpock2GTestWF extends PersistentWorkflow<String> {

    private static final Logger logger = LoggerFactory.getLogger(PersistentSpock2GTestWF.class);
    private static final long serialVersionUID = 1816644971610832088L;
    private static final int DEFAULT_TIMEOUT = 60000;

    private Callback<String> cb;
    private int idx;
    private String correlationId;
    private int x = 0;

    private transient MockAdapter mockAdapter;
    private transient AuditTrail auditTrail;

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
        logger.debug("started");

        // store the workflow start in the database
        auditTrail.asynchLog(1, new Date(), "conv1234", "org.copperengine.core.test.PersistentSpock2GTestWF.main", this.getId(), null, "txnId", "AUSTER TEST 12345", "TEXT");

        // do a call to a adapter e.g. a partner system
        {
            correlationId = "CUST#" + getEngine().createUUID();
            mockAdapter.foo("foo", correlationId);
            logger.debug("Request sent, waiting...");
            wait(WaitMode.ALL, DEFAULT_TIMEOUT, correlationId);
            Response<?> r = super.getAndRemoveResponse(correlationId);
            if (logger.isDebugEnabled())
                logger.debug("Waking up again, response=" + r);
            assert r != null;
            assert r.getResponse() != null || r.isTimeout();
            x++;
        }

        // do a call to a adapter by invoking a method
        partnersystemCall();
        x++;

        // do 3 calls with a callback object that obtains the response
        for (idx = 0; idx < 3; idx++) {
            cb = createCallback();
            mockAdapter.foo("foo", cb);
            logger.debug("Request sent, waiting...");
            wait(WaitMode.ALL, DEFAULT_TIMEOUT, cb);
            logger.debug("Waking up again...");
            if (logger.isDebugEnabled())
                logger.debug("Response = " + cb.getResponse(this));
            x++;
        }

        // ... and one last call
        {
            cb = super.createCallback();
            mockAdapter.foo("foo", cb);
            logger.debug("Request sent, waiting...");
            super.wait(WaitMode.ALL, DEFAULT_TIMEOUT, cb);
            logger.debug("Waking up again...");
            Response<?> response = cb.getResponse(this);
            if (logger.isDebugEnabled())
                logger.debug("Response = " + response);
            assert response != null;
            assert response.getResponse() != null || response.isTimeout();
            x++;
        }

        // check number of calls
        if (x != 6)
            throw new AssertException();
        assert x == 6;

        // decrement workflow counter
        Counter.inc();

        // report the end of this workflow
        auditTrail.asynchLog(1, new Date(), "conv123", "org.copperengine.core.test.PersistentSpock2GTestWF.main", this.getId(), null, "txnId", "FINISHED!", "TEXT");
        logger.debug("finished");
    }

    // do a adapter call and ait for terminaation
    private void partnersystemCall() throws Interrupt {
        correlationId = getEngine().createUUID();
        mockAdapter.foo("foo", correlationId);
        logger.debug("Request sent, waiting (in subsystem call)...");
        super.wait(WaitMode.ALL, DEFAULT_TIMEOUT, correlationId);
        if (logger.isDebugEnabled())
            logger.debug("Waking up again (in subsystem call), response=" + super.getAndRemoveResponse(correlationId));
    }
}
