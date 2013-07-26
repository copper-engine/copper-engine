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
import java.util.concurrent.Callable;

import de.scoopgmbh.copper.monitoring.core.model.AdapterCallInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfNotifyInfo;
import de.scoopgmbh.copper.monitoring.core.model.LogEvent;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;
import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitoring.core.util.PerformanceMonitor;

public class MonitoringDataCollector{
	
	private final MonitoringDataAccessQueue monitoringQueue;
	private final PerformanceMonitor performanceMonitor;
	
	public MonitoringDataCollector(final MonitoringDataAccessQueue monitoringQueue){
		this(monitoringQueue, new PerformanceMonitor());
	}

	
	public MonitoringDataCollector(final MonitoringDataAccessQueue monitoringQueue, PerformanceMonitor performanceMonitor){
		this.monitoringQueue = monitoringQueue; 
		this.performanceMonitor = performanceMonitor;
	}

	public void submitAdapterCalls(final Method method, final Object[] args, final Object adapter, final WorkflowInstanceInfo workflow) {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringDataAdder.addMonitoringData(new AdapterCallInfo(method.getName(), Arrays.toString(args), new Date(), adapter.getClass().getName(),workflow));
			}
		});
	}
	
	public void submitAdapterWfLaunch(final String wfname, final Object adapter) {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringDataAdder.addMonitoringData(new AdapterWfLaunchInfo(wfname,new Date(), adapter.getClass().getName()));
			}
		});
	}
	
	public void submitAdapterWfNotify(final String correlationId, final Object message, final Object adapter) {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringDataAdder.addMonitoringData(new AdapterWfNotifyInfo(correlationId, message!=null?message.toString():"null", new Date(), adapter.getClass().getName()));
			}
		});
	}
	
	public <T> T measureTimePeriod(String measurePointId, Callable<T> action) {
		final MeasurePointData measurePointData = new MeasurePointData(measurePointId);
		measurePointData.setElementCount(1);
		measurePointData.setCount(1);
		measurePointData.setTime(new Date());
		long timestart=System.nanoTime();
		T result;
		try {
			result = action.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		measurePointData.setElapsedTimeMicros((System.nanoTime()-timestart)/1000);
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				measurePointData.setSystemCpuLoad(performanceMonitor.createRessourcenInfo().getSystemCpuLoad());
				monitoringDataAdder.addMonitoringData(measurePointData);
			}
		});
		return result;
	}
	
	public void measureTimePeriod(String measurePointId, final Runnable action) {
		measureTimePeriod(measurePointId, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				action.run();
				return null;
			}
		});
	}

	public void submitLogEvent(final Date date, final String level, final String locationInformation,final String message) {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				dropSilently=true;
				monitoringDataAdder.addMonitoringData(new LogEvent(date,message,locationInformation,level));
			}
		});
	}

	public void submitSystemRessource(final SystemResourcesInfo resourcesInfo) {
		monitoringQueue.offer(new MonitoringDataAwareRunnable() {
			@Override
			public void run() {
				monitoringDataAdder.addMonitoringData(resourcesInfo);
			}
		});
	}

}
