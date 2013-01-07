package de.scoopgmbh.copper.gui.ui.workflowinstance.result;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.context.FormContext;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;

public class WorkflowInstanceResultController implements Initializable, FilterResultController<WorkflowInstanceFilterModel>, FxmlController {
	private final GuiCopperDataProvider copperDataProvider;
	private final FormContext formcontext;
	
	public WorkflowInstanceResultController(GuiCopperDataProvider copperDataProvider, FormContext formcontext) {
		super();
		this.copperDataProvider = copperDataProvider;
		this.formcontext = formcontext;
	}

    @FXML //  fx:id="idColumn"
    private TableColumn<WorkflowInstanceResultModel, String> idColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="prioritynColumn"
    private TableColumn<WorkflowInstanceResultModel, String> prioritynColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="processorPoolColumn"
    private TableColumn<WorkflowInstanceResultModel, String> processorPoolColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="resultTable"
    private TableView<WorkflowInstanceResultModel> resultTable; // Value injected by FXMLLoader

    @FXML //  fx:id="stateColumn"
    private TableColumn<WorkflowInstanceResultModel, WorkflowInstanceState> stateColumn; // Value injected by FXMLLoader

    @FXML //  fx:id="timeoutColumn"
    private TableColumn<WorkflowInstanceResultModel, Date> timeoutColumn; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert idColumn != null : "fx:id=\"idColumn\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert prioritynColumn != null : "fx:id=\"prioritynColumn\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert processorPoolColumn != null : "fx:id=\"processorPoolColumn\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert stateColumn != null : "fx:id=\"stateColumn\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert timeoutColumn != null : "fx:id=\"timeoutColumn\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";


        idColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowInstanceResultModel, String> p) {
				return p.getValue().id;
			}
		});
        
        prioritynColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowInstanceResultModel, String> p) {
				return p.getValue().priority.asString();
			}
		});

        processorPoolColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowInstanceResultModel, String> p) {
				return p.getValue().processorPoolId;
			}
		});
        
        stateColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, WorkflowInstanceState>, ObservableValue<WorkflowInstanceState>>() {
			public ObservableValue<WorkflowInstanceState> call(
					CellDataFeatures<WorkflowInstanceResultModel, WorkflowInstanceState> p) {
				return p.getValue().state;
			}
		});
        
        timeoutColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, Date>, ObservableValue<Date>>() {
			public ObservableValue<Date> call(
					CellDataFeatures<WorkflowInstanceResultModel, Date> p) {
				return p.getValue().timeout;
			}
		});
        timeoutColumn.setCellFactory(new Callback<TableColumn<WorkflowInstanceResultModel, Date>, TableCell<WorkflowInstanceResultModel, Date>>() {
            @Override
            public TableCell<WorkflowInstanceResultModel, Date> call(TableColumn<WorkflowInstanceResultModel, Date> param) {
                TableCell<WorkflowInstanceResultModel, Date> cell = new TableCell<WorkflowInstanceResultModel, Date>() {
                	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    @Override
                    public void updateItem(final Date date, boolean empty) {
                        if (date != null) {
							setText(simpleDateFormat.format(date));
                        }
                    }
                };
                return cell;
            }
        });
        
        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        resultTable.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
		            if(mouseEvent.getClickCount() == 2 && !resultTable.getSelectionModel().isEmpty()){
		            	formcontext.createWorkflowInstanceDetailForm(resultTable.getSelectionModel().getSelectedItem().id.getValue()).show();
		            }
		        }
			}
		});
    }

	@Override
	public void applyFilter(WorkflowInstanceFilterModel filter) {
		ObservableList<WorkflowInstanceResultModel> content = FXCollections.observableList(new ArrayList<WorkflowInstanceResultModel>());;
		content.addAll(copperDataProvider.getWorkflowInstanceList(filter.state.getValue(), filter.priority.getValue()));
		resultTable.setItems(content);
	}
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowInstanceResult.fxml");
	}

}
