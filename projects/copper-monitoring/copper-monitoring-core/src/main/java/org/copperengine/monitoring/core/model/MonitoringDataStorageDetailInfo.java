/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
import java.util.Collection;
import java.util.List;

public class MonitoringDataStorageDetailInfo implements Serializable {
    private static final long serialVersionUID = 1363074938576106353L;

    private Collection<MonitoringDataStorageContentInfo> monitoringDataStorageContentInfo;

    public MonitoringDataStorageDetailInfo(Collection<MonitoringDataStorageContentInfo> monitoringDataStorageContentInfo) {
        super();
        this.monitoringDataStorageContentInfo = monitoringDataStorageContentInfo;
    }

    public MonitoringDataStorageDetailInfo() {
        super();
    }

    public Collection<MonitoringDataStorageContentInfo> getMonitoringDataStorageContentInfo() {
        return monitoringDataStorageContentInfo;
    }

    public void setMonitoringDataStorageContentInfo(List<MonitoringDataStorageContentInfo> monitoringDataStorageContentInfo) {
        this.monitoringDataStorageContentInfo = monitoringDataStorageContentInfo;
    }

}
