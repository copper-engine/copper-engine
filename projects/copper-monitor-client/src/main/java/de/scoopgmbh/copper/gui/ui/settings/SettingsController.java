package de.scoopgmbh.copper.gui.ui.settings;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import de.scoopgmbh.copper.gui.form.FxmlController;

public class SettingsController implements Initializable, FxmlController {
	private final SettingsModel settingsModel;
	public SettingsController(SettingsModel settingsModel) {
		super();
		this.settingsModel=settingsModel;
	}


    @FXML //  fx:id="addButton"
    private Button addButton; // Value injected by FXMLLoader

    @FXML //  fx:id="colorColumn"
    private TableColumn<AuditralColorMapping, Color> colorColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="conversationIdColumn"
    private TableColumn<AuditralColorMapping, String> conversationIdColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="correlationIdColumn"
    private TableColumn<AuditralColorMapping, String> correlationIdColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="idColumn"
    private TableColumn<AuditralColorMapping, String> idColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="loglevelColumn"
    private TableColumn<AuditralColorMapping, String> loglevelColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="messageTypeColumn"
    private TableColumn<AuditralColorMapping, String> messageTypeColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="occurrenceColumn"
    private TableColumn<AuditralColorMapping, String> occurrenceColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="removeButton"
    private Button removeButton; // Value injected by FXMLLoader

    @FXML //  fx:id="transactionIdColumn"
    private TableColumn<AuditralColorMapping, String> transactionIdColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowInstanceIdColumn"
    private TableColumn<AuditralColorMapping, String> workflowInstanceIdColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="colorTable"
    private TableView<AuditralColorMapping> colorTable; // Value injected by FXMLLoader
    
    @FXML //  fx:id="colorDetail"
    private ColorPicker colorDetail; // Value injected by FXMLLoader
    
    @FXML //  fx:id="occurrenceDetail"
    private TextField occurrenceDetail; // Value injected by FXMLLoader



    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert addButton != null : "fx:id=\"addButton\" was not injected: check your FXML file 'Settings.fxml'.";
        assert colorColumn != null : "fx:id=\"colorColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert conversationIdColumn != null : "fx:id=\"conversationIdColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert correlationIdColumn != null : "fx:id=\"correlationIdColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert idColumn != null : "fx:id=\"idColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert loglevelColumn != null : "fx:id=\"loglevelColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert messageTypeColumn != null : "fx:id=\"messageTypeColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert occurrenceColumn != null : "fx:id=\"occurrenceColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert removeButton != null : "fx:id=\"removeButton\" was not injected: check your FXML file 'Settings.fxml'.";
        assert transactionIdColumn != null : "fx:id=\"transactionIdColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert workflowInstanceIdColumn != null : "fx:id=\"workflowInstanceIdColumn\" was not injected: check your FXML file 'Settings.fxml'.";
        assert colorTable != null : "fx:id=\"colorTable\" was not injected: check your FXML file 'Settings.fxml'.";
        assert colorDetail != null : "fx:id=\"colorDetail\" was not injected: check your FXML file 'Settings.fxml'.";
        assert occurrenceDetail != null : "fx:id=\"occurrenceDetail\" was not injected: check your FXML file 'Settings.fxml'.";
        
        colorColumn.setCellFactory(new Callback<TableColumn<AuditralColorMapping,Color>, TableCell<AuditralColorMapping,Color>>() {
			@Override
			public TableCell<AuditralColorMapping, Color> call(TableColumn<AuditralColorMapping, Color> param) {
				return new TextFieldTableCell<AuditralColorMapping, Color>(){
					@Override
					public void updateItem(Color color, boolean empty) {
						if (color != null) {
							this.setTextFill(color);
						}
						super.updateItem(color, empty);
					};
				};
			}
		});
        
        colorColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, Color>, ObservableValue<Color>>() {
			public ObservableValue<Color> call(
					CellDataFeatures<AuditralColorMapping, Color> p) {
				return p.getValue().color;
			}
		});
		
        conversationIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditralColorMapping, String> p) {
				return p.getValue().conversationIdRegEx;
			}
		});
        
        correlationIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditralColorMapping, String> p) {
				return p.getValue().correlationIdRegEx;
			}
		});

        idColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditralColorMapping, String> p) {
				return p.getValue().idRegEx;
			}
		});
        
        messageTypeColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditralColorMapping, String> p) {
				return p.getValue().messageTypeRegEx;
			}
		});
        
        occurrenceColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditralColorMapping, String> p) {
				return p.getValue().occurrenceRegEx;
			}
		});
        
        transactionIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditralColorMapping, String> p) {
				return p.getValue().transactionIdRegEx;
			}
		});
        
        workflowInstanceIdColumn.setCellValueFactory(new Callback<CellDataFeatures<AuditralColorMapping, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<AuditralColorMapping, String> p) {
				return p.getValue().workflowInstanceIdRegEx;
			}
		});
        
		ObservableList<AuditralColorMapping> content = FXCollections.observableList(new ArrayList<AuditralColorMapping>());;
		content.addAll(settingsModel.auditralColorMappings);
		colorTable.setItems(content);
		
		addButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				AuditralColorMapping newItem = new AuditralColorMapping();
				newItem.color.setValue(Color.RED);
				newItem.idRegEx.set("*");
				colorTable.getItems().add(newItem);
			}
		});
	
		colorTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<AuditralColorMapping>() {
			@Override
			public void changed(ObservableValue<? extends AuditralColorMapping> observable, AuditralColorMapping oldValue,
					AuditralColorMapping newValue) {
				if (oldValue!=null){
					colorDetail.valueProperty().unbindBidirectional(oldValue.color);
					occurrenceDetail.textProperty().unbindBidirectional(oldValue.occurrenceRegEx);
				}
				if (newValue!=null){
					colorDetail.valueProperty().bindBidirectional(newValue.color);
					occurrenceDetail.textProperty().bindBidirectional(newValue.occurrenceRegEx);
				} 
			}
		});
		
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("Settings.fxml");
	}
}
