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
package org.copperengine.monitoring.server.monitoring;

import java.util.concurrent.Callable;

import org.copperengine.monitoring.core.data.MonitoringDataAccessor;
import org.copperengine.monitoring.core.data.MonitoringDataAdder;

public abstract class MonitoringDataAwareCallable<T> implements Callable<T> {
    protected MonitoringDataAccessor monitoringDataAccesor;
    protected MonitoringDataAdder monitoringDataAdder;

    public MonitoringDataAccessor getMonitoringDataAccesor() {
        return monitoringDataAccesor;
    }

    public void setMonitoringDataAccesor(MonitoringDataAccessor monitoringDataAccesor) {
        this.monitoringDataAccesor = monitoringDataAccesor;
    }

    public MonitoringDataAdder getMonitoringDataAdder() {
        return monitoringDataAdder;
    }

    public void setMonitoringDataAdder(MonitoringDataAdder monitoringDataAdder) {
        this.monitoringDataAdder = monitoringDataAdder;
    }

}
