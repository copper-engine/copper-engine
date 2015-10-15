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
        this.poolType = new SimpleObjectProperty<ProcessorPoolTyp>(poolType);
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
