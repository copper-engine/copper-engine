package org.copperengine.monitoring.server.statisticcollector;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.monitoring.RuntimeStatisticsCollector;

public class MultipleStatistikCollector implements RuntimeStatisticsCollector {

    CopyOnWriteArrayList<RuntimeStatisticsCollector> runtimeStatisticsCollectors = new CopyOnWriteArrayList<RuntimeStatisticsCollector>();

    public MultipleStatistikCollector(List<RuntimeStatisticsCollector> runtimeStatisticsCollectors) {
        this.runtimeStatisticsCollectors.addAll(runtimeStatisticsCollectors);
    }

    public MultipleStatistikCollector(RuntimeStatisticsCollector... runtimeStatisticsCollectors) {
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
