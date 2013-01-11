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
package de.scoopgmbh.copper.gui.ui.audittrail.result;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.audittrail.filter.AuditTrailFilterModel;
import de.scoopgmbh.copper.gui.ui.settings.AuditralColorMapping;
import de.scoopgmbh.copper.gui.ui.settings.SettingsModel;

public class AuditTrailResultController implements Initializable, FilterResultController<AuditTrailFilterModel,AuditTrailResultModel>, FxmlController {
	GuiCopperDataProvider copperDataProvider;
	SettingsModel settingsModel;

	public AuditTrailResultController(GuiCopperDataProvider copperDataProvider, SettingsModel settingsModel) {
		super();
		this.copperDataProvider = copperDataProvider;
		this.settingsModel = settingsModel;
	}


    @FXML //  fx:id="conversationIdColumn"
    private TableColumn<AuditTrailResultModel, String> conversationIdColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="correlationIdColumn"
    private TableColumn<AuditTrailResultModel, String> correlationIdColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="htmlMessageView"
    private WebView htmlMessageView; // Value injected by FXMLLoader

    @FXML //  fx:id="idColumn"
    private TableColumn<AuditTrailResultModel, String> idColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="loglevelColumn"
    private TableColumn<AuditTrailResultModel, String> loglevelColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="messageTypeColumn"
    private TableColumn<AuditTrailResultModel, String> messageTypeColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="occurrenceColumn"
    private TableColumn<AuditTrailResultModel, String> occurrenceColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="resultTable"
    private TableView<AuditTrailResultModel> resultTable; // Value injected by FXMLLoader

    @FXML //  fx:id="textMessageView"
    private TextArea textMessageView; // Value injected by FXMLLoader

    @FXML //  fx:id="transactionIdColumn"
    private TableColumn<AuditTrailResultModel, String> transactionIdColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowInstanceIdColumn"
    private TableColumn<AuditTrailResultModel, String> workflowInstanceIdColumn; // Value injected by FXMLLoader
    
    @FXML //  fx:id="detailstackPane"
    private StackPane detailstackPane; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
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
        
        
        resultTable.setRowFactory(new Callback<TableView<AuditTrailResultModel>, TableRow<AuditTrailResultModel>>() {
			@Override
			public TableRow<AuditTrailResultModel> call(TableView<AuditTrailResultModel> param) {
				return new TableRow<AuditTrailResultModel>(){
					@Override
					protected void updateItem(AuditTrailResultModel item, boolean empty) {
						if (item!=null ){
							for (int i=0;i<settingsModel.auditralColorMappings.size();i++){
								AuditralColorMapping auditralColorMapping = settingsModel.auditralColorMappings.get(i);
								if (auditralColorMapping.match(item)){
									this.setStyle("-fx-background-color: rgb("+
											(int)(255*auditralColorMapping.color.getValue().getRed())+","+
											(int)(255*auditralColorMapping.color.getValue().getGreen())+","+
											(int)(255*auditralColorMapping.color.getValue().getBlue())+");");

								}
							}
						}
						super.updateItem(item, empty);
					}
				};
			}
		});
   
        
        loglevelColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditTrailResultModel, String> p) {
				return p.getValue().loglevel.asString();
			}
		});
        
        conversationIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditTrailResultModel, String> p) {
				return p.getValue().conversationId;
			}
		});
        
        correlationIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditTrailResultModel, String> p) {
				return p.getValue().correlationId;
			}
		});

        idColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditTrailResultModel, String> p) {
				return p.getValue().id.asString();
			}
		});
        
        messageTypeColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditTrailResultModel, String> p) {
				return p.getValue().messageType;
			}
		});
        
        occurrenceColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditTrailResultModel, String> p) {
				return p.getValue().occurrence;
			}
		});
        
        transactionIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditTrailResultModel, String> p) {
				return p.getValue().transactionId;
			}
		});
        
        workflowInstanceIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditTrailResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditTrailResultModel, String> p) {
				return p.getValue().workflowInstanceId;
			}
		});
        
        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
		final ProgressIndicator bar = new ProgressIndicator();
		detailstackPane.getChildren().add(bar);
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String codemirrorcss;
				final String codemirrorjs;
				final String xmlmodejs;
				final String javascriptjs;
				try {
					try (InputStream input = getClass().getResourceAsStream("/codemirror/lib/codemirror.css")) {
						codemirrorcss = convertStreamToString(input);
					}
					try (InputStream input = getClass().getResourceAsStream("/codemirror/lib/codemirror.js")) {
						codemirrorjs = convertStreamToString(input);
					}
					try (InputStream input = getClass().getResourceAsStream("/codemirror/mode/json/javascript.js")) {
						javascriptjs = convertStreamToString(input);
					}
					try (InputStream input = getClass().getResourceAsStream("/codemirror/mode/xml/xml.js")) {
						xmlmodejs = convertStreamToString(input);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						detailstackPane.getChildren().remove(bar);
						initTable(codemirrorcss, codemirrorjs, xmlmodejs, javascriptjs);
					}
				});
			}
		}).start();
		
    }

	private void initTable(final String codemirrorcss, final String codemirrorjs, final String xmlmodejs, final String javascriptjs) {
		resultTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<AuditTrailResultModel>() {
			private void updateView(String message, final AuditTrailResultModel newValue){
				String mode = xmlmodejs;
				String modeScript= "<script>\n" + 
						"      var editor = CodeMirror.fromTextArea(document.getElementById(\"code\"), {\n" + 
						"        mode: {name: \"xml\", alignCDATA: true},\n" + 
						"        lineNumbers: true\n" + 
						"      });\n" + 
						"    </script>";
				if (newValue.messageType.getValue()!=null && newValue.messageType.getValue().toLowerCase().contains("json")){
					mode=javascriptjs;
					modeScript=
							"    <script>\r\n" + 
							"      var editor = CodeMirror.fromTextArea(document.getElementById(\"code\"), {\r\n" + 
							"        lineNumbers: true,\r\n" + 
							"        matchBrackets: true,\r\n" + 
							"        extraKeys: {\"Enter\": \"newlineAndIndentContinueComment\"}\r\n" + 
							"      });\r\n" + 
							"    </script>";
				}
						
				String formatedMessage = "<!doctype html>" +
						"<html><head>" +
						"<style type=\"text/css\">\n" + 
						codemirrorcss+"\n"+
						"</style>"+
						" <script>"+codemirrorjs+"</script>" +
						" <script>"+mode+"</script>" +
						"</head>" +
						"<body>" +
						"<form><textarea id=\"code\" name=\"code\" style=\"width: 100%; height: 100%;\">\n" +
						message+
						"</textarea></form>" +
						modeScript+
						"</body>" +
						"</html>";
	
				htmlMessageView.getEngine().loadContent(
						formatedMessage);
				textMessageView.setText(message);
			}
			
			@Override
			public void changed(ObservableValue<? extends AuditTrailResultModel> observable, AuditTrailResultModel oldValue,
					final AuditTrailResultModel newValue) {
				//richtext support will is not available in current javafx (plante for 8) for now this is a workaround with a javascriptlib+webview
				if (newValue!=null){
					
					final ProgressIndicator bar = new ProgressIndicator();
					detailstackPane.getChildren().add(bar);
					new Thread(new Runnable() {
						@Override
						public void run() {
							final String message = copperDataProvider.getAuditMessage(newValue.id);

							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									detailstackPane.getChildren().remove(bar);
									updateView(message,newValue);
								}
							});
						}
					}).start();

				} else {
					htmlMessageView.getEngine().loadContent("");
					textMessageView.setText("");
				}
				
			}
		});
		
	}
    
    public static String convertStreamToString(java.io.InputStream is) {
        @SuppressWarnings("resource")
		java.util.Scanner s = new java.util.Scanner(is,"UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("AuditTrailResult.fxml");
	}

	@Override
	public void showFilteredResult(List<AuditTrailResultModel> filteredResult, AuditTrailFilterModel usedFilter) {
		ObservableList<AuditTrailResultModel> content = FXCollections.observableList(new ArrayList<AuditTrailResultModel>());;
		content.addAll(filteredResult);
		resultTable.setItems(content);
	}

	@Override
	public List<AuditTrailResultModel> applyFilterInBackgroundThread(AuditTrailFilterModel filter) {
		return copperDataProvider.getAuditTrails(filter);
	}

	@Override
	public boolean canLimitResult() {
		return true;
	}

}
