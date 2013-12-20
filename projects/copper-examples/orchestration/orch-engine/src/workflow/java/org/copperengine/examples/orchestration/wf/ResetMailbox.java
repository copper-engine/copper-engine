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
package org.copperengine.examples.orchestration.wf;

import org.copperengine.core.AutoWire;
import org.copperengine.core.InterruptException;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.WorkflowDescription;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.customerservice.CustomerService;
import org.copperengine.customerservice.GetCustomersByMsisdnRequest;
import org.copperengine.customerservice.GetCustomersByMsisdnResponse;
import org.copperengine.examples.orchestration.adapter.NetworkServiceAdapter;
import org.copperengine.examples.orchestration.adapter.ResetMailboxResponse;
import org.copperengine.examples.orchestration.data.ResetMailboxData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WorkflowDescription(alias = ResetMailboxDef.NAME, majorVersion = 1, minorVersion = 0, patchLevelVersion = 0)
public class ResetMailbox extends PersistentWorkflow<ResetMailboxData> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ResetMailbox.class);

    private transient CustomerService customerService;
    private transient NetworkServiceAdapter networkServiceAdapter;

    @AutoWire
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @AutoWire
    public void setNetworkServiceAdapter(NetworkServiceAdapter networkServiceAdapter) {
        this.networkServiceAdapter = networkServiceAdapter;
    }

    @Override
    public void main() throws InterruptException {
        logger.info("workflow instance started");
        if (!checkSecretOK()) {
            sendSms("Authentication failed");
        } else {
            if (resetMailbox()) {
                sendSms("Mailbox reset successfully executed");
            } else {
                sendSms("Unable to reset mailbox - please try again later");
            }
        }
        logger.info("workflow instance finished");
    }

    private boolean checkSecretOK() throws InterruptException {
        for (int i = 0; ; i++) {
            try {
                GetCustomersByMsisdnRequest parameters = new GetCustomersByMsisdnRequest();
                parameters.setMsisdn(getData().getMsisdn());
                GetCustomersByMsisdnResponse response = customerService.getCustomersByMsisdn(parameters);
                logger.debug("Received customer data: {}", response.getReturn());
                return getData().getSecret().equals(response.getReturn().getSecret());
            } catch (Exception e) {
                logger.error("checkSecretOK failed", e);
            }
            if (i < 5) {
                sleep(30);
            } else {
                break;
            }
        }
        return false;
    }

    private boolean resetMailbox() throws InterruptException {
        for (int i = 0; ; i++) {
            final String correlationId = networkServiceAdapter.resetMailbox(getData().getMsisdn());
            wait(WaitMode.ALL, 5 * 60 * 60 * 1000, correlationId);
            final Response<ResetMailboxResponse> response = getAndRemoveResponse(correlationId);
            if (response.isTimeout()) {
                logger.warn("resetMailbox request timed out");
            } else if (response.getException() != null) {
                logger.error("resetMailbox request failed", response.getException());
            } else if (!response.getResponse().isSuccess()) {
                logger.info("resetMailbox request failed - success = false in response");
            } else {
                logger.info("resetMailbox succeeded");
                return true;
            }
            if (i == 5) {
                logger.error("reset mailbox failed - max number of retries reached");
                return false;
            }
            sleep(30);
        }

    }

    private void sendSms(String msg) throws InterruptException {
        logger.info("sendSMS({})", msg);
        networkServiceAdapter.sendSMS(getData().getMsisdn(), msg);
    }

    private void sleep(int seconds) throws InterruptException {
        logger.info("Sleeping {} seconds up to next try...", seconds);
        wait(WaitMode.ALL, seconds * 1000, getEngine().createUUID());
    }

}
