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
package de.scoopgmbh.copper.gui.ui.workflowsummery.result;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.context.FormContext;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowinstance.result.WorkflowInstanceResultModel;
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterModel;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;

public class WorkflowSummeryResultController implements Initializable, FilterResultController<WorkflowSummeryFilterModel,WorkflowSummeryResultModel>, FxmlController {
	GuiCopperDataProvider copperDataProvider;

	private FormContext formcontext;
	public WorkflowSummeryResultController(GuiCopperDataProvider copperDataProvider,FormContext formcontext) {
		super();
		this.copperDataProvider = copperDataProvider;
		this.formcontext = formcontext;
	}

    @FXML //  fx:id="aliasColumn"
    private TableColumn<WorkflowSummeryResultModel, String> aliasColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="countColumn"
    private TableColumn<WorkflowSummeryResultModel, String> countColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="majorVersionColumn"
    private TableColumn<WorkflowSummeryResultModel, String> versionColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="resultTable"
    private TableView<WorkflowSummeryResultModel> resultTable; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowClass"
    private TableColumn<WorkflowSummeryResultModel, String> workflowClassColumn; // Value injected by FXMLLoader
    
    


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert aliasColumn != null : "fx:id=\"aliasColumn\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";
        assert countColumn != null : "fx:id=\"countColumn\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";
        assert versionColumn != null : "fx:id=\"versionColumn\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";
        assert workflowClassColumn != null : "fx:id=\"workflowClassColumn\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";

        workflowClassColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummeryResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowSummeryResultModel, String> p) {
				return p.getValue().version.classname;
			}
		});
        
        versionColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummeryResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					final CellDataFeatures<WorkflowSummeryResultModel, String> p) {
				return new SimpleStringProperty(
						p.getValue().version.versionMajor.getValue()+"."+
						p.getValue().version.versionMinor.getValue()+"."+
						p.getValue().version.patchlevel.getValue()); 
			}
		});
        
        aliasColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummeryResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowSummeryResultModel, String> p) {
				return p.getValue().version.alias;
			}
		});
        
        countColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummeryResultModel, String>, ObservableValue<String>>() {
        	@Override
			public ObservableValue<String> call(
        			CellDataFeatures<WorkflowSummeryResultModel, String> p) {
        		return p.getValue().totalcount;
        	}
        });
        
        
        for (final WorkflowInstanceState workflowInstanceState: WorkflowInstanceState.values()){
        	TableColumn<WorkflowSummeryResultModel, String> tableColumn = new TableColumn<>(workflowInstanceState.toString());
        	tableColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummeryResultModel, String>, ObservableValue<String>>() {
            	@Override
				public ObservableValue<String> call(
            			CellDataFeatures<WorkflowSummeryResultModel, String> p) {
            		return new SimpleStringProperty(
            				String.valueOf(p.getValue().workflowStateSummery.getNumberOfWorkflowInstancesWithState().get(workflowInstanceState)));
            	}
            });
			resultTable.getColumns().add(tableColumn);
        }

        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        resultTable.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
		            if(mouseEvent.getClickCount() == 2 && !resultTable.getSelectionModel().isEmpty()){
		            	openWorkflowInstance();
		            }
		        }
			}
		});
        ContextMenu contextMenu = new ContextMenu();
        MenuItem detailMenuItem = new MenuItem("Details");
        detailMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				openWorkflowInstance();
			}
		});
        detailMenuItem.disableProperty().bind(resultTable.getSelectionModel().selectedItemProperty().isNull());
        contextMenu.getItems().add(detailMenuItem);
        
        resultTable.setContextMenu(contextMenu);
    }
    
	private void openWorkflowInstance() {
		FilterAbleForm<WorkflowInstanceFilterModel,WorkflowInstanceResultModel> workflowInstanceForm = formcontext.createWorkflowInstanceForm();
		workflowInstanceForm.getFilter().version.setAllFrom(getSelectedEntry().version);
		workflowInstanceForm.getFilter().engine.setValue(lastFilteredWithProcessingEngineInfo);
		workflowInstanceForm.show();
	}
    
    private WorkflowSummeryResultModel getSelectedEntry(){
    	return resultTable.getSelectionModel().getSelectedItem();
    }

	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowSummeryResult.fxml");
	}

	private ProcessingEngineInfo lastFilteredWithProcessingEngineInfo;
	@Override
	public void showFilteredResult(List<WorkflowSummeryResultModel> filteredResult, WorkflowSummeryFilterModel usedFilter) {
		lastFilteredWithProcessingEngineInfo = usedFilter.engine.getValue();
		ObservableList<WorkflowSummeryResultModel> content = FXCollections.observableList(new ArrayList<WorkflowSummeryResultModel>());;
		content.addAll(filteredResult);
		resultTable.setItems(content);
	}

	@Override
	public List<WorkflowSummeryResultModel> applyFilterInBackgroundThread(WorkflowSummeryFilterModel filter) {
		return copperDataProvider.getWorkflowSummery(filter);
	}
	
	@Override
	public boolean canLimitResult() {
		return false;
	}
	
	@Override
	public void clear() {
		resultTable.getItems().clear();
	}

}
