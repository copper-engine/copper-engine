package de.scoopgmbh.copper.gui.ui.workflowclasssesctree;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterController;

public class WorkflowClassesTreeController {
	private final GuiCopperDataProvider copperDataProvider;
	private final WorkflowSummeryFilterController filterController;
	
	public WorkflowClassesTreeController(GuiCopperDataProvider copperDataProvider, WorkflowSummeryFilterController filterController) {
		super();
		this.copperDataProvider = copperDataProvider;
		this.filterController= filterController;
	}

	private void refresh(final ListView<WorkflowClassesModel> listView) {
		ObservableList<WorkflowClassesModel> content = FXCollections.observableList(new ArrayList<WorkflowClassesModel>());;
		content.addAll(copperDataProvider.getWorkflowClassesList());
		listView.setItems(content);
	}
	
	public void initialize(final Button refreshButton, final ListView<WorkflowClassesModel> listView) {
		refresh(listView);
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				refresh(listView);
			}
		});
		
		listView.setCellFactory(TextFieldListCell.forListView(new StringConverter<WorkflowClassesModel>() {
			@Override
			public WorkflowClassesModel fromString(String value) {
				return null;
			}
			@Override
			public String toString(WorkflowClassesModel value) {
				return value.classname.getValue()+","+value.versionMajor.getValue()+","+value.versionMinor.getValue();
			}
		})); 
		
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent event) {
	            WorkflowClassesModel workflowClassesModel = listView.getSelectionModel().getSelectedItem();
	            filterController.setFilter(workflowClassesModel.classname.getValue(), 
	            		workflowClassesModel.versionMajor.getValue(),
	            		workflowClassesModel.versionMinor.getValue());
	        }
	    });
		
	}
}
