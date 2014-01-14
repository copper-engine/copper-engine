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
package org.copperengine.monitoring.client.ui.custommeasurepoint.filter;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.form.filter.BaseFilterController;
import org.copperengine.monitoring.client.form.filter.defaultfilter.DefaultFilterFactory;
import org.copperengine.monitoring.client.util.ComponentUtil;

public class CustomMeasurePointFilterController extends BaseFilterController<CustomMeasurePointFilterModel> implements Initializable, FxmlController {
    final CustomMeasurePointFilterModel model = new CustomMeasurePointFilterModel();

    private final GuiCopperDataProvider copperDataProvider;

    public CustomMeasurePointFilterController(GuiCopperDataProvider copperDataProvider) {
        super();
        this.copperDataProvider = copperDataProvider;
    }

    @FXML
    // fx:id="measurePointChoice"
    private ComboBox<String> measurePointIdComboBox; // Value injected by FXMLLoader

    @FXML
    // fx:id="measurePointText"
    private TextField measurePointText; // Value injected by FXMLLoader

    @FXML
    // fx:id="parentStackPane"
    private StackPane parentStackPane; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert measurePointIdComboBox != null : "fx:id=\"measurePointIdComboBox\" was not injected: check your FXML file 'CustomMeasurePointFilter.fxml'.";
        assert measurePointText != null : "fx:id=\"measurePointText\" was not injected: check your FXML file 'CustomMeasurePointFilter.fxml'.";
        assert parentStackPane != null : "fx:id=\"parentStackPane\" was not injected: check your FXML file 'CustomMeasurePointFilter.fxml'.";

        model.maxCountFilterModel.maxCount.set(50);

        model.measurePointId.bind(measurePointText.textProperty());

        measurePointIdComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    measurePointText.setText(newValue);
                }
            }
        });
        measurePointIdComboBox.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                final TextFieldListCell<String> textFieldListCell = new TextFieldListCell<String>();
                textFieldListCell.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
                return textFieldListCell;
            }
        });

        measurePointIdComboBox.getItems().clear();
        measurePointIdComboBox.getItems().addAll(copperDataProvider.getMonitoringMeasurePointIds(model.fromToFilterModel.from.get(), model.fromToFilterModel.to.get()));
        measurePointIdComboBox.getSelectionModel().selectFirst();

        model.fromToFilterModel.from.addListener(new ChangeListener<Date>() {
            @Override
            public void changed(ObservableValue<? extends Date> observable, Date oldValue, Date newValue) {
                updateMeasurePointIds();

            }
        });
        model.fromToFilterModel.to.addListener(new ChangeListener<Date>() {
            @Override
            public void changed(ObservableValue<? extends Date> observable, Date oldValue, Date newValue) {
                updateMeasurePointIds();

            }
        });

    }

    public void updateMeasurePointIds() {
        ComponentUtil.executeWithProgressDialogInBackground(new Runnable() {
            @Override
            public void run() {
                final List<String> monitoringMeasurePointIds = copperDataProvider.getMonitoringMeasurePointIds(model.fromToFilterModel.from.get(), model.fromToFilterModel.to.get());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        measurePointIdComboBox.getItems().clear();
                        measurePointIdComboBox.getItems().addAll(monitoringMeasurePointIds);
                    }
                });
            }
        }, parentStackPane, "");
    }

    @Override
    public CustomMeasurePointFilterModel getFilter() {
        return model;
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("CustomMeasurePointFilter.fxml");
    }

    @Override
    public boolean supportsFiltering() {
        return true;
    }

    @Override
    public long getDefaultRefreshInterval() {
        return 1500;
    }

    @Override
    public Node createDefaultFilter() {
        return new DefaultFilterFactory().createFromToMaxCount(model);
    }

}
