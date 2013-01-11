/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
