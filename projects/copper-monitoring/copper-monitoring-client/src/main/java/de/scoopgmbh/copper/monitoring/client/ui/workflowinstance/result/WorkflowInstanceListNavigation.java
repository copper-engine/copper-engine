package de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.result;

import javafx.scene.layout.BorderPane;
import de.scoopgmbh.copper.monitoring.client.form.FxmlForm;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultController;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultModel;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;

public interface WorkflowInstanceListNavigation {
	public void navigateToAudittrail(String workflowInstanceId);
	public void navigateToIntsanceDetail(String workflowInstanceId, ProcessingEngineInfo engine);
	public FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>> createWorkflowinstanceDetailResultForm(
			BorderPane detailPane);
}
