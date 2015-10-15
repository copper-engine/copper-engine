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
package org.copperengine.monitoring.server.monitoring;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.copperengine.monitoring.core.model.AdapterCallInfo;
import org.copperengine.monitoring.core.model.AdapterWfLaunchInfo;
import org.copperengine.monitoring.core.model.AdapterWfNotifyInfo;
import org.copperengine.monitoring.core.model.ConfigurationInfo;
import org.copperengine.monitoring.core.model.GenericMonitoringData;
import org.copperengine.monitoring.core.model.LogEvent;
import org.copperengine.monitoring.core.model.MeasurePointData;
import org.copperengine.monitoring.core.model.MonitoringDataProviderInfo;
import org.copperengine.monitoring.core.model.MonitoringDataStorageInfo;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;
import org.copperengine.monitoring.core.model.SystemResourcesInfo;
import org.copperengine.monitoring.core.model.WorkflowInstanceInfo;
import org.copperengine.monitoring.core.util.CachingPerformanceMonitor;
import org.copperengine.monitoring.core.util.PerformanceMonitor;

public class MonitoringDataCollector {

    private final MonitoringDataAccessQueue monitoringQueue;
    private final CachingPerformanceMonitor performanceMonitor;

    public MonitoringDataCollector(final MonitoringDataAccessQueue monitoringQueue) {
        this(monitoringQueue, new PerformanceMonitor());
    }

    public MonitoringDataCollector(final MonitoringDataAccessQueue monitoringQueue, PerformanceMonitor performanceMonitor) {
        this.monitoringQueue = monitoringQueue;
        this.performanceMonitor = new CachingPerformanceMonitor(performanceMonitor);
    }

    public void submitAdapterCalls(final Method method, final Object[] args, final Object adapter, final WorkflowInstanceInfo workflow) {
        monitoringQueue.offer(new MonitoringDataAwareRunnable() {
            @Override
            public void run() {
                monitoringDataAdder.addMonitoringData(new AdapterCallInfo(method.getName(), Arrays.toString(args), new Date(), adapter.getClass().getName(), workflow));
            }
        });
    }

    public void submitAdapterWfLaunch(final String wfname, final Object adapter) {
        monitoringQueue.offer(new MonitoringDataAwareRunnable() {
            @Override
            public void run() {
                monitoringDataAdder.addMonitoringData(new AdapterWfLaunchInfo(wfname, new Date(), adapter.getClass().getName()));
            }
        });
    }

    public void submitAdapterWfNotify(final String correlationId, final Object message, final Object adapter) {
        monitoringQueue.offer(new MonitoringDataAwareRunnable() {
            @Override
            public void run() {
                monitoringDataAdder.addMonitoringData(new AdapterWfNotifyInfo(correlationId, message != null ? message.toString() : "null", new Date(), adapter.getClass().getName()));
            }
        });
    }

    public void submitMeasurePoint(final String measurePointId, final int elementCount, final long elapsedTime, final TimeUnit timeUnit) {
        monitoringQueue.offer(new MonitoringDataAwareRunnable() {
            @Override
            public void run() {
                final MeasurePointData measurePointData = new MeasurePointData(measurePointId);
                measurePointData.setSystemCpuLoad(performanceMonitor.getCachedSystemResourcesInfo().getSystemCpuLoad());
                measurePointData.setElementCount(elementCount);
                measurePointData.setCount(1);
                measurePointData.setTime(new Date());
                measurePointData.setElapsedTimeMicros(timeUnit.toMicros(elapsedTime));
                monitoringDataAdder.addMonitoringData(measurePointData);
            }
        });
    }

    public <T> T measureTimePeriod(String measurePointId, Callable<T> action) {
        final MeasurePointData measurePointData = new MeasurePointData(measurePointId);
        measurePointData.setElementCount(1);
        measurePointData.setCount(1);
        measurePointData.setTime(new Date());
        long timestart = System.nanoTime();
        T result;
        try {
            result = action.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        measurePointData.setElapsedTimeMicros((System.nanoTime() - timestart) / 1000);
        monitoringQueue.offer(new MonitoringDataAwareRunnable() {
            @Override
            public void run() {
                measurePointData.setSystemCpuLoad(performanceMonitor.getCachedSystemResourcesInfo().getSystemCpuLoad());
                monitoringDataAdder.addMonitoringData(measurePointData);
            }
        });
        return result;
    }

    public void measureTimePeriod(String measurePointId, final Runnable action) {
        measureTimePeriod(measurePointId, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                action.run();
                return null;
            }
        });
    }

    public void submitLogEvent(final Date date, final String level, final String locationInformation, final String message) {
        monitoringQueue.offer(new MonitoringDataAwareRunnable() {
            @Override
            public void run() {
                dropSilently = true;
                monitoringDataAdder.addMonitoringData(new LogEvent(date, message, locationInformation, level));
            }
        });
    }

    public void submitSystemResource(final SystemResourcesInfo resourcesInfo) {
        monitoringQueue.offer(new MonitoringDataAwareRunnable() {
            @Override
            public void run() {
                monitoringDataAdder.addMonitoringData(resourcesInfo);
            }
        });
    }

    public void submitGenericMonitoringData(final GenericMonitoringData genericMonitoringData) {
        monitoringQueue.offer(new MonitoringDataAwareRunnable() {
            @Override
            public void run() {
                monitoringDataAdder.addMonitoringData(genericMonitoringData);
            }
        });
    }

    public void submitConfiguration(final List<ProcessingEngineInfo> processingEngineInfos, final List<MonitoringDataProviderInfo> providers, final MonitoringDataStorageInfo monitoringDataStorageInfo) {
        monitoringQueue.offer(new MonitoringDataAwareRunnable() {
            @Override
            public void run() {
                final ConfigurationInfo configuration = new ConfigurationInfo(new Date(), processingEngineInfos,providers,monitoringDataStorageInfo);
                monitoringDataAdder.addMonitoringData(configuration);
            }
        });
    }


}
