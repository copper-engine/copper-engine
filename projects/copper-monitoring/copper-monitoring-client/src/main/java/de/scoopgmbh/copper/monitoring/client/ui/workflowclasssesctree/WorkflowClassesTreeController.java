/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.monitoring.client.ui.workflowclasssesctree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.util.Callback;
import javafx.util.StringConverter;

import com.google.common.base.Optional;

import de.scoopgmbh.copper.monitoring.client.form.issuereporting.IssueReporter;
import de.scoopgmbh.copper.monitoring.client.util.WorkflowVersion;

public class WorkflowClassesTreeController {
	private final TreeView<DisplayWorkflowClassesModel> treeView;
	
	public WorkflowClassesTreeController(final TreeView<DisplayWorkflowClassesModel> treeView, IssueReporter issueReporter) {
		super();
		this.treeView = treeView;
		
		treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<DisplayWorkflowClassesModel>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<DisplayWorkflowClassesModel>> observable,
					TreeItem<DisplayWorkflowClassesModel> oldValue, TreeItem<DisplayWorkflowClassesModel> newValue) {
				
				if (observable != null && newValue!=null && newValue.getValue()!=null /*&& newValue.getChildren().isEmpty()*/){
					WorkflowVersion workflowClassesModel = newValue.getValue().value;
					selectedItem.set(workflowClassesModel);
					expand(newValue);
				}
				
			}
		});
		
		final ContextMenu contextMenu = new ContextMenu();
		final MenuItem copy = new MenuItem("copy");
		copy.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
		copy.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				final Clipboard clipboard = Clipboard.getSystemClipboard();
			    final ClipboardContent content = new ClipboardContent();
			    content.putString(treeView.getSelectionModel().getSelectedItem().getValue().displayname);
			    clipboard.setContent(content);
			}
		});
		copy.disableProperty().bind(treeView.getSelectionModel().selectedItemProperty().isNull());
		contextMenu.getItems().add(copy);
		treeView.setContextMenu(contextMenu);
	}
	
	private void expand(TreeItem<DisplayWorkflowClassesModel> newValue){
		for (TreeItem<DisplayWorkflowClassesModel> item: newValue.getChildren()){
			expand(item);
		}
		newValue.setExpanded(true);
	}
	
	public static class DisplayWorkflowClassesModel implements Comparable<DisplayWorkflowClassesModel>{
		public WorkflowVersion value;
		public String displayname;
		public DisplayWorkflowClassesModel(WorkflowVersion value, String displayname) {
			super();
			this.value = value;
			this.displayname = displayname;
		}

		@Override
		public int compareTo(DisplayWorkflowClassesModel model) {
			return displayname.compareTo(model.displayname);
		}
		
		@Override
		public String toString() {
			return displayname;
		}
	}
	
	
	private Optional<TreeItem<DisplayWorkflowClassesModel>> findMajorItem(WorkflowVersion newWorkflowVersion,List<TreeItem<DisplayWorkflowClassesModel>> result){
		for (TreeItem<DisplayWorkflowClassesModel> classnameItem: result){
			if (newWorkflowVersion.alias.getValue()!=null && newWorkflowVersion.alias.getValue().equals(classnameItem.getValue().value.alias.getValue())){
				for (TreeItem<DisplayWorkflowClassesModel> majorItem: classnameItem.getChildren()){
					if (newWorkflowVersion.versionMajor.getValue().equals(majorItem.getValue().value.versionMajor.getValue())){
						return Optional.of(majorItem);
					}
				}
			}
		}
		return Optional.absent();
	}
	
	private Optional<TreeItem<DisplayWorkflowClassesModel>> findMinorVersion(Long versionMinor, TreeItem<DisplayWorkflowClassesModel> majorItem){
		for (TreeItem<DisplayWorkflowClassesModel> item : majorItem.getChildren()){
			if (versionMinor!=null && versionMinor.equals(item.getValue().value.versionMinor.getValue())){
				return Optional.of(item);
			}
		}
		return Optional.absent();
	}
	
	
	public String getMajorVersionText(WorkflowVersion workflowVersion){
		return "major: "+ nullFix(workflowVersion.versionMajor.getValue());
	}
	
	public String getMinorVersionText(WorkflowVersion workflowVersion){
		return "minor: "+nullFix(workflowVersion.versionMinor.getValue());
	}
	
	public String getPatchVersionText(WorkflowVersion workflowVersion){
		return  nullFix(workflowVersion.versionMajor.get())+"."+
				nullFix(workflowVersion.versionMinor.get())+"."+
				nullFix(workflowVersion.patchlevel.get())+ ": "+
				"class: "+nullFix(workflowVersion.classname.get());
	}
	
	public String nullFix(Object object){
		return object==null ? "?" : object.toString();
	}
	
	private String getAlias(WorkflowVersion workflowVersion) {
		String alias = workflowVersion.alias.get();
		if(alias == null) {
			alias = workflowVersion.classname.get();
			if(alias == null) {
				alias = "?";
			} else {
				alias = alias.substring(alias.lastIndexOf('.') + 1);
			}
		}
		return alias;
	}

	public List<TreeItem<DisplayWorkflowClassesModel>> groupToTreeItem(List<WorkflowVersion> list){
		//from flat List: alias , majorversion, minorversion
		//totree: 
		//alias 
		//	->majorversion
		//		->minorversion		
		//			->patchlevel + classname	
		
		List<TreeItem<DisplayWorkflowClassesModel>> result = new ArrayList<TreeItem<DisplayWorkflowClassesModel>>();
		for (WorkflowVersion newWorkflowVersion: list){
			Optional<TreeItem<DisplayWorkflowClassesModel>> existingMajorItem=findMajorItem(newWorkflowVersion,result);
			
			TreeItem<DisplayWorkflowClassesModel> majorVersionItemToAdd;
			if (existingMajorItem.isPresent()){
				majorVersionItemToAdd = existingMajorItem.get();
			} else {
				TreeItem<DisplayWorkflowClassesModel> classnameItemToAdd;
				classnameItemToAdd = new TreeItem<DisplayWorkflowClassesModel>(new DisplayWorkflowClassesModel(newWorkflowVersion, getAlias(newWorkflowVersion)));
				result.add(classnameItemToAdd);
				majorVersionItemToAdd = new TreeItem<DisplayWorkflowClassesModel>(new DisplayWorkflowClassesModel(newWorkflowVersion, getMajorVersionText(newWorkflowVersion)));
				classnameItemToAdd.getChildren().add(majorVersionItemToAdd);
			}
			
			Optional<TreeItem<DisplayWorkflowClassesModel>> exintingMinorItem = findMinorVersion(newWorkflowVersion.versionMinor.get(),majorVersionItemToAdd);
			TreeItem<DisplayWorkflowClassesModel> minorItem = new TreeItem<DisplayWorkflowClassesModel>(new DisplayWorkflowClassesModel(newWorkflowVersion, getMinorVersionText(newWorkflowVersion) ));
			if (exintingMinorItem.isPresent()){
				minorItem=exintingMinorItem.get();
			} else {
				majorVersionItemToAdd.getChildren().add(minorItem);
			}
			minorItem.getChildren().add(new TreeItem<DisplayWorkflowClassesModel>(new DisplayWorkflowClassesModel(newWorkflowVersion, getPatchVersionText(newWorkflowVersion) )));

			
		}
		Collections.sort(result, new Comparator<TreeItem<DisplayWorkflowClassesModel>>() {
			@Override
			public int compare(TreeItem<DisplayWorkflowClassesModel> item1, TreeItem<DisplayWorkflowClassesModel> item2) {
				return item1.getValue().compareTo(item2.getValue());
			}
		});
		return result;
	}

	public void refresh(List<WorkflowVersion> newItems) {
        treeView.setShowRoot(false);
		TreeItem<DisplayWorkflowClassesModel> rootItem = new TreeItem<DisplayWorkflowClassesModel>();
		rootItem.getChildren().addAll(groupToTreeItem(newItems));
		treeView.setRoot(rootItem);

		treeView.setCellFactory(new Callback<TreeView<DisplayWorkflowClassesModel>, TreeCell<DisplayWorkflowClassesModel>>() {
		    @Override
		    public TreeCell<DisplayWorkflowClassesModel> call(TreeView<DisplayWorkflowClassesModel> listView) {
		        return new TextFieldTreeCell<DisplayWorkflowClassesModel>(new StringConverter<DisplayWorkflowClassesModel>() {
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
	}
	
	public SimpleObjectProperty<WorkflowVersion> selectedItem = new SimpleObjectProperty<WorkflowVersion>();
	
}
