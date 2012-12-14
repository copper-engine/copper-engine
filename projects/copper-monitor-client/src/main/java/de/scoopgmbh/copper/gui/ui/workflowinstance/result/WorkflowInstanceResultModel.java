package de.scoopgmbh.copper.gui.ui.workflowinstance.result;

import java.util.Date;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;

public class WorkflowInstanceResultModel {
	public SimpleStringProperty id;
	public SimpleObjectProperty<WorkflowInstanceState> state;
	public SimpleIntegerProperty priority;
	public SimpleStringProperty processorPoolId;
	public SimpleObjectProperty<Date> timeout;

	public WorkflowInstanceResultModel(WorkflowInstanceInfo workflowInstanceInfo) {
		this.id = new SimpleStringProperty(workflowInstanceInfo.getId());
		this.state = new SimpleObjectProperty<>(workflowInstanceInfo.getState());
		this.priority = new SimpleIntegerProperty(workflowInstanceInfo.getPriority());
		this.processorPoolId = new SimpleStringProperty(workflowInstanceInfo.getProcessorPoolId());
		this.timeout = new SimpleObjectProperty<>(workflowInstanceInfo.getTimeout());
	}
	
	
	
	
}
