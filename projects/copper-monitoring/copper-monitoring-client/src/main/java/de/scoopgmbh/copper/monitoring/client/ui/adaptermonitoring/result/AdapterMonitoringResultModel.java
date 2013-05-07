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
package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import de.scoopgmbh.copper.monitoring.core.model.AdapterCallInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterHistoryInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfNotifyInfo;

public class AdapterMonitoringResultModel {
	
	public final ObservableList<AdapterCallRowModel> adapterCalls = FXCollections.observableArrayList();
	public final ObservableList<AdapterNotifyRowModel> adapterNotifies = FXCollections.observableArrayList();
	public final ObservableList<AdapterLaunchRowModel> adapterLaunches = FXCollections.observableArrayList();
	
	
	public AdapterMonitoringResultModel(AdapterHistoryInfo adapterHistoryInfo) {
		for (AdapterCallInfo adapterCall: adapterHistoryInfo.getAdapterCalls()){
			adapterCalls.add(new AdapterCallRowModel(adapterCall));
		}
		for (AdapterWfNotifyInfo  adapterWfNotifyInfo: adapterHistoryInfo.getAdapterWfNotifies()){
			adapterNotifies.add(new AdapterNotifyRowModel(adapterWfNotifyInfo));
		}
		for (AdapterWfLaunchInfo adapterWfLaunchInfo: adapterHistoryInfo.getAdapterWfLaunches()){
			adapterLaunches.add(new AdapterLaunchRowModel(adapterWfLaunchInfo));
		}
	}
	
	
	
	
}
