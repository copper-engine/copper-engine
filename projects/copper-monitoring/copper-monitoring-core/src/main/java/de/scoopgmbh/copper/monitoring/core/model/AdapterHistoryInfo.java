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
package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.List;

public class AdapterHistoryInfo implements Serializable{
	private static final long serialVersionUID = 5463189504944441358L;
	
	List<AdapterCallInfo> adapterCalls;
	List<AdapterWfLaunchInfo> adapterWfLaunches;
	List<AdapterWfNotifyInfo> adapterWfNotifies;
	
	public List<AdapterCallInfo> getAdapterCalls() {
		return adapterCalls;
	}

	public void setAdapterCalls(List<AdapterCallInfo> adapterCalls) {
		this.adapterCalls = adapterCalls;
	}

	public AdapterHistoryInfo() {
		super();
	}

	public AdapterHistoryInfo(List<AdapterCallInfo> adapterCalls, List<AdapterWfLaunchInfo> adapterWfLaunches,
			List<AdapterWfNotifyInfo> adapterWfNotifies) {
		super();
		this.adapterCalls = adapterCalls;
		this.adapterWfLaunches = adapterWfLaunches;
		this.adapterWfNotifies = adapterWfNotifies;
	}

	public List<AdapterWfLaunchInfo> getAdapterWfLaunches() {
		return adapterWfLaunches;
	}

	public void setAdapterWfLaunches(List<AdapterWfLaunchInfo> adapterWfLaunches) {
		this.adapterWfLaunches = adapterWfLaunches;
	}

	public List<AdapterWfNotifyInfo> getAdapterWfNotifies() {
		return adapterWfNotifies;
	}

	public void setAdapterWfNotifies(List<AdapterWfNotifyInfo> adapterWfNotifies) {
		this.adapterWfNotifies = adapterWfNotifies;
	}
	
	
	
	

}
