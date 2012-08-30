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
package de.scoopgmbh.copper.sample.orchestration.workflows;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.AutoWire;
import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;
import de.scoopgmbh.copper.sample.datamodel.CustomerData;
import de.scoopgmbh.copper.sample.datamodel.SubscriptionRequest;
import de.scoopgmbh.copper.sample.orchestration.CustomerServiceAdapter;
import de.scoopgmbh.customerservice.Customer;

public class SubscribeWorkflow extends PersistentWorkflow<SubscriptionRequest> {
	
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(SubscribeWorkflow.class);
	
	private transient CustomerServiceAdapter customerServiceAdapter;
	
	
	@AutoWire
	public void setCustomerServiceAdapter(CustomerServiceAdapter customerServiceAdapter) {
		this.customerServiceAdapter = customerServiceAdapter;
	}
	
	@Override
	public void main() throws InterruptException {
		logger.info("started");
		for (;;) {
			try {
				logger.debug("calling 'readCustomer(msisdn={})'", getData().getMsisdn());
				String cid = customerServiceAdapter.readCustomer(getData().getMsisdn());
				wait(WaitMode.ALL,5*60*1000,cid); // Wait without blocking the thread, timeout after 5 minutes
				Response<CustomerData> response = getAndRemoveResponse(cid);
				cid = null;
				logger.debug("customer={}",response.getResponse());
				if (response.isTimeout()) {
					response = null;
					logger.warn("No response received. Trying again..");
					continue;
				}
				getData().setContractNumber(response.getResponse().getContractNumber());
				response = null;
				
				
				// TODO

				logger.info("finished");
				return;
			} 
			catch (RuntimeException e) {
				logger.error("execution failed, sleeping 5 seconds before retry.",e);
				wait(WaitMode.ALL, 5000, this.getEngine().createUUID()); // Wait for a correlationId that no one else knows.. - acts as sleep 
			}
		}
	}
}
