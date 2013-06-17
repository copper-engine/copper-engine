package de.scoopgmbh.copper.monitoring.client.ui.repository.result;

import javafx.scene.control.TreeView;
import de.scoopgmbh.copper.monitoring.client.ui.workflowclasssesctree.WorkflowClassesTreeController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowclasssesctree.WorkflowClassesTreeController.DisplayWorkflowClassesModel;

public interface WorkflowRepositoryDependencyFactory {
	public WorkflowClassesTreeController createWorkflowClassesTreeController(TreeView<DisplayWorkflowClassesModel> workflowView);
}
