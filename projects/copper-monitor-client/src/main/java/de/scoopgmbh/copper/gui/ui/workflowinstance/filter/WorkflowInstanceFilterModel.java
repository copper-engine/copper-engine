package de.scoopgmbh.copper.gui.ui.workflowinstance.filter;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterModel;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;

public class WorkflowInstanceFilterModel {
	
	public SimpleObjectProperty<WorkflowInstanceState> state = new SimpleObjectProperty<>();
	public SimpleIntegerProperty priority = new SimpleIntegerProperty();
	public WorkflowSummeryFilterModel workflowSummeryFilterModel = new WorkflowSummeryFilterModel();
}
