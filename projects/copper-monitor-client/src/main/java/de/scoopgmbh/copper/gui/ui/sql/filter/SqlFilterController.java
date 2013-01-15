/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.gui.ui.sql.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.web.WebView;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterController;
import de.scoopgmbh.copper.gui.util.CodeMirrorFormatter;
import de.scoopgmbh.copper.gui.util.CodeMirrorFormatter.CodeFormatLanguage;

public class SqlFilterController implements Initializable, FilterController<SqlFilterModel>, FxmlController {
	private final SqlFilterModel model = new SqlFilterModel();
	private final CodeMirrorFormatter codeMirrorFormatter;

    @FXML //  fx:id="history"
    private ChoiceBox<?> history; // Value injected by FXMLLoader

    @FXML //  fx:id="sqlEditor"
    private WebView sqlEditor; // Value injected by FXMLLoader


    public SqlFilterController(CodeMirrorFormatter codeMirrorFormatter) {
		this.codeMirrorFormatter = codeMirrorFormatter;
	}

	@Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert history != null : "fx:id=\"history\" was not injected: check your FXML file 'SqlFilter.fxml'.";
        assert sqlEditor != null : "fx:id=\"sqlEditor\" was not injected: check your FXML file 'SqlFilter.fxml'.";

        sqlEditor.getEngine().loadContent(codeMirrorFormatter.format("", CodeFormatLanguage.SQL));
        sqlEditor.setOnKeyTyped(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				model.sqlQuery.setValue( (String )sqlEditor.getEngine().executeScript("editor.getValue();"));
			}
		});
	}

	@Override
	public SqlFilterModel getFilter() {
		return model;
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("SqlFilter.fxml");
	}
	
	
}
