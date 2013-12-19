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
package de.scoopgmbh.copper.monitoring.client.ui.audittrail.result;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.filter.AuditTrailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.settings.AuditralColorMapping;
import de.scoopgmbh.copper.monitoring.client.ui.settings.SettingsModel;
import de.scoopgmbh.copper.monitoring.client.util.CSSHelper;
import de.scoopgmbh.copper.monitoring.client.util.CodeMirrorFormatter;
import de.scoopgmbh.copper.monitoring.client.util.CodeMirrorFormatter.CodeFormatLanguage;

public class AuditTrailResultController extends FilterResultControllerBase<AuditTrailFilterModel, AuditTrailResultModel> implements Initializable {
    private final GuiCopperDataProvider copperDataProvider;
    private final SettingsModel settingsModel;
    private final CodeMirrorFormatter codeMirrorFormatter;

    public AuditTrailResultController(GuiCopperDataProvider copperDataProvider, SettingsModel settingsModel, CodeMirrorFormatter codeMirrorFormatter) {
        super();
        this.copperDataProvider = copperDataProvider;
        this.settingsModel = settingsModel;
        this.codeMirrorFormatter = codeMirrorFormatter;
    }

    @FXML
    // fx:id="conversationIdColumn"
    private TableColumn<AuditTrailResultModel, String> conversationIdColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="correlationIdColumn"
    private TableColumn<AuditTrailResultModel, String> correlationIdColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="htmlMessageView"
    private WebView htmlMessageView; // Value injected by FXMLLoader

    @FXML
    // fx:id="idColumn"
    private TableColumn<AuditTrailResultModel, String> idColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="loglevelColumn"
    private TableColumn<AuditTrailResultModel, String> loglevelColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="messageTypeColumn"
    private TableColumn<AuditTrailResultModel, String> messageTypeColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="occurrenceColumn"
    private TableColumn<AuditTrailResultModel, String> occurrenceColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="resultTable"
    private TableView<AuditTrailResultModel> resultTable; // Value injected by FXMLLoader

    @FXML
    // fx:id="textMessageView"
    private TextArea textMessageView; // Value injected by FXMLLoader

    @FXML
    // fx:id="transactionIdColumn"
    private TableColumn<AuditTrailResultModel, String> transactionIdColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="workflowInstanceIdColumn"
    private TableColumn<AuditTrailResultModel, String> workflowInstanceIdColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="detailstackPane"
    private StackPane detailstackPane; // Value injected by FXMLLoader

    @FXML
    // fx:id="resultTextarea"
    private TextArea resultTextarea; // Value injected by FXMLLoader

    @FXML
    // fx:id="searchField"
    private TextField searchField; // Value injected by FXMLLoader

    @FXML
    // fx:id="copyButton"
    private Button copyButton; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert conversationIdColumn != null : "fx:id=\"conversationIdColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert correlationIdColumn != null : "fx:id=\"correlationIdColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert htmlMessageView != null : "fx:id=\"htmlMessageView\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert idColumn != null : "fx:id=\"idColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert loglevelColumn != null : "fx:id=\"loglevelColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert messageTypeColumn != null : "fx:id=\"messageTypeColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert occurrenceColumn != null : "fx:id=\"occurrenceColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert textMessageView != null : "fx:id=\"textMessageView\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert transactionIdColumn != null : "fx:id=\"transactionIdColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert workflowInstanceIdColumn != null : "fx:id=\"workflowInstanceIdColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert detailstackPane != null : "fx:id=\"detailstackPane\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert resultTextarea != null : "fx:id=\"resultTextarea\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert searchField != null : "fx:id=\"searchField\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert copyButton != null : "fx:id=\"copyButton\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";

        resultTable.setRowFactory(new Callback<TableView<AuditTrailResultModel>, TableRow<AuditTrailResultModel>>() {
            @Override
            public TableRow<AuditTrailResultModel> call(TableView<AuditTrailResultModel> param) {
                return new TableRow<AuditTrailResultModel>() {
                    @Override
                    protected void updateItem(AuditTrailResultModel item, boolean empty) {
                        if (item != null) {
                            for (int i = 0; i < settingsModel.auditralColorMappings.size(); i++) {
                                AuditralColorMapping auditralColorMapping = settingsModel.auditralColorMappings.get(i);
                                if (auditralColorMapping.match(item)) {
                                    this.setStyle(
                                            "-fx-control-inner-background: " + CSSHelper.toCssColor(auditralColorMapping.color.get()) + ";"
                                            );

                                } else {
                                    this.setStyle("");
                                }
                            }
                        }
                        super.updateItem(item, empty);
                    }
                };
            }
        });

        loglevelColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditTrailResultModel, String> p) {
                return p.getValue().loglevel.asString();
            }
        });

        conversationIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditTrailResultModel, String> p) {
                return p.getValue().conversationId;
            }
        });

        correlationIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditTrailResultModel, String> p) {
                return p.getValue().correlationId;
            }
        });

        idColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditTrailResultModel, String> p) {
                return p.getValue().id.asString();
            }
        });

        messageTypeColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditTrailResultModel, String> p) {
                return p.getValue().messageType;
            }
        });

        occurrenceColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditTrailResultModel, String> p) {
                return p.getValue().occurrence;
            }
        });

        transactionIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditTrailResultModel, String> p) {
                return p.getValue().transactionId;
            }
        });

        workflowInstanceIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<AuditTrailResultModel, String> p) {
                return p.getValue().workflowInstanceId;
            }
        });

        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        initTable();

        searchField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (resultTextarea.getText() != null && searchField.getText() != null) {
                    int from = resultTextarea.getText().indexOf(searchField.getText(), resultTextarea.getSelection().getEnd());
                    resultTextarea.selectRange(from, from + searchField.getText().length());

                }
            }
        });

        copyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(resultTextarea.getText());
                clipboard.setContent(content);
            }
        });
    }

    private void initTable() {
        resultTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<AuditTrailResultModel>() {
            private void updateView(String message, final AuditTrailResultModel newValue) {
                CodeFormatLanguage mode = CodeFormatLanguage.XML;
                if (newValue.messageType.getValue() != null && newValue.messageType.getValue().toLowerCase().equals("json")) {
                    mode = CodeFormatLanguage.JAVASCRIPT;
                }
                String formatedMessage = codeMirrorFormatter.format(message, mode, true);

                htmlMessageView.getEngine().loadContent(formatedMessage);
                textMessageView.setText(message);
            }

            @Override
            public void changed(ObservableValue<? extends AuditTrailResultModel> observable, AuditTrailResultModel oldValue,
                    final AuditTrailResultModel newValue) {
                // richtext support will is not available in current javafx (plante for 8) for now this is a workaround
                // with a javascriptlib+webview
                if (newValue != null) {

                    final ProgressIndicator bar = new ProgressIndicator();
                    detailstackPane.getChildren().add(bar);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final String message = copperDataProvider.getAuditTrailMessage(newValue.id);

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    detailstackPane.getChildren().remove(bar);
                                    updateView(message, newValue);
                                }
                            });
                        }
                    });
                    thread.setDaemon(true);
                    thread.start();

                } else {
                    htmlMessageView.getEngine().loadContent("");
                    textMessageView.setText("");
                }

            }
        });

    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("AuditTrailResult.fxml");
    }

    @Override
    public void showFilteredResult(List<AuditTrailResultModel> filteredResult, AuditTrailFilterModel usedFilter) {
        ObservableList<AuditTrailResultModel> content = FXCollections.observableList(new ArrayList<AuditTrailResultModel>());
        content.addAll(filteredResult);
        resultTable.setItems(content);

        StringBuilder textresult = new StringBuilder();
        textresult.append("id");
        textresult.append("\t");
        textresult.append("loglevel");
        textresult.append("\t");
        textresult.append("occurrence");
        textresult.append("\t");
        textresult.append("workflowInstanceId");
        textresult.append("\t");
        textresult.append("correlationId");
        textresult.append("\t");
        textresult.append("messageType");
        textresult.append("\t");
        textresult.append("transactionId");
        textresult.append("\n");

        for (AuditTrailResultModel row : filteredResult) {

            textresult.append(row.id.getValue());
            textresult.append("\t");
            textresult.append(row.loglevel.getValue());
            textresult.append("\t");
            textresult.append(row.occurrence.getValue());
            textresult.append("\t");
            textresult.append(row.workflowInstanceId.getValue());
            textresult.append("\t");
            textresult.append(row.correlationId.getValue());
            textresult.append("\t");
            textresult.append(row.messageType.getValue());
            textresult.append("\t");
            textresult.append(row.transactionId.getValue());
            textresult.append("\n");
        }
        resultTextarea.setText(textresult.toString());
    }

    @Override
    public List<AuditTrailResultModel> applyFilterInBackgroundThread(AuditTrailFilterModel filter) {
        return copperDataProvider.getAuditTrails(filter, filter.getMaxCount());
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
