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
package org.copperengine.regtest.test;

import java.util.ArrayList;
import java.util.List;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Callback;
import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.tranzient.TransientProcessorPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Spock2GTestWF extends Workflow<String> {

    private static final Logger logger = LoggerFactory.getLogger(Spock2GTestWF.class);
    private static final long serialVersionUID = 1816644971610832088L;

    private Callback<String> cb;
    private int idx;
    private String correlationId;
    private List<String> cids = null;

    protected transient MockAdapter mockAdapter;

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @Override
    public void main() throws Interrupt {
        logger.debug("started");

        logger.info("This is version five");

        logger.debug("changing processor pool");
        setProcessorPoolId("PS47112");
        resubmit();
        logger.debug("done resubmitting");

        logger.debug("changing processor pool again");
        setProcessorPoolId(TransientProcessorPool.DEFAULT_POOL_ID);
        super.resubmit();
        logger.debug("done resubmitting");

        abstractPartnersystemCall();

        {
            correlationId = "ThisIsACustomCorrelationId" + System.nanoTime();
            mockAdapter.foo("foo", correlationId);
            logger.debug("Request sent, waiting...");
            wait(WaitMode.ALL, 250, correlationId);
            logger.debug("Waking up again, response=" + super.getAndRemoveResponse(correlationId));
        }

        partnersystemCall();

        logger.debug("Now sleeping for 2 seconds...");
        wait(WaitMode.ALL, 2000, getEngine().createUUID());

        for (idx = 0; idx < 3; idx++) {
            cb = createCallback();
            mockAdapter.foo("foo", cb);
            logger.debug("Request sent, waiting...");
            waitForAll(cb);
            logger.debug("Waking up again...");
            logger.debug("Response = " + cb.getResponse(this));
        }

        {
            cb = super.createCallback();
            mockAdapter.foo("foo", cb);
            logger.debug("Request sent, waiting...");
            super.waitForAll(cb);
            logger.debug("Waking up again...");
            logger.debug("Response = " + cb.getResponse(this));
        }

        cids = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            correlationId = getEngine().createUUID();
            cids.add(correlationId);
            mockAdapter.foo("foo", correlationId);
        }
        logger.debug("Waiting for first...");
        super.wait(WaitMode.FIRST, 5000, cids.toArray(new String[cids.size()]));
        logger.debug("Waking up again");
        cids = null;

        logger.debug("finished");
    }

    protected void partnersystemCall() throws Interrupt {
        correlationId = getEngine().createUUID();
        mockAdapter.foo("foo", correlationId);
        logger.debug("Request sent, waiting (in subsystem call)...");
        super.wait(WaitMode.ALL, 5000, correlationId);
        logger.debug("Waking up again (in subsystem call), response=" + super.getAndRemoveResponse(correlationId));
    }

    protected abstract void abstractPartnersystemCall() throws Interrupt;

}
