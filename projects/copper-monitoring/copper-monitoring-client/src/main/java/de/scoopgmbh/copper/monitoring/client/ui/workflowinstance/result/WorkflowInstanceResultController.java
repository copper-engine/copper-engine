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
package de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.result;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.context.FormContext;
import de.scoopgmbh.copper.monitoring.client.form.FxmlController;
import de.scoopgmbh.copper.monitoring.client.form.FxmlForm;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultController;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.filter.AuditTrailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.result.AuditTrailResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultModel;
import de.scoopgmbh.copper.monitoring.client.util.ComponentUtil;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceState;

public class WorkflowInstanceResultController implements Initializable, FilterResultController<WorkflowInstanceFilterModel,WorkflowInstanceResultModel>, FxmlController {
	
	
	
	public static final class DetailLoadService extends Service<Void> {
		private WorkflowInstanceResultModel workflowInstanceResultModel;
		private StackPane stackDetailPane;
		private FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel>> detailForm;

		public DetailLoadService(WorkflowInstanceResultModel workflowInstanceResultModel,StackPane stackDetailPane, FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel>> detailForm) {
			this.workflowInstanceResultModel = workflowInstanceResultModel;
			this.stackDetailPane = stackDetailPane;
			this.detailForm = detailForm;
		}
		
		public WorkflowInstanceResultModel getWorkflowInstanceResultModel() {
			return workflowInstanceResultModel;
		}

		public void setWorkflowInstanceResultModel(WorkflowInstanceResultModel workflowInstanceResultModel) {
			this.workflowInstanceResultModel = workflowInstanceResultModel;
		}

		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				final ProgressIndicator indicator = ComponentUtil.createProgressIndicator();
				private WorkflowInstanceDetailFilterModel filter;
				private List<WorkflowInstanceDetailResultModel> result;
				@Override
				protected Void call() throws Exception {
					Platform.runLater(new Runnable() {
		                 @Override public void run() {
		                	 stackDetailPane.getChildren().add(indicator);
		                 }
		             });
					filter = new WorkflowInstanceDetailFilterModel();
					filter.workflowInstanceId.setValue(workflowInstanceResultModel.id.getValue());
					filter.filteredWithworkflowInstanceInfo=workflowInstanceResultModel;
					result = detailForm.getController().applyFilterInBackgroundThread(filter);
					return null;
				}
				
				@Override 
				protected void succeeded() {
					detailForm.getController().showFilteredResult(result, filter);
					stackDetailPane.getChildren().remove(indicator);
					if (getException()!=null){
						throw new RuntimeException(this.getException());
					}
					super.succeeded();
				}
				
				@Override 
				protected void failed() {
					stackDetailPane.getChildren().remove(indicator);
					if (getException()!=null){
						throw new RuntimeException(this.getException());
					}
					super.failed();
				}
				
			};
		}
	}

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

    @FXML //  fx:id="timeoutColumn"
    private TableColumn<WorkflowInstanceResultModel, Date> lastActivityTimestamp;

    @FXML //  fx:id="timeoutColumn"
    private TableColumn<WorkflowInstanceResultModel, String> overallLifetimeInMs;

    @FXML //  fx:id="timeoutColumn"
    private TableColumn<WorkflowInstanceResultModel, Date> startTime;

    @FXML //  fx:id="timeoutColumn"
    private TableColumn<WorkflowInstanceResultModel, Date> finishTime;

    @FXML //  fx:id="timeoutColumn"
    private TableColumn<WorkflowInstanceResultModel, Date> lastErrorTime;

    @FXML //  fx:id="timeoutColumn"
    private TableColumn<WorkflowInstanceResultModel, String> errorInfos;

    @FXML //  fx:id="timeoutColumn"
    private BorderPane detailPane;
    
    @FXML //  fx:id="timeoutColumn"
    private StackPane stackDetailPane;
	private FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel>> detailForm;
	private DetailLoadService service;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert detailPane != null : "fx:id=\"detailPane\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert errorInfos != null : "fx:id=\"errorInfos\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert finishTime != null : "fx:id=\"finishTime\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert idColumn != null : "fx:id=\"idColumn\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert lastActivityTimestamp != null : "fx:id=\"lastActivityTimestamp\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert lastErrorTime != null : "fx:id=\"lastErrorTime\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert overallLifetimeInMs != null : "fx:id=\"overallLifetimeInMs\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert prioritynColumn != null : "fx:id=\"prioritynColumn\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert processorPoolColumn != null : "fx:id=\"processorPoolColumn\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert stackDetailPane != null : "fx:id=\"stackDetailPane\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert startTime != null : "fx:id=\"startTime\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert stateColumn != null : "fx:id=\"stateColumn\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";
        assert timeoutColumn != null : "fx:id=\"timeoutColumn\" was not injected: check your FXML file 'WorkflowInstanceResult.fxml'.";


        idColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowInstanceResultModel, String> p) {
				return p.getValue().id;
			}
		});
        
        prioritynColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowInstanceResultModel, String> p) {
				return p.getValue().priority.asString();
			}
		});

        processorPoolColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowInstanceResultModel, String> p) {
				return p.getValue().processorPoolId;
			}
		});
        
        stateColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, WorkflowInstanceState>, ObservableValue<WorkflowInstanceState>>() {
			@Override
			public ObservableValue<WorkflowInstanceState> call(
					CellDataFeatures<WorkflowInstanceResultModel, WorkflowInstanceState> p) {
				return p.getValue().state;
			}
		});
        
        timeoutColumn.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, Date>, ObservableValue<Date>>() {
			@Override
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
        
        
        lastActivityTimestamp.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, Date>, ObservableValue<Date>>() {
			@Override
			public ObservableValue<Date> call(
					CellDataFeatures<WorkflowInstanceResultModel, Date> p) {
				return p.getValue().lastActivityTimestamp;
			}
		});
        overallLifetimeInMs.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowInstanceResultModel, String> p) {
				return p.getValue().overallLifetimeInMs.asString();
			}
		});
        startTime.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, Date>, ObservableValue<Date>>() {
			@Override
			public ObservableValue<Date> call(
					CellDataFeatures<WorkflowInstanceResultModel, Date> p) {
				return p.getValue().startTime;
			}
		});
        finishTime.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, Date>, ObservableValue<Date>>() {
			@Override
			public ObservableValue<Date> call(
					CellDataFeatures<WorkflowInstanceResultModel, Date> p) {
				return p.getValue().finishTime;
			}
		});
        lastErrorTime.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, Date>, ObservableValue<Date>>() {
			@Override
			public ObservableValue<Date> call(
					CellDataFeatures<WorkflowInstanceResultModel, Date> p) {
				return p.getValue().lastErrorTime;
			}
		});
        errorInfos.setCellValueFactory(new Callback<CellDataFeatures<WorkflowInstanceResultModel, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<WorkflowInstanceResultModel, String> p) {
				return p.getValue().errorInfos;
			}
		});
        errorInfos.setCellFactory(new Callback<TableColumn<WorkflowInstanceResultModel,String>, TableCell<WorkflowInstanceResultModel,String>>() {
			@Override
			public TableCell<WorkflowInstanceResultModel, String> call(TableColumn<WorkflowInstanceResultModel, String> param) {
				return new TextFieldTableCell<WorkflowInstanceResultModel, String>(new StringConverter<String>() {
					@Override
					public String fromString(String string) {
						return string;
					}

					@Override
					public String toString(String object) {
						return abbreviate(object,40);
					}
				});
			}
		});

        resultTable.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
		            if(mouseEvent.getClickCount() == 2 && !resultTable.getSelectionModel().isEmpty()){
		            	formcontext.createWorkflowInstanceDetailForm(resultTable.getSelectionModel().getSelectedItem().id.getValue()).show();
		            }
		            if(mouseEvent.getClickCount() == 1 && !resultTable.getSelectionModel().isEmpty()){
		            	showDetails(resultTable.getSelectionModel().getSelectedItem());
		            }
		            
		        }
			}
		});
        
        ContextMenu contextMenu = new ContextMenu();
        MenuItem detailMenuItem = new MenuItem("Details in new tab");
        detailMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				formcontext.createWorkflowInstanceDetailForm(resultTable.getSelectionModel().getSelectedItem().id.getValue()).show();
			}
		});
        detailMenuItem.disableProperty().bind(resultTable.getSelectionModel().selectedItemProperty().isNull());
        contextMenu.getItems().add(detailMenuItem);
        MenuItem audittrailMenuItem = new MenuItem("Audittrail");
        audittrailMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FilterAbleForm<AuditTrailFilterModel,AuditTrailResultModel> audittrailForm = formcontext.createAudittrailForm();
				audittrailForm.getFilter().workflowInstanceId.set(resultTable.getSelectionModel().getSelectedItem().id.getValue());
				audittrailForm.show();
			}
		});
        audittrailMenuItem.disableProperty().bind(resultTable.getSelectionModel().selectedItemProperty().isNull());
        contextMenu.getItems().add(audittrailMenuItem);
        
        resultTable.setContextMenu(contextMenu);
        
        
     
 
		detailForm = formcontext.createWorkflowinstanceDetailResultForm(detailPane);
		detailForm.show();
    }
    
    public static String abbreviate(String str, int maxWidth) {
        if (null == str) {
            return null;
        }
        if (str.length() <= maxWidth) {
            return str;
        }
        return str.substring(0, maxWidth) + "...";
    }
    
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowInstanceResult.fxml");
	}

	@Override
	public void showFilteredResult(List<WorkflowInstanceResultModel> filteredResult, WorkflowInstanceFilterModel usedFilter) {
		ObservableList<WorkflowInstanceResultModel> content = FXCollections.observableList(new ArrayList<WorkflowInstanceResultModel>());;
		content.addAll(filteredResult);
		resultTable.setItems(content);
	}

	@Override
	public List<WorkflowInstanceResultModel> applyFilterInBackgroundThread(WorkflowInstanceFilterModel filter) {
		return copperDataProvider.getWorkflowInstanceList(filter);
	}
	
	@Override
	public boolean canLimitResult() {
		return true;
	}
	
	@Override
	public void clear() {
		resultTable.getItems().clear();
	}
	
	private void showDetails(final WorkflowInstanceResultModel workflowInstanceResultModel){

		

		if (service==null) {
			service = new DetailLoadService(workflowInstanceResultModel,stackDetailPane,detailForm);
		}
		
		if (!service.isRunning()){
			service.reset();
			service.setWorkflowInstanceResultModel(workflowInstanceResultModel);
			service.start();
		}
	}

}
