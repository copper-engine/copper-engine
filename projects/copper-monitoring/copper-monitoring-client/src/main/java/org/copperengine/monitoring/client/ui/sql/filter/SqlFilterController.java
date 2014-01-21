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
package org.copperengine.monitoring.client.ui.sql.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebView;

import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.form.filter.BaseFilterController;
import org.copperengine.monitoring.client.form.filter.FilterController;
import org.copperengine.monitoring.client.form.filter.defaultfilter.DefaultFilterFactory;
import org.copperengine.monitoring.client.util.CodeMirrorFormatter;
import org.copperengine.monitoring.client.util.CodeMirrorFormatter.CodeFormatLanguage;

public class SqlFilterController extends BaseFilterController<SqlFilterModel> implements Initializable, FxmlController {
    private final SqlFilterModel model = new SqlFilterModel();
    private final CodeMirrorFormatter codeMirrorFormatter;

    @FXML
    // fx:id="history"
    private ComboBox<String> history; // Value injected by FXMLLoader

    @FXML
    // fx:id="sqlEditor"
    private WebView sqlEditor; // Value injected by FXMLLoader

    public SqlFilterController(CodeMirrorFormatter codeMirrorFormatter) {
        this.codeMirrorFormatter = codeMirrorFormatter;
    }

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert history != null : "fx:id=\"history\" was not injected: check your FXML file 'SqlFilter.fxml'.";
        assert sqlEditor != null : "fx:id=\"sqlEditor\" was not injected: check your FXML file 'SqlFilter.fxml'.";

        history.getItems().add("SELECT * FROM COP_WORKFLOW_INSTANCE");
        history.getItems().add("SELECT * FROM COP_WORKFLOW_INSTANCE_ERROR");
        history.getItems().add("SELECT * FROM COP_RESPONSE");
        history.getItems().add("SELECT * FROM COP_WAIT");
        history.getItems().add("SELECT * FROM COP_QUEUE");
        history.getItems().add("SELECT * FROM COP_AUDIT_TRAIL_EVENT");
        final int fixSize = history.getItems().size();

        sqlEditor.getEngine().loadContent(codeMirrorFormatter.format("", CodeFormatLanguage.SQL, false));
        sqlEditor.setOnKeyReleased(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        String query = (String) sqlEditor.getEngine().executeScript("editor.getValue();");
                        model.sqlQuery.setValue(query);
                        if (!query.isEmpty()) {
                            history.getItems().add(query);
                            if (history.getItems().size() > fixSize + 10) {
                                history.getItems().remove(fixSize);
                            }
                        }
                    }
                });
            }
        });

        history.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                model.sqlQuery.setValue(history.getSelectionModel().getSelectedItem());
                sqlEditor.getEngine().executeScript("editor.setValue('" + history.getSelectionModel().getSelectedItem() + "');");
            }
        });
    }

    @Override
    public SqlFilterModel getFilter() {
        return model;
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("SqlFilter.fxml");
    }

    @Override
    public boolean supportsFiltering() {
        return true;
    }

    @Override
    public long getDefaultRefreshInterval() {
        return FilterController.DEFAULT_REFRESH_INTERVAL;
    }

    @Override
    public Node createDefaultFilter() {
        return new DefaultFilterFactory().createMaxCount(model);
    }
}
