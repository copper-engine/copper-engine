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

import java.util.ArrayList;
import java.util.List;

import de.scoopgmbh.copper.monitoring.core.model.AdapterCallInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfNotifyInfo;
import de.scoopgmbh.copper.monitoring.core.model.LogEvent;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;

/**
 *	Contains the data for monitoring.
 *  Should only be accessed via the {@link MonitoringDataAccessQueue}
 */
public class MonitoringData {
	
	public static class LimitedBuffer<T>{
		private int limit;
		private Object[] content; 
		private int cursur;
		
		public LimitedBuffer(int limit) {
			super();
			this.limit = limit;

			cursur=0;
			content = new Object[limit];
		}
		//private ArrayDeque<T> test; slightly less performant 
		public void addWitdhLimit(T value){
			content[cursur]=value; 
			cursur++;
			if (cursur>=limit){
				cursur=0;
			}
		}

		public List<T> createList() {
			ArrayList<T> result = new ArrayList<T>();
			for (int i=0;i<limit;i++){
				int x = (i+cursur) % limit;
				@SuppressWarnings("unchecked")
				final T value = (T)content[x];
				if (value!=null){
					result.add(value);
				}
			}
			
			return result;
		}
		
	}
	
	private final LimitedBuffer<AdapterCallInfo> adapterCalls;
	private final LimitedBuffer<AdapterWfNotifyInfo> adapterWfNotifies;
	private final LimitedBuffer<AdapterWfLaunchInfo> adapterWfLaunches;
	private final LimitedBuffer<MeasurePointData> measurePoints;
	private final LimitedBuffer<LogEvent> logEvents;
	
	public MonitoringData(int adapterCallsLimit,int adapterWfNotifiesLimit, int adapterWfLaunchesLimit, int measurePointLimit, int logEventsLimit) {
		super();
		adapterCalls = new LimitedBuffer<AdapterCallInfo>(adapterCallsLimit);
		adapterWfNotifies = new LimitedBuffer<AdapterWfNotifyInfo>(adapterWfNotifiesLimit);
		adapterWfLaunches = new LimitedBuffer<AdapterWfLaunchInfo>(adapterWfLaunchesLimit);
		measurePoints = new LimitedBuffer<MeasurePointData>(measurePointLimit);
		logEvents = new LimitedBuffer<LogEvent>(logEventsLimit);
	}
	
	public MonitoringData() {
		this(1000,1000,1000,5000,1000);
	}
	
	public void addAdapterCallsWitdhLimit(AdapterCallInfo adapterCall){
		adapterCalls.addWitdhLimit(adapterCall);
	}
	
	public void addAdapterWfNotifyWitdhLimit(AdapterWfNotifyInfo adapterWfNotifyInfo){
		adapterWfNotifies.addWitdhLimit(adapterWfNotifyInfo);
	}
	
	public void addAdapterWflaunchWitdhLimit(AdapterWfLaunchInfo adapterWfLaunch){
		adapterWfLaunches.addWitdhLimit(adapterWfLaunch);
	}
	
	public void addMeasurePointWitdhLimit(MeasurePointData measurePoint){
		measurePoints.addWitdhLimit(measurePoint);
	}
	
	public void addLogEventWitdhLimit(LogEvent logEvent){
		logEvents.addWitdhLimit(logEvent);
	}

	public List<AdapterCallInfo> getAdapterCalls() {
		return adapterCalls.createList();
	}

	public List<AdapterWfNotifyInfo> getAdapterWfNotifies() {
		return adapterWfNotifies.createList();
	}

	public List<AdapterWfLaunchInfo> getAdapterWfLaunches() {
		return adapterWfLaunches.createList();
	}

	public List<MeasurePointData> getMeasurePoints() {
		return measurePoints.createList();
	}
	
	public List<LogEvent> getLogEvents() {
		return logEvents.createList();
	}

	

	
}
