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
package de.scoopgmbh.copper.examples.orechstration.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.examples.orechstration.data.ResetMailboxData;
import de.scoopgmbh.copper.examples.orechstration.wf.ResetMailboxDef;
import de.scoopgmbh.orchestration.OrchestrationService;

@javax.jws.WebService(
		serviceName = "OrchestrationService",
		portName = "OrchestrationServicePort",
		targetNamespace = "http://orchestration.scoopgmbh.de/",
		wsdlLocation = "classpath:wsdl/OrchestrationEngine.wsdl",
		endpointInterface = "de.scoopgmbh.orchestration.OrchestrationService")
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
			engine.run(wfName, data);
			logger.info("Workflow instance {} launched", wfName);
		}
		catch(Exception e) {
			logger.error("resetMailbox failed",e);
		}
	}

}
