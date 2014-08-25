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
