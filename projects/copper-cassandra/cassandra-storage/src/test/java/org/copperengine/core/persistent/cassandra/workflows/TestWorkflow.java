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

package org.copperengine.core.persistent.cassandra.workflows;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.persistent.cassandra.DummyResponseSender;
import org.copperengine.core.persistent.cassandra.TestData;
import org.copperengine.core.util.Backchannel;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TestWorkflow extends PersistentWorkflow<TestData> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(TestWorkflow.class);
    private static final int DEFAULT_TIMEOUT = 5000;

    private transient DummyResponseSender dummyResponseSender;
    private transient Backchannel backchannel;

    @AutoWire(beanId = "backchannel")
    public void setBackchannel(Backchannel backchannel) {
        this.backchannel = backchannel;
    }

    @AutoWire(beanId = "dummyResponseSender")
    public void setDummyResponseSender(DummyResponseSender dummyResponseSender) {
        this.dummyResponseSender = dummyResponseSender;
    }

    @Override
    public void main() throws Interrupt {
        try {
            logger.info("started");

            logger.info("Testing delayed response...");
            delayedResponse();

            logger.info("Testing early response...");
            earlyResponse();

            logger.info("Testing timeout response...");
            timeoutResponse();

            logger.info("Testing delayed multi response...");
            delayedMultiResponse();

            backchannel.notify(getData().id, "OK");
            logger.info("finished");
        } catch (Exception e) {
            logger.error("workflow failed", e);
            backchannel.notify(getData().id, e);
            System.exit(0);
        } catch (AssertionError e) {
            logger.error("workflow failed", e);
            backchannel.notify(getData().id, e);
            System.exit(0);
        }
    }

    private void delayedResponse() throws Interrupt {
        final String cid = getEngine().createUUID();
        dummyResponseSender.foo(cid, 100, TimeUnit.MILLISECONDS);
        wait(WaitMode.ALL, DEFAULT_TIMEOUT, cid);
        checkResponse(cid);
    }

    private void earlyResponse() throws Interrupt {
        final String cid = getEngine().createUUID();
        dummyResponseSender.foo(cid, 0, TimeUnit.MILLISECONDS);
        wait(WaitMode.ALL, DEFAULT_TIMEOUT, cid);
        checkResponse(cid);
    }

    private void checkResponse(final String cid) {
        Response<String> r = getAndRemoveResponse(cid);
        Assertions.assertNotNull(r, "Response is null for wfid=" + getId() + " and cid=" + cid);
        Assertions.assertEquals(r.getResponse(), "Unexpected response for  wfid=" + getId() + " and cid=" + cid, "foo" + cid);
    }

    private void timeoutResponse() throws Interrupt {
        final String cid = getEngine().createUUID();
        wait(WaitMode.ALL, 100, cid);
        Response<String> r = getAndRemoveResponse(cid);
        Assertions.assertNotNull(r);
        Assertions.assertNull(r.getResponse());
        Assertions.assertTrue(r.isTimeout());
    }

    private void delayedMultiResponse() throws Interrupt {
        final String cid1 = getEngine().createUUID();
        final String cid2 = getEngine().createUUID();
        final String cid3 = getEngine().createUUID();
        dummyResponseSender.foo(cid1, 50, TimeUnit.MILLISECONDS);
        dummyResponseSender.foo(cid2, 100, TimeUnit.MILLISECONDS);
        dummyResponseSender.foo(cid3, 150, TimeUnit.MILLISECONDS);
        wait(WaitMode.ALL, DEFAULT_TIMEOUT, cid1, cid2, cid3);
        checkResponse(cid1);
        checkResponse(cid2);
        checkResponse(cid3);

    }

}
