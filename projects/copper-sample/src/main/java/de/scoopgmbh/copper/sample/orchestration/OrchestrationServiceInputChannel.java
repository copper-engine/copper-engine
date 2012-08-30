/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.sample.orchestration;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.sample.datamodel.SubscriptionRequest;
import de.scoopgmbh.orchestration.OrchestrationService;

@javax.jws.WebService(
		serviceName = "OrchestrationService",
		portName = "OrchestrationServicePort",
		targetNamespace = "http://orchestration.scoopgmbh.de/",
		wsdlLocation = "classpath:wsdl/Orchestration.wsdl",
		endpointInterface = "de.scoopgmbh.orchestration.OrchestrationService")
@SchemaValidation(enabled=true)                      
public class OrchestrationServiceInputChannel implements OrchestrationService {

	private static final Logger logger = LoggerFactory.getLogger(OrchestrationServiceInputChannel.class);

	private final ProcessingEngine engine;

	public OrchestrationServiceInputChannel(ProcessingEngine engine) {
		this.engine = engine;
	}

	public void createSubscription(java.lang.String msisdn, java.lang.String subscriptionTemplateId) {
		try {
			logger.info("createSubscription({},{})", msisdn, subscriptionTemplateId);
			SubscriptionRequest request = new SubscriptionRequest();
			request.setMsisdn(msisdn);
			request.setSubscriptionTemplateId(subscriptionTemplateId);
			engine.run("de.scoopgmbh.copper.sample.orchestration.workflows.SubscribeWorkflow", request);
		}
		catch(RuntimeException e) {
			throw e;
		}
		catch(Exception e) {
			throw new RuntimeException("createSubscription failed",e);
		}
	}

}
