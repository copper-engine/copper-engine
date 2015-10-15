/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.monitoring.server.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.copperengine.monitoring.core.model.MonitoringDataProviderInfo;

import com.google.common.base.Optional;

public class MonitoringDataProviderManager {

    List<MonitoringDataProvider> provider = new ArrayList<MonitoringDataProvider>();

    public MonitoringDataProviderManager(MonitoringDataProvider... provider) {
        this(Arrays.asList(provider));
    }

    public MonitoringDataProviderManager(List<MonitoringDataProvider> provider) {
        super();
        this.provider = provider;
    }

    public void stopAll() {
        for (MonitoringDataProvider monitoringDataProvider : provider) {
            monitoringDataProvider.stopProvider();
        }
    }

    public void startAll() {
        for (MonitoringDataProvider monitoringDataProvider : provider) {
            monitoringDataProvider.startProvider();
        }
    }

    public List<MonitoringDataProviderInfo> getInfos() {
        ArrayList<MonitoringDataProviderInfo> result = new ArrayList<MonitoringDataProviderInfo>();
        for (MonitoringDataProvider monitoringDataProvider : provider) {
            result.add(monitoringDataProvider.createInfo());
        }
        return result;
    }

    public Optional<MonitoringDataProvider> getProvider(String name) {
        for (MonitoringDataProvider monitoringDataProvider : provider) {
            if (monitoringDataProvider.getProviderName().equals(name)) {
                return Optional.of(monitoringDataProvider);
            }
        }
        return Optional.absent();
    }

}
