package de.scoopgmbh.copper.gui.ui.audittrail.result;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.audittrail.filter.AuditTrailFilterModel;

public class AuditTrailResultController implements Initializable, FilterResultController<AuditTrailFilterModel>, FxmlController {
	GuiCopperDataProvider copperDataProvider;

	public AuditTrailResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}



    @FXML //  fx:id="conversationIdColumn"
    private TableColumn<AuditTrailResultModel, String> conversationIdColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="correlationIdColumn"
    private TableColumn<AuditTrailResultModel, String> correlationIdColumn; // Value injected by FXMLLoader

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

    @FXML //  fx:id="transactionIdColumn"
    private TableColumn<AuditTrailResultModel, String> transactionIdColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowInstanceIdColumn"
    private TableColumn<AuditTrailResultModel, String> workflowInstanceIdColumn; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert conversationIdColumn != null : "fx:id=\"conversationIdColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert correlationIdColumn != null : "fx:id=\"correlationIdColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert idColumn != null : "fx:id=\"idColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert loglevelColumn != null : "fx:id=\"loglevelColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert messageTypeColumn != null : "fx:id=\"messageTypeColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert occurrenceColumn != null : "fx:id=\"occurrenceColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert transactionIdColumn != null : "fx:id=\"transactionIdColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";
        assert workflowInstanceIdColumn != null : "fx:id=\"workflowInstanceIdColumn\" was not injected: check your FXML file 'AuditTrailResult.fxml'.";



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
        
        resultTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

	@Override
	public void applyFilter(AuditTrailFilterModel filter) {
		ObservableList<AuditTrailResultModel> content = FXCollections.observableList(new ArrayList<AuditTrailResultModel>());;
		content.addAll(copperDataProvider.getAuditTrails(filter));
		resultTable.setItems(content);
	}
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("AuditTrailResult.fxml");
	}

}
