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
package org.copperengine.monitoring.client.ui.databasemonitor.result;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.filter.EmptyFilterModel;
import org.copperengine.monitoring.client.form.filter.FilterResultControllerBase;
import org.copperengine.monitoring.client.util.ComponentUtil;

public class DatabaseMonitorResultController extends FilterResultControllerBase<EmptyFilterModel, String> implements Initializable {
    private final GuiCopperDataProvider copperDataProvider;

    public DatabaseMonitorResultController(GuiCopperDataProvider copperDataProvider) {
        super();
        this.copperDataProvider = copperDataProvider;
    }

    @FXML
    // fx:id="detailView"
    private WebView detailView; // Value injected by FXMLLoader

    @FXML
    // fx:id="listView"
    private WebView listView; // Value injected by FXMLLoader

    @FXML
    // fx:id="showDeatils"
    private Button showDeatils; // Value injected by FXMLLoader

    @FXML
    // fx:id="showTuning"
    private Button showTuning; // Value injected by FXMLLoader

    @FXML
    // fx:id="sqlId"
    private TextField sqlId; // Value injected by FXMLLoader

    @FXML
    // fx:id="stackpane"
    private StackPane stackpane; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert detailView != null : "fx:id=\"detailView\" was not injected: check your FXML file 'DatabaseMonitorResult.fxml'.";
        assert listView != null : "fx:id=\"listView\" was not injected: check your FXML file 'DatabaseMonitorResult.fxml'.";
        assert showDeatils != null : "fx:id=\"showDeatils\" was not injected: check your FXML file 'DatabaseMonitorResult.fxml'.";
        assert showTuning != null : "fx:id=\"showTuning\" was not injected: check your FXML file 'DatabaseMonitorResult.fxml'.";
        assert sqlId != null : "fx:id=\"sqlId\" was not injected: check your FXML file 'DatabaseMonitorResult.fxml'.";
        assert stackpane != null : "fx:id=\"stackpane\" was not injected: check your FXML file 'DatabaseMonitorResult.fxml'.";

        showDeatils.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ComponentUtil.executeWithProgressDialogInBackground(new Runnable() {
                    @Override
                    public void run() {
                        final String text = copperDataProvider.getDatabaseMonitoringHtmlDetailReport(sqlId.getText());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                detailView.getEngine().loadContent(text);
                            }
                        });
                    }
                }, stackpane, "");
            }
        });
        showDeatils.disableProperty().bind(sqlId.textProperty().isEqualTo(""));

        showTuning.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ComponentUtil.executeWithProgressDialogInBackground(new Runnable() {
                    @Override
                    public void run() {
                        String text = copperDataProvider.getDatabaseMonitoringRecommendationsReport(sqlId.getText());
                        text = text.replace("\n", "<br/>");
                        text = "<span style=\"font-family:courier new\">" + text + "</span>";
                        final String finalText = text;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                detailView.getEngine().loadContent(finalText);
                            }
                        });
                    }
                }, stackpane, "");

            }
        });
        showTuning.disableProperty().bind(showDeatils.disableProperty());
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("DatabaseMonitorResult.fxml");
    }

    @Override
    public void showFilteredResult(List<String> filteredlist, EmptyFilterModel usedFilter) {
        listView.getEngine().loadContent(filteredlist.get(0));

    }

    @Override
    public List<String> applyFilterInBackgroundThread(EmptyFilterModel filter) {
        return Arrays.asList(copperDataProvider.getDatabaseMonitoringHtmlReport());
    }

    @Override
    public boolean supportsClear() {
        return true;
    }

    @Override
    public void clear() {
        listView.getEngine().loadContent("");
    }
}
