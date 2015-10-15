/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.ui.workflowsummary.result;

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
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.filter.FilterAbleForm;
import org.copperengine.monitoring.client.form.filter.FilterResultControllerBase;
import org.copperengine.monitoring.client.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import org.copperengine.monitoring.client.ui.workflowinstance.result.WorkflowInstanceResultModel;
import org.copperengine.monitoring.client.ui.workflowsummary.filter.WorkflowSummaryFilterModel;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;
import org.copperengine.monitoring.core.model.WorkflowInstanceState;

public class WorkflowSummaryResultController extends FilterResultControllerBase<WorkflowSummaryFilterModel, WorkflowSummaryResultModel> implements
        Initializable {
    private final GuiCopperDataProvider copperDataProvider;

    private final WorkflowSummaryDependencyFactory workflowSummaryDependencyFactory;
    private final MenuItem[] detailMenuItems;

    public WorkflowSummaryResultController(GuiCopperDataProvider copperDataProvider,
            WorkflowSummaryDependencyFactory workflowSummaryDependencyFactory,
            MenuItem... detailMenuItems) {
        super();
        this.copperDataProvider = copperDataProvider;
        this.workflowSummaryDependencyFactory = workflowSummaryDependencyFactory;
        this.detailMenuItems = (detailMenuItems.length != 0) ?
                detailMenuItems : new MenuItem[] { getDefaultMenuItem() };
    }

    @FXML
    // fx:id="countColumn"
    private TableColumn<WorkflowSummaryResultModel, String> countColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="resultTable"
    private TableView<WorkflowSummaryResultModel> resultTable; // Value injected by FXMLLoader

    @FXML
    // fx:id="workflowClass"
    private TableColumn<WorkflowSummaryResultModel, String> workflowClassColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="borderPane"
    private BorderPane borderPane; // Value injected by FXMLLoader

    private MenuItem getDefaultMenuItem() {
        MenuItem detailMenuItem = new MenuItem("Instancelist (db)");
        detailMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openWorkflowInstance();
            }
        });
        return detailMenuItem;
    }

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert countColumn != null : "fx:id=\"countColumn\" was not injected: check your FXML file 'WorkflowSummaryResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'WorkflowSummaryResult.fxml'.";
        assert workflowClassColumn != null : "fx:id=\"workflowClassColumn\" was not injected: check your FXML file 'WorkflowSummaryResult.fxml'.";
        assert borderPane != null;

        borderPane.setBottom(createTableControls(resultTable));

        workflowClassColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummaryResultModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<WorkflowSummaryResultModel, String> p) {
                return p.getValue().version.classname;
            }
        });

        countColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummaryResultModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<WorkflowSummaryResultModel, String> p) {
                return p.getValue().totalCount;
            }
        });

        countColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(2).multiply(0.075));
        workflowClassColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(2).multiply(0.525));
        double totalSpaceForStateColumns = 0.4;

        for (final WorkflowInstanceState workflowInstanceState : WorkflowInstanceState.values()) {
            TableColumn<WorkflowSummaryResultModel, String> tableColumn = new TableColumn<WorkflowSummaryResultModel, String>(workflowInstanceState.toString());
            tableColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummaryResultModel, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(
                        CellDataFeatures<WorkflowSummaryResultModel, String> p) {
                    return new SimpleStringProperty(
                            String.valueOf(p.getValue().workflowStateSummary.getNumberOfWorkflowInstancesWithState().get(workflowInstanceState)));
                }
            });
            tableColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(2).multiply(totalSpaceForStateColumns / WorkflowInstanceState.values().length));
            resultTable.getColumns().add(tableColumn);
        }

        resultTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2 && !resultTable.getSelectionModel().isEmpty()) {
                        openWorkflowInstance();
                    }
                }
            }
        });
        ContextMenu contextMenu = new ContextMenu();

        for (MenuItem menuItem : detailMenuItems) {
            menuItem.disableProperty().bind(resultTable.getSelectionModel().selectedItemProperty().isNull());
            contextMenu.getItems().add(menuItem);
        }
        resultTable.setContextMenu(contextMenu);
    }

    private void openWorkflowInstance() {
        FilterAbleForm<WorkflowInstanceFilterModel, WorkflowInstanceResultModel> workflowInstanceForm = workflowSummaryDependencyFactory.createWorkflowInstanceListForm();
        workflowInstanceForm.getFilter().version.setAllFrom(getSelectedEntry().version);
        workflowInstanceForm.getFilter().selectedEngine.setValue(lastFilteredWithProcessingEngineInfo);
        workflowInstanceForm.show();
    }

    private WorkflowSummaryResultModel getSelectedEntry() {
        return resultTable.getSelectionModel().getSelectedItem();
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("WorkflowSummaryResult.fxml");
    }

    private ProcessingEngineInfo lastFilteredWithProcessingEngineInfo;

    @Override
    public void showFilteredResult(List<WorkflowSummaryResultModel> filteredResult, WorkflowSummaryFilterModel usedFilter) {
        lastFilteredWithProcessingEngineInfo = usedFilter.selectedEngine.getValue();
        ObservableList<WorkflowSummaryResultModel> content = FXCollections.observableList(new ArrayList<WorkflowSummaryResultModel>());
        content.addAll(filteredResult);
        setOriginalItems(resultTable, content);
    }

    @Override
    public List<WorkflowSummaryResultModel> applyFilterInBackgroundThread(WorkflowSummaryFilterModel filter) {
        return copperDataProvider.getWorkflowSummary(filter);
    }

    @Override
    public boolean supportsClear() {
        return true;
    }

    @Override
    public void clear() {
        resultTable.getItems().clear();
    }

}
