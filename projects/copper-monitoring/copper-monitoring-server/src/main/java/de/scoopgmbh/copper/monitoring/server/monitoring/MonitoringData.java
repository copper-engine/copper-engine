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
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;

/**
 *	Contains the data for monitoring.
 *  Should only be accessed via the {@link MonitoringDataAccessQueue}
 */
public class MonitoringData {
	private List<AdapterCallInfo> adapterCalls= new LinkedList<AdapterCallInfo>();
	private List<AdapterWfNotifyInfo> adapterWfNotifies= new LinkedList<AdapterWfNotifyInfo>();
	private List<AdapterWfLaunchInfo> adapterWfLaunches= new LinkedList<AdapterWfLaunchInfo>();
	private List<MeasurePointData> measurePoints= new LinkedList<MeasurePointData>();
	private final long adapterCallsLimit;
	private final long adapterWfNotifiesLimit;
	private final long adapterWfLaunchesLimit;
	private final long measurePointLimit;
	
	public MonitoringData(long adapterCallsLimit,long adapterWfNotifiesLimit, long adapterWfLaunchesLimit, long measurePointLimit) {
		super();
		this.adapterCallsLimit=adapterCallsLimit;
		this.adapterWfNotifiesLimit=adapterWfNotifiesLimit;
		this.adapterWfLaunchesLimit=adapterWfLaunchesLimit;
		this.measurePointLimit = measurePointLimit;
	}
	
	public MonitoringData() {
		this(1000,1000,1000,10000);
	}
	
	public void addAdapterCallsWitdhLimit(AdapterCallInfo adapterCall){
		if(adapterCalls.size()>=adapterCallsLimit){
			adapterCalls.remove(0);
		}
		adapterCalls.add(adapterCall);
	}
	
	public void addAdapterWfNotifyWitdhLimit(AdapterWfNotifyInfo adapterWfNotifyInfo){
		if(adapterWfNotifies.size()>=adapterWfNotifiesLimit){
			adapterWfNotifies.remove(0);
		}
		adapterWfNotifies.add(adapterWfNotifyInfo);
	}
	
	public void addAdapterWflaunchWitdhLimit(AdapterWfLaunchInfo adapterWfLaunch){
		if(adapterWfLaunches.size()>=adapterWfLaunchesLimit){
			adapterWfLaunches.remove(0);
		}
		adapterWfLaunches.add(adapterWfLaunch);
	}
	
	public void addMeasurePointWitdhLimit(MeasurePointData measurePoint){
		if(measurePoints.size()>=measurePointLimit){
			measurePoints.remove(0);
		}
		measurePoints.add(measurePoint);
	}

	public List<AdapterCallInfo> getAdapterCalls() {
		return adapterCalls;
	}

	public List<AdapterWfNotifyInfo> getAdapterWfNotifies() {
		return adapterWfNotifies;
	}

	public List<AdapterWfLaunchInfo> getAdapterWfLaunches() {
		return adapterWfLaunches;
	}

	public List<MeasurePointData> getMeasurePoints() {
		return measurePoints;
	}

	

	
}
