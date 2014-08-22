/*
 * Copyright 2002-2014 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.copperengine.monitoring.core.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ConfigurationInfo implements MonitoringData, Serializable {
    private static final long serialVersionUID = 1L;

    private Date date;

    public ConfigurationInfo(Date date, List<ProcessingEngineInfo> engines, List<MonitoringDataProviderInfo> providers, MonitoringDataStorageInfo monitoringDataStorageInfo) {
        this.date = date;
        this.engines = engines;
        this.providers = providers;
        this.monitoringDataStorageInfo = monitoringDataStorageInfo;
    }

    private ConfigurationInfo() {//for kryo

    }

    public List<ProcessingEngineInfo> getEngines() {
        return engines;
    }

    public void setEngines(List<ProcessingEngineInfo> engines) {
        this.engines = engines;
    }

    public List<MonitoringDataProviderInfo> getProviders() {
        return providers;
    }

    public void setProviders(List<MonitoringDataProviderInfo> providers) {
        this.providers = providers;
    }

    public MonitoringDataStorageInfo getMonitoringDataStorageInfo() {
        return monitoringDataStorageInfo;
    }

    public void setMonitoringDataStorageInfo(MonitoringDataStorageInfo monitoringDataStorageInfo) {
        this.monitoringDataStorageInfo = monitoringDataStorageInfo;
    }

    private List<ProcessingEngineInfo> engines;
    private List<MonitoringDataProviderInfo> providers;
    private MonitoringDataStorageInfo monitoringDataStorageInfo;




    @Override
    public Date getTimeStamp() {
        return date;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
