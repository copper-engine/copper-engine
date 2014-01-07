package org.copperengine.monitoring.server.statisticcollector;

import java.util.concurrent.TimeUnit;

import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.monitoring.server.monitoring.MonitoringDataCollector;

public class MonitoringStatisticCollector implements RuntimeStatisticsCollector {
    private final MonitoringDataCollector monitoringDataCollector;

    public MonitoringStatisticCollector(MonitoringDataCollector monitoringDataCollector) {
        this.monitoringDataCollector = monitoringDataCollector;
    }

    @Override
    public void submit(String measurePointId, int elementCount, long elapsedTime, TimeUnit timeUnit) {
        monitoringDataCollector.submitMeasurePoint(measurePointId,elementCount,elapsedTime,timeUnit);
    }
}
