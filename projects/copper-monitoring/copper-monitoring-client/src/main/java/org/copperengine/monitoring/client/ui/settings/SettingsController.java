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
package org.copperengine.monitoring.client.ui.settings;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.copperengine.monitoring.client.form.FxmlController;

public class SettingsController implements Initializable, FxmlController {
    private static final double SCALE = 0.75;

    private final SettingsModel settingsModel;

    public SettingsController(SettingsModel settingsModel) {
        this.settingsModel = settingsModel;
    }

    @FXML
    // fx:id="addButton"
    private Button addButton; // Value injected by FXMLLoader

    @FXML
    // fx:id="colorColumn"
    private TableColumn<AuditralColorMapping, Color> colorColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="colorDetail"
    private ColorPicker colorDetail; // Value injected by FXMLLoader

    @FXML
    // fx:id="colorTable"
    private TableView<AuditralColorMapping> colorTable; // Value injected by FXMLLoader

    @FXML
    // fx:id="conversationIdColumn"
    private TableColumn<AuditralColorMapping, String> conversationIdColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="conversionidDetail"
    private TextField conversionidDetail; // Value injected by FXMLLoader

    @FXML
    // fx:id="correlationIdColumn"
    private TableColumn<AuditralColorMapping, String> correlationIdColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="correlationidDetail"
    private TextField correlationidDetail; // Value injected by FXMLLoader

    @FXML
    // fx:id="idColumn"
    private TableColumn<AuditralColorMapping, String> idColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="idDetail"
    private TextField idDetail; // Value injected by FXMLLoader

    @FXML
    // fx:id="loglevelColumn"
    private TableColumn<AuditralColorMapping, String> loglevelColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="loglevelDetail"
    private TextField loglevelDetail; // Value injected by FXMLLoader

    @FXML
    // fx:id="messageTypDetail"
    private TextField messageTypDetail; // Value injected by FXMLLoader

    @FXML
    // fx:id="messageTypeColumn"
    private TableColumn<AuditralColorMapping, String> messageTypeColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="occurrenceColumn"
    private TableColumn<AuditralColorMapping, String> occurrenceColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="occurrenceDetail"
    private TextField occurrenceDetail; // Value injected by FXMLLoader

    @FXML
    // fx:id="removeButton"
    private Button removeButton; // Value injected by FXMLLoader

    @FXML
    // fx:id="transactionIdColumn"
    private TableColumn<AuditralColorMapping, String> transactionIdColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="workflowInstanceIdColumn"
    private TableColumn<AuditralColorMapping, String> workflowInstanceIdColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="workflowidDetail"
    private TextField workflowidDetail; // Value injected by FXMLLoader

    @FXML
    // fx:id="butSave"
    private Button butEditColors; // Value injected by FXMLLoader

    @FXML
    // fx:id="previewPane"
    private Pane previewPane; // Value injected by FXMLLoader
    

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert addButton != null : "fx:id=\"addButton\" was not injected: check your FXML file 'Settings.fxml'.";
        assert colorColumn != null : "fx:id=\"colorColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert colorDetail != null : "fx:id=\"colorDetail\" was not injected: check your FXML file 'Settings.fxml'.";
        assert colorTable != null : "fx:id=\"colorTable\" was not injected: check your FXML file 'Settings.fxml'.";
        assert conversationIdColumn != null : "fx:id=\"conversationIdColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert conversionidDetail != null : "fx:id=\"conversionidDetail\" was not injected: check your FXML file 'Settings.fxml'.";
        assert correlationIdColumn != null : "fx:id=\"correlationIdColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert correlationidDetail != null : "fx:id=\"correlationidDetail\" was not injected: check your FXML file 'Settings.fxml'.";
        assert idColumn != null : "fx:id=\"idColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert idDetail != null : "fx:id=\"idDetail\" was not injected: check your FXML file 'Settings.fxml'.";
        assert loglevelColumn != null : "fx:id=\"loglevelColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert loglevelDetail != null : "fx:id=\"loglevelDetail\" was not injected: check your FXML file 'Settings.fxml'.";
        assert messageTypDetail != null : "fx:id=\"messageTypDetail\" was not injected: check your FXML file 'Settings.fxml'.";
        assert messageTypeColumn != null : "fx:id=\"messageTypeColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert occurrenceColumn != null : "fx:id=\"occurrenceColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert occurrenceDetail != null : "fx:id=\"occurrenceDetail\" was not injected: check your FXML file 'Settings.fxml'.";
        assert removeButton != null : "fx:id=\"removeButton\" was not injected: check your FXML file 'Settings.fxml'.";
        assert transactionIdColumn != null : "fx:id=\"transactionIdColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert workflowInstanceIdColumn != null : "fx:id=\"workflowInstanceIdColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert workflowidDetail != null : "fx:id=\"workflowidDetail\" was not injected: check your FXML file 'Settings.fxml'.";

        assert butEditColors != null : "fx:id=\"butEditColors\" was not injected: check your FXML file 'Settings.fxml'.";
        assert previewPane != null : "fx:id=\"previewPane\" was not injected: check your FXML file 'Settings.fxml'.";

        colorColumn.setCellFactory(new Callback<TableColumn<AuditralColorMapping, Color>, TableCell<AuditralColorMapping, Color>>() {
            @Override
            public TableCell<AuditralColorMapping, Color> call(TableColumn<AuditralColorMapping, Color> param) {
                return new TextFieldTableCell<AuditralColorMapping, Color>() {
                    @Override
                    public void updateItem(Color color, boolean empty) {
                        if (color != null) {
                            this.setTextFill(color);
                        }
                        super.updateItem(color, empty);
                    }
                };
            }
        });

        loglevelColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditralColorMapping, String> p) {
                return p.getValue().loglevelRegEx;
            }
        });

        colorColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, Color>, ObservableValue<Color>>() {
            @Override
            public ObservableValue<Color> call(
                    CellDataFeatures<AuditralColorMapping, Color> p) {
                return p.getValue().color;
            }
        });

        conversationIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditralColorMapping, String> p) {
                return p.getValue().conversationIdRegEx;
            }
        });

        correlationIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditralColorMapping, String> p) {
                return p.getValue().correlationIdRegEx;
            }
        });

        idColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditralColorMapping, String> p) {
                return p.getValue().idRegEx;
            }
        });

        messageTypeColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditralColorMapping, String> p) {
                return p.getValue().messageTypeRegEx;
            }
        });

        occurrenceColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditralColorMapping, String> p) {
                return p.getValue().occurrenceRegEx;
            }
        });

        transactionIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditralColorMapping, String> p) {
                return p.getValue().transactionIdRegEx;
            }
        });

        workflowInstanceIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditralColorMapping, String> p) {
                return p.getValue().workflowInstanceIdRegEx;
            }
        });

        colorTable.setItems(settingsModel.auditralColorMappings);

        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AuditralColorMapping newItem = new AuditralColorMapping();
                newItem.color.setValue(Color.rgb(255, 128, 128));
                colorTable.getItems().add(newItem);
            }
        });

        removeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                colorTable.getItems().remove(colorTable.getSelectionModel().getSelectedIndex());
            }
        });
        removeButton.disableProperty().bind(colorTable.getSelectionModel().selectedIndexProperty().greaterThan(-1).not());

        colorTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<AuditralColorMapping>() {
            @Override
            public void changed(ObservableValue<? extends AuditralColorMapping> observable, AuditralColorMapping oldValue,
                    AuditralColorMapping newValue) {
                if (oldValue != null) {
                    colorDetail.valueProperty().unbindBidirectional(oldValue.color);
                    occurrenceDetail.textProperty().unbindBidirectional(oldValue.occurrenceRegEx);
                    loglevelDetail.textProperty().unbindBidirectional(oldValue.loglevelRegEx);
                    workflowidDetail.textProperty().unbindBidirectional(oldValue.workflowInstanceIdRegEx);
                    correlationidDetail.textProperty().unbindBidirectional(oldValue.correlationIdRegEx);
                    conversionidDetail.textProperty().unbindBidirectional(oldValue.conversationIdRegEx);
                    messageTypDetail.textProperty().unbindBidirectional(oldValue.messageTypeRegEx);
                    idDetail.textProperty().unbindBidirectional(oldValue.idRegEx);

                }
                if (newValue != null) {
                    colorDetail.valueProperty().bindBidirectional(newValue.color);
                    occurrenceDetail.textProperty().bindBidirectional(newValue.occurrenceRegEx);
                    loglevelDetail.textProperty().bindBidirectional(newValue.loglevelRegEx);
                    workflowidDetail.textProperty().bindBidirectional(newValue.workflowInstanceIdRegEx);
                    correlationidDetail.textProperty().bindBidirectional(newValue.correlationIdRegEx);
                    conversionidDetail.textProperty().bindBidirectional(newValue.conversationIdRegEx);
                    messageTypDetail.textProperty().bindBidirectional(newValue.messageTypeRegEx);
                    idDetail.textProperty().bindBidirectional(newValue.idRegEx);
                    colorDetail.fireEvent(new ActionEvent());// javafx bug workaround else color chooser wont update
                                                             // http://javafx-jira.kenai.com/browse/RT-26633
                }
            }
        });
        colorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        initPreview();
        
    }

    private void initPreview() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Preview.fxml"));
        try {
            VBox preview = (VBox)loader.load();
            double width = preview.getPrefWidth();
            double height = preview.getPrefHeight();

            previewPane.getChildren().add(preview);
            
            previewPane.setScaleX(SCALE);
            previewPane.setScaleY(SCALE);

            double translateX = -width * (1 - SCALE) / 2;
            double translateY = -height * (1 - SCALE) / 2;
            previewPane.setTranslateX(translateX);
            previewPane.setTranslateY(translateY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void openGuiColorsDialog() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GuiColors.fxml"));
        GuiColorsController guiColorsController = new GuiColorsController(previewPane.getScene(), settingsModel);
        loader.setController(guiColorsController);
        try {
            AnchorPane guiColorsPane = (AnchorPane)loader.load();
            Scene scene = new Scene(guiColorsPane);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(scene);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public URL getFxmlResource() {
        return getClass().getResource("Settings.fxml");
    }
}
