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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.ShowFormStrategy;
import de.scoopgmbh.copper.gui.ui.workflowclasssesctree.WorkflowClassesTreeController.DisplayWorkflowClassesModel;
import de.scoopgmbh.copper.gui.ui.workflowsummary.filter.WorkflowSummaryFilterController;
import de.scoopgmbh.copper.gui.util.WorkflowVersion;

public class WorkflowClassesTreeForm extends Form<WorkflowClassesTreeController>{

	private WorkflowSummaryFilterController filterController;
	private final TreeView<DisplayWorkflowClassesModel> workflowView;
	private final GuiCopperDataProvider copperDataProvider;
	
	public WorkflowClassesTreeForm(String dynamicTitle, ShowFormStrategy<?> showFormStrategie,
			WorkflowClassesTreeController controller,WorkflowSummaryFilterController filterController,
			TreeView<DisplayWorkflowClassesModel> workflowView, GuiCopperDataProvider copperDataProvider) {
		super(dynamicTitle, showFormStrategie, controller);
		this.filterController = filterController;
		this.workflowView = workflowView;
		this.copperDataProvider = copperDataProvider;
	}

	@Override
	public Node createContent() {
		BorderPane pane = new BorderPane();
		workflowView.setPrefWidth(600);
		pane.setCenter(workflowView);
		Button refreshButton  = new Button("Refresh");
		BorderPane.setMargin(refreshButton, new Insets(5));
		pane.setBottom(refreshButton);
		controller.refresh(copperDataProvider.getWorkflowClassesList(filterController.getFilter().engine.get().getId()));
		
		controller.selectedItem.addListener(new ChangeListener<WorkflowVersion>() {
			@Override
			public void changed(ObservableValue<? extends WorkflowVersion> observable, WorkflowVersion oldValue, WorkflowVersion newValue) {
				if (newValue!=null){
					filterController.setFilter(newValue);
					filterController.startValueSetAnimation();
				}
			}
		});
		
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				controller.refresh(copperDataProvider.getWorkflowClassesList(filterController.getFilter().engine.get().getId()));
			}
		});
		return pane;
	}

}
