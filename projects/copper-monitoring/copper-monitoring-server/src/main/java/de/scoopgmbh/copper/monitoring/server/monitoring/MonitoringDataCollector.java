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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import de.scoopgmbh.copper.monitoring.core.model.AdapterCallInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfNotifyInfo;

public class MonitoringDataCollector{
	
	private final MonitoringDataAccessQueue monitoringQueue;
	
	public MonitoringDataCollector(final MonitoringDataAccessQueue monitoringQueue){
		this.monitoringQueue = monitoringQueue; 
	}

	public void submitAdapterCalls(final Method method, final Object[] args, final Object adapter) {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringData.addAdapterCallsWitdhLimit(new AdapterCallInfo(method.getName(), Arrays.toString(args), new Date(), adapter.getClass().getName()));
			}
		});
	}
	
	public void submitAdapterWfLaunch(final String wfname, final Object adapter) {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringData.addAdapterWflaunchWitdhLimit(new AdapterWfLaunchInfo(wfname,new Date(), adapter.getClass().getName()));
			}
		});
	}
	
	public void submitAdapterWfNotify(final String correlationId, final Object message, final Object adapter) {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringData.addAdapterWfNotifyWitdhLimit(new AdapterWfNotifyInfo(correlationId, message!=null?message.toString():"null", new Date(), adapter.getClass().getName()));
			}
		});
	}



}
