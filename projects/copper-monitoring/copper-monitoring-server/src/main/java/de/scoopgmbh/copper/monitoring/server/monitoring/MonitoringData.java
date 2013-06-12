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

import java.util.LinkedList;
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
	
	public static class LimitedList<T>{
		public long limit;
		
		public LimitedList(long limit) {
			super();
			this.limit = limit;
		}
		private List<T> list= new LinkedList<T>();
		public void addWitdhLimit(T value){
			if(list.size()>=limit){
				list.remove(0);
			}
			list.add(value);
		}
		public List<T> getList() {
			return list;
		}
		
	}
	
	private final LimitedList<AdapterCallInfo> adapterCalls;
	private final LimitedList<AdapterWfNotifyInfo> adapterWfNotifies;
	private final LimitedList<AdapterWfLaunchInfo> adapterWfLaunches;
	private final LimitedList<MeasurePointData> measurePoints;
	private final LimitedList<LogEvent> logEvents;
	
	public MonitoringData(long adapterCallsLimit,long adapterWfNotifiesLimit, long adapterWfLaunchesLimit, long measurePointLimit, long logEventsLimit) {
		super();
		adapterCalls = new LimitedList<AdapterCallInfo>(adapterCallsLimit);
		adapterWfNotifies = new LimitedList<AdapterWfNotifyInfo>(adapterWfNotifiesLimit);
		adapterWfLaunches = new LimitedList<AdapterWfLaunchInfo>(adapterWfLaunchesLimit);
		measurePoints = new LimitedList<MeasurePointData>(measurePointLimit);
		logEvents = new LimitedList<LogEvent>(logEventsLimit);
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
		return adapterCalls.getList();
	}

	public List<AdapterWfNotifyInfo> getAdapterWfNotifies() {
		return adapterWfNotifies.getList();
	}

	public List<AdapterWfLaunchInfo> getAdapterWfLaunches() {
		return adapterWfLaunches.getList();
	}

	public List<MeasurePointData> getMeasurePoints() {
		return measurePoints.getList();
	}
	
	public List<LogEvent> getLogEvents() {
		return logEvents.getList();
	}

	

	
}
