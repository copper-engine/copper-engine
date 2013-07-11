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
package de.scoopgmbh.copper.monitoring.server.monitoring;

import org.junit.Test;


public class MonitoringDataAccessQueueTest {
	private static final class Mock extends MonitoringDataAwareRunnable {
		boolean endless=true;
		@Override
		public void run() {
			while(endless){
				
			}
		}
	}

	@Test
	public void test_offer_to_full(){
		MonitoringDataAccessQueue monitoringEventQueue = new MonitoringDataAccessQueue(1, null,null);
		Mock runnable = new Mock();
		monitoringEventQueue.offer(runnable);
		
		monitoringEventQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				System.out.println(Math.random());
			}
		});
		
		//runnable.endless=false;
	}
}
