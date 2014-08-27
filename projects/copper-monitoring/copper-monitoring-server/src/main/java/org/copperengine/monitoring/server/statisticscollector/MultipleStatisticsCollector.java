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
package org.copperengine.monitoring.server.statisticscollector;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.monitoring.RuntimeStatisticsCollector;

public class MultipleStatisticsCollector implements RuntimeStatisticsCollector {

    CopyOnWriteArrayList<RuntimeStatisticsCollector> runtimeStatisticsCollectors = new CopyOnWriteArrayList<RuntimeStatisticsCollector>();

    public MultipleStatisticsCollector(List<RuntimeStatisticsCollector> runtimeStatisticsCollectors) {
        this.runtimeStatisticsCollectors.addAll(runtimeStatisticsCollectors);
    }

    public MultipleStatisticsCollector(RuntimeStatisticsCollector... runtimeStatisticsCollectors) {
        this.runtimeStatisticsCollectors.addAll(Arrays.asList(runtimeStatisticsCollectors));
    }

    @Override
    public void submit(String measurePointId, int elementCount, long elapsedTime, TimeUnit timeUnit) {
        for (RuntimeStatisticsCollector runtimeStatisticsCollector: runtimeStatisticsCollectors){
            runtimeStatisticsCollector.submit(measurePointId,elementCount,elapsedTime,timeUnit);
        }
    }

    public void addStatisticsCollector(RuntimeStatisticsCollector runtimeStatisticsCollector){
        runtimeStatisticsCollectors.add(runtimeStatisticsCollector);
    }
}
