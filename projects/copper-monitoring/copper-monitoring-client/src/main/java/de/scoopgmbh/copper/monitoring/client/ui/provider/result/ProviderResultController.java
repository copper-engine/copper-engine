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
package de.scoopgmbh.copper.monitoring.client.ui.provider.result;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import javafx.util.converter.DateStringConverter;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.client.ui.provider.filter.ProviderFilterModel;
import de.scoopgmbh.copper.monitoring.client.util.TableColumnHelper;

public class ProviderResultController extends FilterResultControllerBase<ProviderFilterModel,ProviderResultModel> implements Initializable {
	private final GuiCopperDataProvider copperDataProvider;

	
	public ProviderResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}



    @FXML //  fx:id="content"
    private WebView content; // Value injected by FXMLLoader

    @FXML //  fx:id="idColumn"
    private TableColumn<ProviderResultModel, String> idColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="resultTable"
    private TableView<ProviderResultModel> resultTable; // Value injected by FXMLLoader

    @FXML //  fx:id="timeColumn"
    private TableColumn<ProviderResultModel, Date> timeColumn; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert content != null : "fx:id=\"content\" was not injected: check your FXML file 'ProviderResult.fxml'.";
        assert idColumn != null : "fx:id=\"idColumn\" was not injected: check your FXML file 'ProviderResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'ProviderResult.fxml'.";
        assert timeColumn != null : "fx:id=\"timeColumn\" was not injected: check your FXML file 'ProviderResult.fxml'.";

        
        idColumn.setCellValueFactory(new Callback<CellDataFeatures<ProviderResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<ProviderResultModel, String> p) {
				return p.getValue().creatorId;
			}
		});
        timeColumn.setCellValueFactory(new Callback<CellDataFeatures<ProviderResultModel, Date>, ObservableValue<Date>>() {
			@Override
			public ObservableValue<Date> call(
					CellDataFeatures<ProviderResultModel, Date> p) {
				return p.getValue().timeStamp;
			}
		});
        TableColumnHelper.setConverterCellFactory(timeColumn, new DateStringConverter("dd.MM.yyyy HH:mm:ss,SSS"));
        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
     
        resultTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ProviderResultModel>() {
			@Override
			public void changed(ObservableValue<? extends ProviderResultModel> observable, ProviderResultModel oldValue,
					ProviderResultModel newValue) {
				if (newValue!=null){
					content.getEngine().loadContent(newValue.content.get());
				}
			}
		});
        

    }

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("ProviderResult.fxml");
	}
	
	@Override
	public void showFilteredResult(List<ProviderResultModel> filteredResult, ProviderFilterModel usedFilter) {
		resultTable.getItems().clear();
		resultTable.getItems().addAll(filteredResult);
	}

	@Override
	public List<ProviderResultModel> applyFilterInBackgroundThread(ProviderFilterModel filter) {
		return copperDataProvider.getGenericMonitoringData(filter.id.get(),filter.fromToFilterModel.from.get(),filter.fromToFilterModel.to.get(),filter.maxCountFilterModel.getMaxCount());
	}

	@Override
	public void clear() {
		resultTable.getItems().clear();
		content.getEngine().loadContent("");
	}


}
