/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.ui.logs.result;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.DateStringConverter;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.filter.FilterResultControllerBase;
import org.copperengine.monitoring.client.ui.logs.filter.LogsFilterModel;
import org.copperengine.monitoring.client.ui.logs.result.LogsResultModel.LogsRowModel;
import org.copperengine.monitoring.client.util.CSSHelper;
import org.copperengine.monitoring.client.util.TableColumnHelper;

public class LogsResultController extends FilterResultControllerBase<LogsFilterModel, LogsResultModel> implements Initializable {
    private final GuiCopperDataProvider copperDataProvider;

    public LogsResultController(GuiCopperDataProvider copperDataProvider) {
        super();
        this.copperDataProvider = copperDataProvider;
    }

    @FXML
    // fx:id="copyButton"
    private Button copyButton; // Value injected by FXMLLoader

    @FXML
    // fx:id="detailstackPane"
    private StackPane detailstackPane; // Value injected by FXMLLoader

    @FXML
    // fx:id="idColumn"
    private TableColumn<LogsRowModel, Date> timeColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="logConfig"
    private TextArea logConfig; // Value injected by FXMLLoader

    @FXML
    // fx:id="loglevelColumn"
    private TableColumn<LogsRowModel, String> loglevelColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="occurrenceColumn"
    private TableColumn<LogsRowModel, String> messageColumn; // Value injected by FXMLLoader

    @FXML
    // fx:id="resultTable"
    private TableView<LogsRowModel> resultTable; // Value injected by FXMLLoader

    @FXML
    // fx:id="resultTextarea"
    private TextArea resultTextarea; // Value injected by FXMLLoader

    @FXML
    // fx:id="searchField"
    private TextField searchField; // Value injected by FXMLLoader

    @FXML
    private BorderPane tableBorderPane;

    @FXML
    private Button updateConfig;

    @FXML
    private TableColumn<LogsRowModel, String> locationColumn;

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert copyButton != null : "fx:id=\"copyButton\" was not injected: check your FXML file 'LogsResult.fxml'.";
        assert detailstackPane != null : "fx:id=\"detailstackPane\" was not injected: check your FXML file 'LogsResult.fxml'.";
        assert timeColumn != null : "fx:id=\"timeColumn\" was not injected: check your FXML file 'LogsResult.fxml'.";
        assert logConfig != null : "fx:id=\"logConfig\" was not injected: check your FXML file 'LogsResult.fxml'.";
        assert loglevelColumn != null : "fx:id=\"loglevelColumn\" was not injected: check your FXML file 'LogsResult.fxml'.";
        assert messageColumn != null : "fx:id=\"occurrenceColumn\" was not injected: check your FXML file 'LogsResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'LogsResult.fxml'.";
        assert resultTextarea != null : "fx:id=\"resultTextarea\" was not injected: check your FXML file 'LogsResult.fxml'.";
        assert searchField != null : "fx:id=\"searchField\" was not injected: check your FXML file 'LogsResult.fxml'.";
        assert tableBorderPane != null;
        assert updateConfig != null;
        assert locationColumn != null;

        resultTable.setRowFactory(new Callback<TableView<LogsRowModel>, TableRow<LogsRowModel>>() {
            @Override
            public TableRow<LogsRowModel> call(TableView<LogsRowModel> param) {
                return new TableRow<LogsRowModel>() {
                    @Override
                    protected void updateItem(LogsRowModel item, boolean empty) {
                        if (item != null) {
                            if ("ERROR".equals(item.level.get())) {
                                this.setStyle("-fx-control-inner-background: " + CSSHelper.toCssColor(Color.rgb(255, 128, 128)) + ";");
                            } else {
                                this.setStyle("");
                            }

                        }
                        super.updateItem(item, empty);
                    }
                };
            }
        });

        loglevelColumn.setCellValueFactory(new Callback<CellDataFeatures<LogsRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<LogsRowModel, String> p) {
                return p.getValue().level;
            }
        });

        tableBorderPane.setBottom(createTabelControlls(resultTable));

        timeColumn.setCellValueFactory(new Callback<CellDataFeatures<LogsRowModel, Date>, ObservableValue<Date>>() {
            @Override
            public ObservableValue<Date> call(
                    CellDataFeatures<LogsRowModel, Date> p) {
                return p.getValue().time;
            }
        });
        TableColumnHelper.setConverterCellFactory(timeColumn, new DateStringConverter("dd.MM.yyyy HH:mm:ss,SSS"));

        locationColumn.setCellValueFactory(new Callback<CellDataFeatures<LogsRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<LogsRowModel, String> p) {
                return p.getValue().locationInformation;
            }
        });
        TableColumnHelper.setTextOverrunCellFactory(locationColumn, OverrunStyle.LEADING_ELLIPSIS);

        messageColumn.setCellValueFactory(new Callback<CellDataFeatures<LogsRowModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<LogsRowModel, String> p) {
                return p.getValue().message;
            }
        });
        messageColumn.setCellFactory(new Callback<TableColumn<LogsRowModel, String>, TableCell<LogsRowModel, String>>() {
            @Override
            public TableCell<LogsRowModel, String> call(TableColumn<LogsRowModel, String> param) {
                final TextFieldTableCell<LogsRowModel, String> textFieldTableCell = new TextFieldTableCell<LogsRowModel, String>();
                textFieldTableCell.getStyleClass().add("consoleFont");
                return textFieldTableCell;
            }
        });

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

        timeColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(3).multiply(0.15));
        loglevelColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(3).multiply(0.05));
        locationColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(3).multiply(0.05));
        messageColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(3).multiply(0.73));

        updateConfig.getStyleClass().add("copperActionButton");
        updateConfig.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                copperDataProvider.updateLogConfig(logConfig.getText());
            }
        });
        logConfig.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateConfig.setDisable(false);
            }
        });

        resultTextarea.getStyleClass().add("consoleFont");
        resultTextarea.setWrapText(false);
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("LogsResult.fxml");
    }

    @Override
    public void showFilteredResult(List<LogsResultModel> filteredResult, LogsFilterModel usedFilter) {
        LogsResultModel resultModel = filteredResult.get(0);

        resultTable.getItems().clear();
        resultTable.setItems(resultModel.logs);

        StringBuilder textresult = new StringBuilder();
        textresult.append("Time");
        textresult.append("\t");
        textresult.append("Level");
        textresult.append("\t");
        textresult.append("message");
        textresult.append("\t");
        textresult.append("Location");
        textresult.append("\n");

        for (LogsRowModel row : resultModel.logs) {
            row.fillString(textresult);
        }
        resultTextarea.setText(textresult.toString());

        logConfig.setText(resultModel.config.get());
        updateConfig.setDisable(true);
    }

    @Override
    public List<LogsResultModel> applyFilterInBackgroundThread(LogsFilterModel filter) {
        return Arrays.asList(copperDataProvider.getLogData(filter.fromToFilterModel.from.get(), filter.fromToFilterModel.to.get(), filter.maxCountFilterModel.getMaxCount()));
    }

    @Override
    public boolean supportsClear() {
        return true;
    }

    @Override
    public void clear() {
        resultTable.getItems().clear();
        resultTextarea.clear();
        logConfig.clear();
    }

}
