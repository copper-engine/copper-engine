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
package de.scoopgmbh.copper.monitoring.client.form.filter;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import de.scoopgmbh.copper.monitoring.client.form.FxmlController;

/**
*
* @param <F>Filtermodel
* @param <T>Resultmodel
*/
public abstract class FilterResultControllerBase<F,R> implements FilterResultController<F,R>, FxmlController {
	
	SimpleObjectProperty<Integer> maxResultCount= new SimpleObjectProperty<Integer>(1000);
	@Override
	public SimpleObjectProperty<Integer> maxResultCountProperty(){
		return maxResultCount;
	}
	
	
	public HBox createTabelControlls(final TableView<?> tableView){
		HBox pane= new HBox();
		BorderPane.setMargin(pane,new Insets(3));
		pane.setSpacing(3);
		pane.setAlignment(Pos.CENTER_LEFT);
		Button copy = new Button("copy");
		copy.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				copyTable(tableView);
			}
		});
		pane.getChildren().add(copy);
		pane.getChildren().add(new Label("Search"));
		final TextField textField = new TextField();
		textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue!=null && newValue.length()>1){
					serachInTable(tableView, newValue);
				}
			}
		});
		textField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				serachInTable(tableView,textField.getText());
			}
		});
		HBox.setHgrow(textField,Priority.SOMETIMES);
		pane.getChildren().add(textField);
		return pane;
	}
	
	private void serachInTable(final TableView<?> tableView, String newValue) {
		int selectedRow= tableView.getSelectionModel().getSelectedIndex();
		int toSelectedRow= tableView.getSelectionModel().getSelectedIndex();
		for (int row=0; row<tableView.getItems().size();row++){
			String rowString="";
			int rowIndex=(row+1+selectedRow)%tableView.getItems().size();
			for (int column=0; column<tableView.getColumns().size();column++){
				 Object cell = tableView.getColumns().get(column).getCellData(rowIndex);
				 if (cell!=null && cell.toString()!=null && cell.toString().contains(newValue)){
					 rowString+=cell.toString();
					
				 }
			}
			if (rowString.contains(newValue)){
				toSelectedRow=rowIndex;
				break;
			}
		}
		final int rowfinal = toSelectedRow;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				tableView.getSelectionModel().select(rowfinal);
				tableView.scrollTo(rowfinal);
			}
		});
	}
	
	private void copyTable(final TableView<?> tableView) {
		StringBuilder clipboardString = new StringBuilder();
		for (int row=0; row<tableView.getItems().size();row++){
			for (int column=0; column<tableView.getColumns().size();column++){
				 Object cell = tableView.getColumns().get(column).getCellData(row);
				 clipboardString.append(cell+"\t");
			}
		}
        final ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
	}
	
	
}
