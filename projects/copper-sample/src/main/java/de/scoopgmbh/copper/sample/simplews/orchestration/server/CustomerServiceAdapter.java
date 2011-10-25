/*
 * Copyright 2002-2011 SCOOP Software GmbH
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
package de.scoopgmbh.copper.sample.simplews.orchestration.server;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.customerservice.Customer;
import de.scoopgmbh.customerservice.CustomerService;
import de.scoopgmbh.customerservice.GetCustomersByNameResponse;
import de.scoopgmbh.customerservice.ResetMailboxResponse;

public class CustomerServiceAdapter {
	
	private CustomerService customerService;
	
	public void setCustomerService(CustomerService customerService) {
		this.customerService = customerService;
	}
	
	
	/**
	 * read customer data by customername
	 * @param customername
	 * @return correlationId
	 */
	public String getCustomersByNameAsync(final String customername, final ProcessingEngine engine) {
		final String correlationId = engine.createUUID();
		// call web service asynchronously
		customerService.getCustomersByNameAsync(customername, new AsyncHandler<GetCustomersByNameResponse>() {
			@Override
			public void handleResponse(Response<GetCustomersByNameResponse> res) {
				try {
					de.scoopgmbh.copper.Response<Customer> response = new de.scoopgmbh.copper.Response<Customer>(correlationId,res.get().getReturn(),null);
					// notify caller via engine
					engine.notify(response);
				} 
				catch (Exception e) {
					e.printStackTrace();
					engine.notify(new de.scoopgmbh.copper.Response<GetCustomersByNameResponse>(correlationId,null,e));
				} 
			}
		});
		// return correlation id to caller 
		return correlationId;
	}

	/**
	 * resets mailbox
	 * @param customerId
	 * @return correlationId
	 */
    public String resetMailbox(int customerId, final ProcessingEngine engine) {
		final String correlationId = engine.createUUID();
		// call web service asynchronously
		customerService.resetMailboxAsync(customerId, new AsyncHandler<ResetMailboxResponse>() {
			@Override
			public void handleResponse(Response<ResetMailboxResponse> res) {
				try {
					de.scoopgmbh.copper.Response<Object> response = new de.scoopgmbh.copper.Response<Object>(correlationId,null,null);
					// notify caller via engine
					engine.notify(response);
				} 
				catch (Exception e) {
					e.printStackTrace();
					engine.notify(new de.scoopgmbh.copper.Response<ResetMailboxResponse>(correlationId,null,e));
				} 
			}
		});
		// return correlation id to caller 
		return correlationId;
    }
	
	
	
}
