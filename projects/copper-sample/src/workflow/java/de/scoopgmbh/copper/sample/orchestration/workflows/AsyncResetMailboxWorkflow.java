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
package de.scoopgmbh.copper.sample.orchestration.workflows;


import de.scoopgmbh.copper.AutoWire;
import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;
import de.scoopgmbh.copper.sample.simplews.orchestration.server.CustomerServiceAdapter;
import de.scoopgmbh.copper.sample.simplews.orchestration.server.ResetMailboxRequest;
import de.scoopgmbh.customerservice.Customer;
import de.scoopgmbh.customerservice.GetCustomersByNameResponse;

public class AsyncResetMailboxWorkflow extends PersistentWorkflow<ResetMailboxRequest> {
	
	private transient CustomerServiceAdapter customerServiceAdapter;
	
	@AutoWire
	public void setCustomerServiceAdapter(CustomerServiceAdapter customerServiceAdapter) {
		this.customerServiceAdapter = customerServiceAdapter;
	}
	
	@Override
	public void main() throws InterruptException {
		for (;;) {
			try {
				System.out.println("started");

				System.out.println("calling 'getCustomersByName("+getData().getCustomerName()+")'");
				String cid = customerServiceAdapter.getCustomersByNameAsync(getData().getCustomerName(), getEngine());
				wait(WaitMode.ALL,5*1000,cid); // Wait without blocking the thread, timeout after 5 minutes
				Response<Customer> response = getAndRemoveResponse(cid);
				final Customer c = response.getResponse();
				System.out.println("customer="+c);
				
				
				resubmit();
				
				System.out.println("calling 'resetMailbox("+c.getCustomerId()+")'");
				cid = customerServiceAdapter.resetMailbox(c.getCustomerId(), getEngine());
				wait(WaitMode.ALL,5*1000,cid); // Wait without blocking the thread, timeout after 5 minutes
				getAndRemoveResponse(cid); // ignore the response

				System.out.println("finished");
				return;
			} 
			catch (RuntimeException e) {
				e.printStackTrace();
				System.err.println("resetMailbox failed, sleeping 5 seconds before retry.");
				wait(WaitMode.ALL, 5000, this.getEngine().createUUID());
			}
		}
	}
}
