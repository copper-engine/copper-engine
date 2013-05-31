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
package de.scoopgmbh.copper.monitoring.example.workflow;

import java.math.BigDecimal;
import java.util.ArrayList;

import de.scoopgmbh.copper.AutoWire;
import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowDescription;
import de.scoopgmbh.copper.monitoring.example.adapter.Bill;
import de.scoopgmbh.copper.monitoring.example.adapter.BillAdapter;
import de.scoopgmbh.copper.monitoring.example.adapter.BillableService;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;

@WorkflowDescription(alias="BillWorkflow", majorVersion=1, minorVersion=0, patchLevelVersion=0)
public class BillWorkflow extends PersistentWorkflow<String> {
	private static final long serialVersionUID = 1L;

	private transient BillAdapter billAdapter;
	
	private ArrayList<BillableService> billableServices= new ArrayList<BillableService>();

	@AutoWire
	public void setBillAdapter(BillAdapter billAdapter) {
		this.billAdapter = billAdapter;
	}

	@Override
	public void main() throws InterruptException {
		while (true){
			wait(WaitMode.ALL,Workflow.NO_TIMEOUT, BillAdapter.BILL_TIME,BillAdapter.BILLABLE_SERVICE);

			
			ArrayList<Response<?>> all = new ArrayList<Response<?>>(getAndRemoveResponses(BillAdapter.BILL_TIME));
			all.addAll(getAndRemoveResponses(BillAdapter.BILLABLE_SERVICE));
			
			for(Response<?> response: all){
				if (response.getResponse() instanceof BillableService){
					billableServices.add(((BillableService)response.getResponse()));
				}
			}
			for(Response<?> response: all){
				if (response.getResponse() instanceof Bill){
					Bill bill = ((Bill)response.getResponse());
					BigDecimal sum = new BigDecimal(0);
					for (BillableService billableService: billableServices){
						sum = sum.add(billableService.getAmount());
					}
					bill.setTotalAmount(sum);
					billAdapter.publishBill(bill);
					billableServices.clear();
				}
			}
			
			
		}
		
	}

}
