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
package de.scoopgmbh.copper.monitoring.example.monitoringprovider;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;

import de.scoopgmbh.copper.monitoring.core.model.GenericMonitoringData;
import de.scoopgmbh.copper.monitoring.core.model.GenericMonitoringData.ContentType;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataCollector;
import de.scoopgmbh.copper.monitoring.server.provider.RepeatingMonitoringDataProviderBase;

public class GcDataProvider extends RepeatingMonitoringDataProviderBase {

    public GcDataProvider(MonitoringDataCollector monitoringDataCollector) {
        super(monitoringDataCollector);
    }

    @Override
    protected void provideData() {
        StringBuilder result = new StringBuilder();
        result.append("<html>");
        List<java.lang.management.GarbageCollectorMXBean> gcmxb = ManagementFactory.getGarbageCollectorMXBeans();
        for (java.lang.management.GarbageCollectorMXBean ob : gcmxb) {
            result.append("name of memory manager:" + ob.getName() + "<br/>");
            result.append("CollectionTime:" + ob.getCollectionTime() + "<br/>");
        }
        result.append("</html>");
        GenericMonitoringData dat = new GenericMonitoringData(new Date(), result.toString(), ContentType.HTML, "GcData");
        monitoringDataCollector.submitGenericMonitoringData(dat);
    }

}
