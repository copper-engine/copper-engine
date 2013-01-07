package de.scoopgmbh.copper.gui.ui.workflowsummery.result;

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
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterModel;

public class WorkflowSummeryResultController implements Initializable, FilterResultController<WorkflowSummeryFilterModel>, FxmlController {
	GuiCopperDataProvider copperDataProvider;

	public WorkflowSummeryResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}


    @FXML //  fx:id="aliasColumn"
    private TableColumn<WorkflowSummeryResultModel, String> aliasColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="countColumn"
    private TableColumn<WorkflowSummeryResultModel, String> countColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="majorVersionColumn"
    private TableColumn<WorkflowSummeryResultModel, String> majorVersionColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="minorVersionColumn"
    private TableColumn<WorkflowSummeryResultModel, String> minorVersionColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="resultTable"
    private TableView<WorkflowSummeryResultModel> resultTable; // Value injected by FXMLLoader

    @FXML //  fx:id="statusColumn"
    private TableColumn<WorkflowSummeryResultModel, String> statusColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowClass"
    private TableColumn<WorkflowSummeryResultModel, String> workflowClassColumn; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert aliasColumn != null : "fx:id=\"aliasColumn\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";
        assert countColumn != null : "fx:id=\"countColumn\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";
        assert majorVersionColumn != null : "fx:id=\"majorVersionColumn\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";
        assert minorVersionColumn != null : "fx:id=\"minorVersionColumn\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";
        assert statusColumn != null : "fx:id=\"statusColumn\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";
        assert workflowClassColumn != null : "fx:id=\"workflowClassColumn\" was not injected: check your FXML file 'WorkflowSummeryResult.fxml'.";


        workflowClassColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummeryResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowSummeryResultModel, String> p) {
				return p.getValue().clazz;
			}
		});
        
        majorVersionColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummeryResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowSummeryResultModel, String> p) {
				return p.getValue().workflowMajorVersion;
			}
		});

        minorVersionColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummeryResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowSummeryResultModel, String> p) {
				return p.getValue().workflowMinorVersion;
			}
		});
        
        aliasColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummeryResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowSummeryResultModel, String> p) {
				return p.getValue().alias;
			}
		});
        
        statusColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummeryResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowSummeryResultModel, String> p) {
				return p.getValue().status;
			}
		});
        
        countColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowSummeryResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowSummeryResultModel, String> p) {
				return p.getValue().count;
			}
		});
        
        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

	@Override
	public void applyFilter(WorkflowSummeryFilterModel filter) {
		ObservableList<WorkflowSummeryResultModel> content = FXCollections.observableList(new ArrayList<WorkflowSummeryResultModel>());;
		content.addAll(copperDataProvider.getWorkflowSummery(
				filter.workflowclass.getValue(), filter.majorVersion.getValue(), filter.minorVersion.getValue()));
		resultTable.setItems(content);
	}
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowSummeryResult.fxml");
	}

}
