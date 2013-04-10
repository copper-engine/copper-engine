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

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import de.scoopgmbh.copper.gui.util.WorkflowVersion;

public class WorkflowClassesTreeController {
	private final TreeView<DisplayWorkflowClassesModel> treeView;
	
	public WorkflowClassesTreeController(TreeView<DisplayWorkflowClassesModel> treeView) {
		super();
		this.treeView = treeView;
		
		treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<DisplayWorkflowClassesModel>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<DisplayWorkflowClassesModel>> observable,
					TreeItem<DisplayWorkflowClassesModel> oldValue, TreeItem<DisplayWorkflowClassesModel> newValue) {
				
				if (newValue!=null && newValue.getValue()!=null && newValue.getChildren().isEmpty()){
					WorkflowVersion workflowClassesModel = newValue.getValue().value;
					selectedItem.set(workflowClassesModel);
				}
			}
		});
	}
	
	public static class DisplayWorkflowClassesModel{
		public WorkflowVersion value;
		public String displayname;
		public DisplayWorkflowClassesModel(WorkflowVersion value, String displayname) {
			super();
			this.value = value;
			this.displayname = displayname;
		}
	}
	
	public List<TreeItem<DisplayWorkflowClassesModel>> groupToTreeItem(List<WorkflowVersion> list){
		//from flat List: classname, majorversion, minorversion
		//totree: 
		//classname 
		//	->majorversion
		//		->minorversion		
		
		List<TreeItem<DisplayWorkflowClassesModel>> result = new ArrayList<>();
		for (WorkflowVersion newWorkflowVersion: list){
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
				classnameItemToAdd = new TreeItem<>(new DisplayWorkflowClassesModel(newWorkflowVersion, newWorkflowVersion.classname.get()));
				result.add(classnameItemToAdd);
			}
			
			if (majorVersionItemToAdd==null){
				TreeItem<DisplayWorkflowClassesModel> newitemMajor =new TreeItem<>(new DisplayWorkflowClassesModel(newWorkflowVersion, "Major: "+newWorkflowVersion.versionMajor.getValue().toString()));
				classnameItemToAdd.getChildren().add(newitemMajor);
				majorVersionItemToAdd=newitemMajor;
			}
			majorVersionItemToAdd.getChildren().add(new TreeItem<>(new DisplayWorkflowClassesModel(newWorkflowVersion, "Minor: "+newWorkflowVersion.versionMinor.getValue().toString()+"\nAlias: "+newWorkflowVersion.alias.get() )));
		}
		
		return result;
	}

	public void refresh(List<WorkflowVersion> newItems) {
		TreeItem<DisplayWorkflowClassesModel> rootItem = new TreeItem<>();
		rootItem.getChildren().addAll(groupToTreeItem(newItems));
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
	
	public SimpleObjectProperty<WorkflowVersion> selectedItem = new SimpleObjectProperty<>();
	
}
