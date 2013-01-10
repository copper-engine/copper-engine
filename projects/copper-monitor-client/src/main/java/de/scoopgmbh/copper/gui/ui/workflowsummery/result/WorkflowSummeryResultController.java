package de.scoopgmbh.copper.gui.ui.workflowsummery.result;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.context.FormContext;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowinstance.result.WorkflowInstanceResultModel;
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterModel;

public class WorkflowSummeryResultController implements Initializable, FilterResultController<WorkflowSummeryFilterModel,WorkflowSummeryResultModel>, FxmlController {
	GuiCopperDataProvider copperDataProvider;

	private FormContext formcontext;
	public WorkflowSummeryResultController(GuiCopperDataProvider copperDataProvider,FormContext formcontext) {
		super();
		this.copperDataProvider = copperDataProvider;
		this.formcontext = formcontext;
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
				return p.getValue().workflowclass;
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
        
        resultTable.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
		            if(mouseEvent.getClickCount() == 2 && !resultTable.getSelectionModel().isEmpty()){
		            	FilterAbleForm<WorkflowInstanceFilterModel,WorkflowInstanceResultModel> workflowInstanceForm = formcontext.createWorkflowInstanceForm();
						workflowInstanceForm.getFilter().workflowSummeryFilterModel.setAllFrom(getSelectedEntry());
						workflowInstanceForm.show();
		            }
		        }
			}
		});
        ContextMenu contextMenu = new ContextMenu();
        MenuItem detailMenuItem = new MenuItem("Details");
        detailMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FilterAbleForm<WorkflowInstanceFilterModel,WorkflowInstanceResultModel> workflowInstanceForm = formcontext.createWorkflowInstanceForm();
				workflowInstanceForm.getFilter().workflowSummeryFilterModel.setAllFrom(getSelectedEntry());
				workflowInstanceForm.show();
			}
		});
        detailMenuItem.disableProperty().bind(resultTable.getSelectionModel().selectedItemProperty().isNull());
        contextMenu.getItems().add(detailMenuItem);
        
        resultTable.setContextMenu(contextMenu);
    }
    
    private WorkflowSummeryResultModel getSelectedEntry(){
    	return resultTable.getSelectionModel().getSelectedItem();
    }

	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowSummeryResult.fxml");
	}

	@Override
	public void showFilteredResult(List<WorkflowSummeryResultModel> filteredResult, WorkflowSummeryFilterModel usedFilter) {
		ObservableList<WorkflowSummeryResultModel> content = FXCollections.observableList(new ArrayList<WorkflowSummeryResultModel>());;
		content.addAll(filteredResult);
		resultTable.setItems(content);
	}

	@Override
	public List<WorkflowSummeryResultModel> applyFilterInBackgroundThread(WorkflowSummeryFilterModel filter) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return copperDataProvider.getWorkflowSummery(
				filter.workflowclass.getValue(), filter.workflowMajorVersion.getValue(), filter.workflowMinorVersion.getValue());
	}

}
