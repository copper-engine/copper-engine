/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.ui.dashboard.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.copperengine.monitoring.core.model.MonitoringDataProviderInfo;
import org.copperengine.monitoring.core.model.MonitoringDataStorageInfo;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;
import org.copperengine.monitoring.core.model.WorkflowStateSummary;

public class DashboardResultModel {

    public final List<ProcessingEngineInfo> engines = new ArrayList<ProcessingEngineInfo>();
    private final Map<String, WorkflowStateSummary> engineIdToStateSummery = new HashMap<String, WorkflowStateSummary>();
    public List<MonitoringDataProviderInfo> providers = new ArrayList<MonitoringDataProviderInfo>();
    public final MonitoringDataStorageInfo monitoringDataStorageInfo;

    public DashboardResultModel(Map<String, WorkflowStateSummary> engineIdToStateSummery,
            List<ProcessingEngineInfo> processingEngineInfo,
            List<MonitoringDataProviderInfo> providers,
            MonitoringDataStorageInfo monitoringDataStorageInfo) {
        this.engineIdToStateSummery.putAll(engineIdToStateSummery);
        this.engines.addAll(processingEngineInfo);
        this.providers = providers;
        this.monitoringDataStorageInfo = monitoringDataStorageInfo;
    }

    public WorkflowStateSummary getStateSummery(String engineId) {
        return engineIdToStateSummery.get(engineId);
    }

}
