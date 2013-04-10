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
package de.scoopgmbh.copper.gui.ui.workflowhistory.result;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javafx.util.converter.DateStringConverter;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.workflowhistory.filter.WorkflowHistoryFilterModel;
import de.scoopgmbh.copper.gui.util.ConvertingStringProperty;

public class WorkflowHistoryResultController implements Initializable, FilterResultController<WorkflowHistoryFilterModel,WorkflowHistoryResultModel>, FxmlController {
	
	
	
	private final GuiCopperDataProvider copperDataProvider;

	
	public WorkflowHistoryResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}

    @FXML //  fx:id="classnameColumn"
    private TableColumn<WorkflowHistoryResultModel, String> classnameColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="idColumn"
    private TableColumn<WorkflowHistoryResultModel, String> idColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="resultTable"
    private TableView<WorkflowHistoryResultModel> resultTable; // Value injected by FXMLLoader

    @FXML //  fx:id="stateColumn"
    private TableColumn<WorkflowHistoryResultModel, String> stateColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="timeColumn"
    private TableColumn<WorkflowHistoryResultModel, String> timeColumn; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert classnameColumn != null : "fx:id=\"classnameColumn\" was not injected: check your FXML file 'WorkflowHistoryResult.fxml'.";
        assert idColumn != null : "fx:id=\"idColumn\" was not injected: check your FXML file 'WorkflowHistoryResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'WorkflowHistoryResult.fxml'.";
        assert stateColumn != null : "fx:id=\"stateColumn\" was not injected: check your FXML file 'WorkflowHistoryResult.fxml'.";
        assert timeColumn != null : "fx:id=\"timeColumn\" was not injected: check your FXML file 'WorkflowHistoryResult.fxml'.";

        
        timeColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowHistoryResultModel, String>, ObservableValue<String>>() {
     			@SuppressWarnings({ "rawtypes", "unchecked" })
				@Override
     			public ObservableValue<String> call(
     					CellDataFeatures<WorkflowHistoryResultModel, String> p) {
     				return new ConvertingStringProperty(p.getValue().timestamp,new DateStringConverter("dd.MM.yyyy HH:mm:ss:SSS"));
     			}
     		});
        idColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowHistoryResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowHistoryResultModel, String> p) {
				return p.getValue().instanceId;
			}
		});
        
        stateColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowHistoryResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowHistoryResultModel, String> p) {
				return p.getValue().stateName;
			}
		});
        
        classnameColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowHistoryResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowHistoryResultModel, String> p) {
				return p.getValue().classname;
			}
		});

//        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        timeColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(2).multiply(0.15));
        idColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(2).multiply(0.35));
        stateColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(2).multiply(0.15));
        classnameColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(2).multiply(0.35)); 
    }
    
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowHistoryResult.fxml");
	}

	@Override
	public void showFilteredResult(List<WorkflowHistoryResultModel> filteredResult, WorkflowHistoryFilterModel usedFilter) {
		ObservableList<WorkflowHistoryResultModel> content = FXCollections.observableList(new ArrayList<WorkflowHistoryResultModel>());;
		content.addAll(filteredResult);
		resultTable.setItems(content);
	}

	@Override
	public List<WorkflowHistoryResultModel> applyFilterInBackgroundThread(WorkflowHistoryFilterModel filter) {
		return copperDataProvider.getWorkflowInstanceHistory(filter);  
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
