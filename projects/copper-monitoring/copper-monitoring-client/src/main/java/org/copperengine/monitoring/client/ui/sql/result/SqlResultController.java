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
package org.copperengine.monitoring.client.ui.sql.result;

import java.net.URL;
import java.util.Collections;
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
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.filter.FilterResultControllerBase;
import org.copperengine.monitoring.client.ui.sql.filter.SqlFilterModel;

import com.google.common.base.Strings;

public class SqlResultController extends FilterResultControllerBase<SqlFilterModel, SqlResultModel> implements Initializable {
    private final GuiCopperDataProvider copperDataProvider;

    public SqlResultController(GuiCopperDataProvider copperDataProvider) {
        super();
        this.copperDataProvider = copperDataProvider;
    }

    @FXML
    // fx:id="borderPane"
    private BorderPane borderPane; // Value injected by FXMLLoader

    @FXML
    // fx:id="resultTable"
    private TableView<SqlResultModel> resultTable; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'SqlResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'SqlResult.fxml'.";

        borderPane.setBottom(createTabelControlls(resultTable));
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("SqlResult.fxml");
    }

    @Override
    public void showFilteredResult(List<SqlResultModel> filteredResult, SqlFilterModel usedFilter) {
        resultTable.getColumns().clear();

        if (!filteredResult.isEmpty()) {
            for (int i = 0; i < filteredResult.get(0).rows.size(); i++) {
                TableColumn<SqlResultModel, String> rowColumn = new TableColumn<SqlResultModel, String>();
                rowColumn.setText(filteredResult.get(0).rows.get(i).get());
                final int rowindex = i;
                rowColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SqlResultModel, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(CellDataFeatures<SqlResultModel, String> param) {
                        return param.getValue().rows.get(rowindex);
                    }
                });// -3 for the border
                rowColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(3).divide(filteredResult.get(0).rows.size()));
                resultTable.getColumns().add(rowColumn);
            }
            ObservableList<SqlResultModel> content = FXCollections.observableArrayList();
            content.addAll(filteredResult);
            content.remove(0);
            resultTable.setItems(content);
        }
    }

    @Override
    public List<SqlResultModel> applyFilterInBackgroundThread(SqlFilterModel filter) {
        if (!Strings.isNullOrEmpty(filter.sqlQuery.get())) {
            return copperDataProvider.executeSqlQuery(filter, filter.getMaxCount());
        }
        return Collections.emptyList();
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
