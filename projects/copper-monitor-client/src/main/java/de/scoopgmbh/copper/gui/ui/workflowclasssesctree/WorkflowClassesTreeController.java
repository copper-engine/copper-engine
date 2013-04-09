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

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.ui.workflowsummary.filter.WorkflowSummeryFilterController;

public class WorkflowClassesTreeController {
	private final GuiCopperDataProvider copperDataProvider;
	private final WorkflowSummeryFilterController filterController;
	
	public WorkflowClassesTreeController(GuiCopperDataProvider copperDataProvider, WorkflowSummeryFilterController filterController) {
		super();
		this.copperDataProvider = copperDataProvider;
		this.filterController= filterController;
	}
	
	public static class DisplayWorkflowClassesModel{
		public WorkflowClassesModel value;
		public String displayname;
		public DisplayWorkflowClassesModel(WorkflowClassesModel value, String displayname) {
			super();
			this.value = value;
			this.displayname = displayname;
		}
	}
	
	public List<TreeItem<DisplayWorkflowClassesModel>> groupToTreeItem(List<WorkflowClassesModel> list){
		//from flat List: classname, majorversion, minorversion
		//totree: 
		//classname 
		//	->majorversion
		//		->minorversion		
		
		List<TreeItem<DisplayWorkflowClassesModel>> result = new ArrayList<>();
		for (WorkflowClassesModel newWorkflowVersion: list){
			TreeItem<DisplayWorkflowClassesModel> majorVersionItemToAdd = null;
			TreeItem<DisplayWorkflowClassesModel> classnameItemToAdd = null;
			for (TreeItem<DisplayWorkflowClassesModel> classnameItem: result){
				if (newWorkflowVersion.classname.getValue().equals(classnameItem.getValue().value.classname.getValue())){
					classnameItemToAdd=classnameItem;
					for (TreeItem<DisplayWorkflowClassesModel> majorItem: classnameItem.getChildren()){
						if (newWorkflowVersion.versionMajor.getValue().equals(majorItem.getValue().value.versionMajor.getValue())){
							majorVersionItemToAdd=majorItem;
							break;
						}
					}
				}
			}
			
			if (classnameItemToAdd==null){
				classnameItemToAdd = new TreeItem<>(new DisplayWorkflowClassesModel(newWorkflowVersion, newWorkflowVersion.classname.getValue()));
				result.add(classnameItemToAdd);
			}
			
			if (majorVersionItemToAdd==null){
				TreeItem<DisplayWorkflowClassesModel> newitemMajor =new TreeItem<>(new DisplayWorkflowClassesModel(newWorkflowVersion, newWorkflowVersion.versionMajor.getValue().toString()));
				classnameItemToAdd.getChildren().add(newitemMajor);
				majorVersionItemToAdd=newitemMajor;
			}
			majorVersionItemToAdd.getChildren().add(new TreeItem<>(new DisplayWorkflowClassesModel(newWorkflowVersion, newWorkflowVersion.versionMinor.getValue().toString())));
		}
		
		return result;
	}

	private void refresh(final TreeView<DisplayWorkflowClassesModel> treeView) {
		List<WorkflowClassesModel> allItems = copperDataProvider.getWorkflowClassesList();
		
		TreeItem<DisplayWorkflowClassesModel> rootItem = new TreeItem<>();
		rootItem.getChildren().addAll(groupToTreeItem(allItems));
		treeView.setRoot(rootItem);

		treeView.setCellFactory(new Callback<TreeView<DisplayWorkflowClassesModel>, TreeCell<DisplayWorkflowClassesModel>>() {
		    @Override
		    public TreeCell<DisplayWorkflowClassesModel> call(TreeView<DisplayWorkflowClassesModel> listView) {
		        return new TextFieldTreeCell<>(new StringConverter<DisplayWorkflowClassesModel>() {
					@Override
					public DisplayWorkflowClassesModel fromString(String string) {
						return null;
					}
					@Override
					public String toString(DisplayWorkflowClassesModel object) {
						return object.displayname;
					}
				});
		    }
		});
		
        rootItem.setExpanded(true);
        treeView.setShowRoot(false);
	}
	
	public void initialize(final Button refreshButton, final TreeView<DisplayWorkflowClassesModel> treeView) {
		refresh(treeView);
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				refresh(treeView);
			}
		});
		
		treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<DisplayWorkflowClassesModel>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<DisplayWorkflowClassesModel>> observable,
					TreeItem<DisplayWorkflowClassesModel> oldValue, TreeItem<DisplayWorkflowClassesModel> newValue) {
				if (newValue!=null && newValue.getValue()!=null && newValue.getChildren().isEmpty()){
					WorkflowClassesModel workflowClassesModel = newValue.getValue().value;
					filterController.setFilter(workflowClassesModel);
					filterController.startValueSetAnimation();
				}
			}
		});
		
	}
}
