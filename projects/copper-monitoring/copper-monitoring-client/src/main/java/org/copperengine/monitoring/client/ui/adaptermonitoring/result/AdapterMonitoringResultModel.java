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
package org.copperengine.monitoring.client.ui.adaptermonitoring.result;

import javafx.collections.ObservableList;

public class AdapterMonitoringResultModel {

    public final ObservableList<AdapterCallRowModel> adapterCalls;
    public final ObservableList<AdapterNotifyRowModel> adapterNotifies;
    public final ObservableList<AdapterLaunchRowModel> adapterLaunches;

    public AdapterMonitoringResultModel(ObservableList<AdapterCallRowModel> adapterCalls,
            ObservableList<AdapterNotifyRowModel> adapterNotifies, ObservableList<AdapterLaunchRowModel> adapterLaunches) {
        super();
        this.adapterCalls = adapterCalls;
        this.adapterNotifies = adapterNotifies;
        this.adapterLaunches = adapterLaunches;
    }

}
