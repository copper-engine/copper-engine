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
package org.copperengine.examples.orchestration.adapter;

import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.examples.orchestration.data.ResetMailboxData;
import org.copperengine.examples.orchestration.wf.ResetMailboxDef;
import org.copperengine.orchestration.OrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@javax.jws.WebService(
        serviceName = "OrchestrationService",
        portName = "OrchestrationServicePort",
        targetNamespace = "http://orchestration.copperengine.org/",
        wsdlLocation = "classpath:wsdl/OrchestrationEngine.wsdl",
        endpointInterface = "org.copperengine.orchestration.OrchestrationService")
public class OrchestrationInputAdapter implements OrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestrationInputAdapter.class);

    private ProcessingEngine engine;
    private String wfName = ResetMailboxDef.NAME;

    public void setWfName(String wfName) {
        this.wfName = wfName;
    }

    public void setEngine(ProcessingEngine engine) {
        this.engine = engine;
    }

    @Override
    public void resetMailbox(String msisdn, String secret) {
        try {
            logger.info("resetMailbox(msisdn={}, secret={})", msisdn, secret);
            ResetMailboxData data = new ResetMailboxData();
            data.setMsisdn(msisdn);
            data.setSecret(secret);
            WorkflowInstanceDescr<ResetMailboxData> desc = new WorkflowInstanceDescr<ResetMailboxData>(ResetMailboxDef.NAME);
            desc.setData(data);
            engine.run(desc);
            logger.info("Workflow instance {} launched", wfName);
        } catch (Exception e) {
            logger.error("resetMailbox failed", e);
        }
    }

}
