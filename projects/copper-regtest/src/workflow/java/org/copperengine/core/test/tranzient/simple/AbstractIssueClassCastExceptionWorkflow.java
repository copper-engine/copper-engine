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

import java.util.concurrent.TimeUnit;

import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractIssueClassCastExceptionWorkflow extends Workflow<CompletionIndicator> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AbstractIssueClassCastExceptionWorkflow.class);
    private int retriesLeft = 5;

    protected abstract void callAbstractExceptionSimulation0(String partnerLink);

    protected abstract void callAbstractExceptionSimulation1() throws Interrupt;

    protected abstract void callAbstractExceptionSimulation2(String partnerLink);

    protected void callPartner(int theWaitInterval) throws Interrupt {
        logger.warn("Start " + this.getClass().getName());
        boolean retryInterrupted = false;
        while (!retryInterrupted && retriesLeft > 0) {
            boolean callWait = callIt();
            if (callWait) {
                retryInterrupted = waitForNetRetry(theWaitInterval);
            }
        }
        logger.info("Done callPartner");
    }

    private boolean callIt() {
        try {
            callAbstractExceptionSimulation0("partnerLink");
            return false;
        } catch (Exception e) {
            logger.warn("Handle exception");
            return true;
        }
    }

    private boolean waitForNetRetry(int theWaitInterval) throws Interrupt {
        logger.info("waitForNetRetry(" + theWaitInterval + ")");
        boolean interupted = false;
        if (retriesLeft > 0) {
            retriesLeft--;
            String correlationID = "RETRY-" + this.getEngine().createUUID();
            logger.info("before WAIT");
            wait(WaitMode.FIRST, theWaitInterval, TimeUnit.MILLISECONDS, correlationID);
            logger.info("after WAIT");
            Response<String> r = getAndRemoveResponse(correlationID);
            if (logger.isInfoEnabled())
                logger.info("Response for " + correlationID + ": " + r);
            if (!r.isTimeout()) {
                if (logger.isInfoEnabled())
                    logger.info("Receiver no TIMEOUT while retring, so must be INTERRUPT_RETRY.");
                interupted = true;
            }
        }
        return interupted;
    }
}
