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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.sample.datamodel.CustomerData;
import de.scoopgmbh.copper.sample.simulator.customerservice.CustomerServiceSimulator;
import de.scoopgmbh.customerservice.CustomerService;
import de.scoopgmbh.customerservice.CustomerServiceService;
import de.scoopgmbh.customerservice.GetCustomersByMsisdnRequest;
import de.scoopgmbh.customerservice.GetCustomersByMsisdnResponse;

public class CustomerServiceAdapter {
	
	private ProcessingEngine engine;
	private Executor pool = Executors.newFixedThreadPool(1);
	private CustomerService port;
	
	public CustomerServiceAdapter() throws MalformedURLException {
        CustomerServiceService ss = new CustomerServiceService(new URL(CustomerServiceSimulator.address+"?wsdl"));
        port = ss.getCustomerServicePort();  
	}

	public void setEngine(ProcessingEngine engine) {
		this.engine = engine;
	}
	
	public String readCustomer(final String msisdn) {
		final String cid = engine.createUUID();
		pool.execute(new Runnable() {
			@Override
			public void run() {
				GetCustomersByMsisdnRequest parameters = new GetCustomersByMsisdnRequest();
				parameters.setMsisdn(msisdn);
				GetCustomersByMsisdnResponse rv = port.getCustomersByMsisdn(parameters);
				CustomerData customerData = new CustomerData();
				customerData.setContractNumber(rv.getReturn().getContractNumber());
				customerData.setMsisdn(rv.getReturn().getMsisdn());
				engine.notify(new Response<CustomerData>(cid, customerData, null));
			}
		});
		return cid;
	}
}
