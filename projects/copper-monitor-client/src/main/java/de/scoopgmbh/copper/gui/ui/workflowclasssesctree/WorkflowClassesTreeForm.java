package de.scoopgmbh.copper.gui.ui.workflowclasssesctree;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.NotShowFormStrategie;
import de.scoopgmbh.copper.gui.ui.workflowclasssesctree.WorkflowClassesTreeController.DisplayWorkflowClassesModel;
import de.scoopgmbh.copper.gui.util.MessageProvider;

public class WorkflowClassesTreeForm extends Form<WorkflowClassesTreeController>{

	public WorkflowClassesTreeForm(String menueItemtextKey, MessageProvider messageProvider, WorkflowClassesTreeController controller) {
		super(menueItemtextKey, messageProvider, new NotShowFormStrategie(), controller);
	}

	@Override
	public Node createContent() {
		BorderPane pane = new BorderPane();
		TreeView<DisplayWorkflowClassesModel> workflowView = new TreeView<>();
		pane.setCenter(workflowView);
		Button refreshButton  = new Button("Refresh");
		BorderPane.setMargin(refreshButton, new Insets(5));
		pane.setBottom(refreshButton);
		controller.initialize(refreshButton, workflowView);
		return pane;
	}

}
