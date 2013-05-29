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
package de.scoopgmbh.copper.monitoring.example.adapter;

import java.math.BigDecimal;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Response;

public class BillAdapterImpl implements BillAdapter{
	
	private ProcessingEngine engine;
	
	
	long lastBilltime=System.currentTimeMillis();
	long lastServicetime=System.currentTimeMillis();
	
	public BillAdapterImpl(){
	}
	
	
	public void initWithEngine(ProcessingEngine enginepar){
		this.engine = enginepar;
		Thread servicesCreator = new Thread("servicesCreator"){
			@Override
			public void run() {
				while(true){
					long now = System.currentTimeMillis();
					if (lastServicetime+700<now){
						engine.notify(new Response<BillableService>(BILLABLE_SERVICE, new BillableService(new BigDecimal("5")),null));
						lastServicetime=now;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			};
		};
		servicesCreator.setDaemon(true);
		servicesCreator.start();
		
		Thread billScheduler = new Thread("billScheduler"){
			@Override
			public void run() {
				while(true){
					long now = System.currentTimeMillis();
					if (lastBilltime+5000<now){
						engine.notify(new Response<Bill>(BILL_TIME,new Bill(),null));
						lastBilltime=now;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			};
		};
		billScheduler.setDaemon(true);
		billScheduler.start();
	}
	
	@Override
	public void publishBill(Bill bill){
		System.out.println("Bill, sum:"+bill.getTotalAmount());
	}

}

