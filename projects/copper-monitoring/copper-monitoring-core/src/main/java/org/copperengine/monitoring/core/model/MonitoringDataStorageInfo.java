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
package org.copperengine.monitoring.core.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class MonitoringDataStorageInfo implements Serializable {
    private static final long serialVersionUID = 2363074938576106353L;
    private double sizeInMb;
    private String path;
    private Collection<MonitoringDataStorageContentInfo> monitoringDataStorageContentInfo;
    private Date min;
    private Date max;

    public MonitoringDataStorageInfo(double sizeInMb, String path, Collection<MonitoringDataStorageContentInfo> monitoringDataStorageContentInfo, Date min, Date max) {
        super();
        this.sizeInMb = sizeInMb;
        this.path = path;
        this.monitoringDataStorageContentInfo = monitoringDataStorageContentInfo;
        this.min = min;
        this.max = max;
    }

    public MonitoringDataStorageInfo() {
        super();
    }

    public double getSizeInMb() {
        return sizeInMb;
    }

    public void setSizeInMb(double sizeInMb) {
        this.sizeInMb = sizeInMb;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getMin() {
        return min;
    }

    public void setMin(Date min) {
        this.min = min;
    }

    public Date getMax() {
        return max;
    }

    public void setMax(Date max) {
        this.max = max;
    }

    public Collection<MonitoringDataStorageContentInfo> getMonitoringDataStorageContentInfo() {
        return monitoringDataStorageContentInfo;
    }

    public void setMonitoringDataStorageContentInfo(List<MonitoringDataStorageContentInfo> monitoringDataStorageContentInfo) {
        this.monitoringDataStorageContentInfo = monitoringDataStorageContentInfo;
    }

}
