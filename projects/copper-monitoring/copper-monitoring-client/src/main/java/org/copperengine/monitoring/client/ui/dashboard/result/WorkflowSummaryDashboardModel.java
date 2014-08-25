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
package org.copperengine.monitoring.client.ui.dashboard.result;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.copperengine.monitoring.core.model.WorkflowInstanceState;

public class WorkflowSummaryDashboardModel {
    private final SimpleObjectProperty<WorkflowInstanceState> state;
    private final SimpleIntegerProperty count;
    
    public WorkflowSummaryDashboardModel(WorkflowInstanceState state, int count) {
        this.state = new SimpleObjectProperty<WorkflowInstanceState>(state);
        this.count = new SimpleIntegerProperty(count);
    }

    public WorkflowInstanceState getState() {
        return state.get();
    }

    public int getCount() {
        return count.get();
    }
    
    
}
