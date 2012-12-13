package de.scoopgmbh.copper.gui.ui.dynamicworkflow;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.model.WorkflowInstancesInfoModel;

public class DynamicWorkflowController implements Initializable, FxmlController {
	GuiCopperDataProvider copperDataProvider;

    @FXML
    private TableView<WorkflowInstancesInfoModel> dynamicWorkflowTable;

    @FXML //  fx:id="idColum"
    private TableColumn<WorkflowInstancesInfoModel,String> idColum; 

    @FXML 
    private MenuBar menuBar; 

    @FXML
    private TableColumn<WorkflowInstancesInfoModel,String> stateColumn; 
	
	public DynamicWorkflowController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}

	@Override
	public void initialize(final URL url, final ResourceBundle rb) {
		
		ObservableList<WorkflowInstancesInfoModel> content = FXCollections.observableList(new ArrayList<WorkflowInstancesInfoModel>());;
		content.addAll(copperDataProvider.getWorkflowInstancesInfos(1,1));
		dynamicWorkflowTable.setItems(content);
		 
//		idColum.setCellValueFactory(new PropertyValueFactory<WorkflowInstancesInfoModel,String>("id"));
//		stateColumn.setCellValueFactory(new PropertyValueFactory<WorkflowInstancesInfoModel,String>("state"));
		
		
		idColum.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstancesInfoModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowInstancesInfoModel, String> p) {
				return p.getValue().getId();
			}
		});

		stateColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstancesInfoModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowInstancesInfoModel, String> p) {
				return p.getValue().getState();
			}
		});
		
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("DynamicWorkflow.fxml");
	}
}
