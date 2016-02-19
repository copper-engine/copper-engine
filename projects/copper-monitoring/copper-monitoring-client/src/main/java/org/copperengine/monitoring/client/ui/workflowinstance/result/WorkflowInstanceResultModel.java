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
package org.copperengine.monitoring.client.ui.workflowinstance.result;

import java.util.Date;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import org.copperengine.monitoring.core.model.WorkflowInstanceInfo;
import org.copperengine.monitoring.core.model.WorkflowInstanceState;

public class WorkflowInstanceResultModel {
    public final SimpleStringProperty id;
    public final SimpleObjectProperty<WorkflowInstanceState> state;
    public final SimpleIntegerProperty priority;
    public final SimpleStringProperty processorPoolId;
    public final SimpleObjectProperty<Date> timeout;

    public final SimpleObjectProperty<Date> lastActivityTimestamp;
    public final SimpleLongProperty overallLifetimeInMs;
    public final SimpleObjectProperty<Date> startTime;
    public final SimpleObjectProperty<Date> finishTime;
    public final SimpleObjectProperty<Date> lastErrorTime;
    public final SimpleStringProperty errorInfos;

    public WorkflowInstanceResultModel(WorkflowInstanceInfo workflowInstanceInfo) {
        this.id = new SimpleStringProperty(workflowInstanceInfo.getId());
        this.state = new SimpleObjectProperty<WorkflowInstanceState>(workflowInstanceInfo.getState());
        this.priority = new SimpleIntegerProperty(workflowInstanceInfo.getPriority());
        this.processorPoolId = new SimpleStringProperty(workflowInstanceInfo.getProcessorPoolId());
        this.timeout = new SimpleObjectProperty<Date>(workflowInstanceInfo.getTimeout());

        this.lastActivityTimestamp = new SimpleObjectProperty<Date>(workflowInstanceInfo.getLastActivityTimestamp());
        this.overallLifetimeInMs = new SimpleLongProperty(workflowInstanceInfo.getOverallLifetimeInMs());
        this.startTime = new SimpleObjectProperty<Date>(workflowInstanceInfo.getStartTime());
        this.finishTime = new SimpleObjectProperty<Date>(workflowInstanceInfo.getFinishTime());
        this.lastErrorTime = new SimpleObjectProperty<Date>(workflowInstanceInfo.getLastErrorTime());
        this.errorInfos = new SimpleStringProperty(workflowInstanceInfo.getErrorInfos());
    }

}
