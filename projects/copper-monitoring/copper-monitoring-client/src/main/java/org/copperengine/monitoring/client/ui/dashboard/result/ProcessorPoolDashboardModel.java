package org.copperengine.monitoring.client.ui.dashboard.result;

import org.copperengine.monitoring.core.model.ProcessorPoolInfo.ProcessorPoolTyp;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class ProcessorPoolDashboardModel {
    private final SimpleStringProperty poolName;
    private final SimpleIntegerProperty queueSize;
    private final SimpleIntegerProperty threadCount;
    private final SimpleObjectProperty<ProcessorPoolTyp> poolType;
    
    public ProcessorPoolDashboardModel(String poolName, int queueSize, int threadCount, ProcessorPoolTyp poolType) {
        this.poolName = new SimpleStringProperty(poolName);
        this.queueSize = new SimpleIntegerProperty(queueSize);
        this.threadCount = new SimpleIntegerProperty(threadCount);
        this.poolType = new SimpleObjectProperty<>(poolType);
    }

    public String getPoolName() {
        return poolName.get();
    }

    public int getQueueSize() {
        return queueSize.get();
    }

    public int getThreadCount() {
        return threadCount.get();
    }

    public ProcessorPoolTyp getPoolType() {
        return poolType.get();
    }
}
