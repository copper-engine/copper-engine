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


import de.scoopgmbh.copper.AutoWire;
import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;
import de.scoopgmbh.copper.sample.simplews.orchestration.server.ResetMailboxRequest;
import de.scoopgmbh.customerservice.Customer;
import de.scoopgmbh.customerservice.CustomerService;

public class ResetMailboxWorkflow extends PersistentWorkflow<ResetMailboxRequest> {
	
	private transient CustomerService customerService;
	
	@AutoWire
	public void setCustomerService(CustomerService customerService) {
		this.customerService = customerService;
	}
	
	@Override
	public void main() throws InterruptException {
		for (;;) {
			try {
				System.out.println("started");
				System.out.println("calling 'getCustomersByName("+getData().getCustomerName()+")'");
				Customer c = customerService.getCustomersByName(getData().getCustomerName());
				System.out.println("customer="+c);
				
				System.out.println("calling 'resetMailbox("+c.getCustomerId()+")'");
				customerService.resetMailbox(c.getCustomerId());
				System.out.println("finished");
				return;
			} 
			catch (Exception e) {
				System.err.println("resetMailbox failed, sleeping 5 seconds before retry.");
				wait(WaitMode.ALL, 5000, this.getEngine().createUUID());
			}
		}
	}
}
