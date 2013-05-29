package de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.result;

import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import de.scoopgmbh.copper.monitoring.client.form.FxmlForm;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultModel;
import de.scoopgmbh.copper.monitoring.client.util.ComponentUtil;

public final class DetailLoadService extends Service<Void> {
	private WorkflowInstanceResultModel workflowInstanceResultModel;
	private StackPane stackDetailPane;
	private FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel>> detailForm;
	private final WorkflowInstanceFilterModel usedFilter;
	
	public DetailLoadService(WorkflowInstanceFilterModel usedFilter, WorkflowInstanceResultModel workflowInstanceResultModel,StackPane stackDetailPane, FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel>> detailForm) {
		this.workflowInstanceResultModel = workflowInstanceResultModel;
		this.stackDetailPane = stackDetailPane;
		this.detailForm = detailForm;
		this.usedFilter = usedFilter;
	}
	
	public WorkflowInstanceResultModel getWorkflowInstanceResultModel() {
		return workflowInstanceResultModel;
	}

	public void setWorkflowInstanceResultModel(WorkflowInstanceResultModel workflowInstanceResultModel) {
		this.workflowInstanceResultModel = workflowInstanceResultModel;
	}

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			final ProgressIndicator indicator = ComponentUtil.createProgressIndicator();
			private WorkflowInstanceDetailFilterModel filter;
			private List<WorkflowInstanceDetailResultModel> result;
			@Override
			protected Void call() throws Exception {
				Platform.runLater(new Runnable() {
	                 @Override public void run() {
	                	 stackDetailPane.getChildren().add(indicator);
	                 }
	             });
				filter = new WorkflowInstanceDetailFilterModel(workflowInstanceResultModel.id.getValue(),usedFilter.enginePoolModel.selectedEngine.get());
				filter.workflowInstanceId.setValue(workflowInstanceResultModel.id.getValue());
				result = detailForm.getController().applyFilterInBackgroundThread(filter);
				return null;
			}
			
			@Override 
			protected void succeeded() {
				detailForm.getController().showFilteredResult(result, filter);
				stackDetailPane.getChildren().remove(indicator);
				if (getException()!=null){
					throw new RuntimeException(this.getException());
				}
				super.succeeded();
			}
			
			@Override 
			protected void failed() {
				stackDetailPane.getChildren().remove(indicator);
				if (getException()!=null){
					throw new RuntimeException(this.getException());
				}
				super.failed();
			}
			
		};
	}
}