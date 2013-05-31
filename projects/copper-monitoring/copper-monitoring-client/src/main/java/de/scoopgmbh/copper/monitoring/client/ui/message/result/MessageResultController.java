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
package de.scoopgmbh.copper.monitoring.client.ui.message.result;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.converter.DateStringConverter;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.client.ui.message.filter.MessageFilterModel;
import de.scoopgmbh.copper.monitoring.client.util.TableColumnHelper;

public class MessageResultController extends FilterResultControllerBase<MessageFilterModel,MessageResultModel> implements Initializable {
	private final GuiCopperDataProvider copperDataProvider;
	
	public MessageResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}
	
    @FXML //  fx:id="borderPane"
    private BorderPane borderPane;

    @FXML //  fx:id="idColumn"
    private TableColumn<MessageResultModel, String> idColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="resultTable"
    private TableView<MessageResultModel> resultTable; // Value injected by FXMLLoader

    @FXML //  fx:id="stateColumn"
    private TableColumn<MessageResultModel, String> messageColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="timeColumn"
    private TableColumn<MessageResultModel, Date> timeColumn; // Value injected by FXMLLoader
    
    @FXML 
    private TableColumn<MessageResultModel, Date> timeout; // Value injected by FXMLLoader

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert idColumn != null : "fx:id=\"idColumn\" was not injected: check your FXML file 'MessageResult.fxml'.";
        assert messageColumn != null : "fx:id=\"messageColumn\" was not injected: check your FXML file 'MessageResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'MessageResult.fxml'.";
        assert timeColumn != null : "fx:id=\"timeColumn\" was not injected: check your FXML file 'MessageResult.fxml'.";
        assert timeout != null;
        assert borderPane != null;
        
        final HBox createTabelControlls = createTabelControlls(resultTable);
        BorderPane.setMargin(createTabelControlls, new Insets(3));
		borderPane.setBottom(createTabelControlls);

        timeColumn.setCellValueFactory(new Callback<CellDataFeatures<MessageResultModel, Date>, ObservableValue<Date>>() {
				@Override
     			public ObservableValue<Date> call(
     					CellDataFeatures<MessageResultModel, Date> p) {
     				return p.getValue().time;
     			}
     		});
        TableColumnHelper.setConverterCellFactory(timeColumn, new DateStringConverter("dd.MM.yyyy HH:mm:ss:SSS"));
        timeout.setCellValueFactory(new Callback<CellDataFeatures<MessageResultModel, Date>, ObservableValue<Date>>() {
				@Override
     			public ObservableValue<Date> call(
     					CellDataFeatures<MessageResultModel, Date> p) {
     				return p.getValue().timeout;
     			}
     		});
        TableColumnHelper.setConverterCellFactory(timeout, new DateStringConverter("dd.MM.yyyy HH:mm:ss:SSS"));
        idColumn.setCellValueFactory(new Callback<CellDataFeatures<MessageResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<MessageResultModel, String> p) {
				return p.getValue().correlationId;
			}
		});
        
        messageColumn.setCellValueFactory(new Callback<CellDataFeatures<MessageResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<MessageResultModel, String> p) {
				return p.getValue().message;
			}
		});
        

//        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        timeColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(2).multiply(0.15));
        timeout.prefWidthProperty().bind(resultTable.widthProperty().subtract(2).multiply(0.15));
        messageColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(2).multiply(0.45));
        idColumn.prefWidthProperty().bind(resultTable.widthProperty().subtract(2).multiply(0.25));

    }
    
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("MessageResult.fxml");
	}

	@Override
	public void showFilteredResult(List<MessageResultModel> filteredResult, MessageFilterModel usedFilter) {
		ObservableList<MessageResultModel> content = FXCollections.observableList(new ArrayList<MessageResultModel>());;
		content.addAll(filteredResult);
		resultTable.setItems(content);
	}

	@Override
	public List<MessageResultModel> applyFilterInBackgroundThread(MessageFilterModel filter) {
		return copperDataProvider.getMessageList(filter,maxResultCountProperty().get());  
	}
	
	@Override
	public boolean canLimitResult() {
		return true;
	}
	
	@Override
	public void clear() {
		resultTable.getItems().clear();
	}

}
