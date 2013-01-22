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
package de.scoopgmbh.copper.gui.context;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.BorderPaneShowFormStrategie;
import de.scoopgmbh.copper.gui.form.EmptyShowFormStrategie;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.FormCreator;
import de.scoopgmbh.copper.gui.form.FormGroup;
import de.scoopgmbh.copper.gui.form.FxmlForm;
import de.scoopgmbh.copper.gui.form.ShowFormStrategy;
import de.scoopgmbh.copper.gui.form.TabPaneShowFormStrategie;
import de.scoopgmbh.copper.gui.form.enginefilter.EngineFilterAbleform;
import de.scoopgmbh.copper.gui.form.filter.EmptyFilterController;
import de.scoopgmbh.copper.gui.form.filter.EmptyFilterModel;
import de.scoopgmbh.copper.gui.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.gui.form.filter.FilterController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.audittrail.filter.AuditTrailFilterController;
import de.scoopgmbh.copper.gui.ui.audittrail.filter.AuditTrailFilterModel;
import de.scoopgmbh.copper.gui.ui.audittrail.result.AuditTrailResultController;
import de.scoopgmbh.copper.gui.ui.audittrail.result.AuditTrailResultModel;
import de.scoopgmbh.copper.gui.ui.dashboard.result.DashboardResultController;
import de.scoopgmbh.copper.gui.ui.dashboard.result.DashboardResultModel;
import de.scoopgmbh.copper.gui.ui.dashboard.result.engine.ProcessingEngineController;
import de.scoopgmbh.copper.gui.ui.dashboard.result.pool.ProccessorPoolController;
import de.scoopgmbh.copper.gui.ui.load.filter.EngineLoadFilterController;
import de.scoopgmbh.copper.gui.ui.load.filter.EngineLoadFilterModel;
import de.scoopgmbh.copper.gui.ui.load.result.EngineLoadResultController;
import de.scoopgmbh.copper.gui.ui.settings.SettingsController;
import de.scoopgmbh.copper.gui.ui.settings.SettingsModel;
import de.scoopgmbh.copper.gui.ui.sql.filter.SqlFilterController;
import de.scoopgmbh.copper.gui.ui.sql.filter.SqlFilterModel;
import de.scoopgmbh.copper.gui.ui.sql.result.SqlResultController;
import de.scoopgmbh.copper.gui.ui.sql.result.SqlResultModel;
import de.scoopgmbh.copper.gui.ui.systemresource.filter.ResourceFilterController;
import de.scoopgmbh.copper.gui.ui.systemresource.filter.ResourceFilterModel;
import de.scoopgmbh.copper.gui.ui.systemresource.result.RessourceResultController;
import de.scoopgmbh.copper.gui.ui.workflowclasssesctree.WorkflowClassesTreeController;
import de.scoopgmbh.copper.gui.ui.workflowclasssesctree.WorkflowClassesTreeForm;
import de.scoopgmbh.copper.gui.ui.workflowinstance.filter.WorkflowInstanceFilterController;
import de.scoopgmbh.copper.gui.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowinstance.result.WorkflowInstanceResultController;
import de.scoopgmbh.copper.gui.ui.workflowinstance.result.WorkflowInstanceResultModel;
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterController;
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowsummery.result.WorkflowSummeryResultController;
import de.scoopgmbh.copper.gui.ui.workflowsummery.result.WorkflowSummeryResultModel;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterController;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultController;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultModel;
import de.scoopgmbh.copper.gui.util.CodeMirrorFormatter;
import de.scoopgmbh.copper.gui.util.MessageProvider;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessorPoolInfo;
import de.scoopgmbh.copper.monitor.adapter.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummery;

public class FormContext {
	private final TabPane mainTabPane;
	private final BorderPane mainPane;
	private FormGroup formGroup;
	private final MessageProvider messageProvider;
	private final SettingsModel settingsModelSinglton;
	private final CodeMirrorFormatter codeMirrorFormatterSingelton = new CodeMirrorFormatter();

	public TabPane getMainTabPane() {
		return mainTabPane;
	}

	GuiCopperDataProvider guiCopperDataProvider;
	private FxmlForm<SettingsController> settingsForSingleton;
	private FilterAbleForm<EmptyFilterModel, DashboardResultModel> dasboardFormSingleton;
	public FormContext(BorderPane mainPane, GuiCopperDataProvider guiCopperDataProvider, MessageProvider messageProvider, SettingsModel settingsModelSinglton) {
		this.mainTabPane = new TabPane();
		this.messageProvider = messageProvider;
		this.guiCopperDataProvider = guiCopperDataProvider;
		this.mainPane = mainPane;
		this.settingsModelSinglton = settingsModelSinglton;
		
		ArrayList<FormCreator> maingroup = new ArrayList<>();
		maingroup.add(new FormCreator(messageProvider.getText("dashboard.title")) {
			@Override
			public Form<?> createForm() {
				return createDashboardForm();
			}
		});
		maingroup.add(new FormCreator(messageProvider.getText("workflowsummery.title")) {
			@Override
			public Form<?> createForm() {
				return createWorkflowOverviewForm();
			}
		});
		maingroup.add(new FormCreator(messageProvider.getText("workflowInstance.title")) {
			@Override
			public Form<?> createForm() {
				return createWorkflowInstanceForm();
			}
		});
		maingroup.add(new FormCreator(messageProvider.getText("audittrail.title")) {
			@Override
			public Form<?> createForm() {
				return createAudittrailForm();
			}
		});
		
		ArrayList<FormCreator> loadgroup = new ArrayList<>();
		loadgroup.add(new FormCreator(messageProvider.getText("engineLoad.title")) {
			@Override
			public Form<?> createForm() {
				return createEngineLoadForm();
			}
		});
		loadgroup.add(new FormCreator(messageProvider.getText("resource.title")) {
			@Override
			public Form<?> createForm() {
				return createRessourceForm();
			}
		});
		maingroup.add(new FormGroup(messageProvider.getText("loadGroup.title"),loadgroup));
		
		maingroup.add(new FormCreator(messageProvider.getText("sql.title")) {
			@Override
			public Form<?> createForm() {
				return createSqlForm();
			}
		});
		maingroup.add(new FormCreator(messageProvider.getText("settings.title")) {
			@Override
			public Form<?> createForm() {
				return createSettingsForm();
			}
		});
		formGroup = new FormGroup("",maingroup);
	}
	
	public void setupGUIStructure(){
		mainPane.setCenter(mainTabPane);
		mainPane.setTop(createToolbar());
	}

	public MenuBar createMenueBar(){
		final MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(formGroup.createMenu());
		return menuBar;
	}
	
	public ToolBar createToolbar() {
		ToolBar toolBar = new ToolBar();
		Region spacer = new Region();
		spacer.getStyleClass().setAll("spacer");

		HBox buttonBar = new HBox();
		buttonBar.getStyleClass().setAll("segmented-button-bar");

		List<ButtonBase> buttons = formGroup.createButtonList();
		buttons.get(0).getStyleClass().addAll("first");
		buttons.get(buttons.size() - 1).getStyleClass().addAll("last", "capsule");

		buttonBar.getChildren().addAll(buttons);
		toolBar.getItems().addAll(spacer, buttonBar);

		return toolBar;
	}
	
	public WorkflowClassesTreeForm createWorkflowClassesTreeForm(WorkflowSummeryFilterController filterController){
		return new WorkflowClassesTreeForm("",new EmptyShowFormStrategie(),new WorkflowClassesTreeController(guiCopperDataProvider,filterController));
	}
	
	
	public FilterAbleForm<WorkflowSummeryFilterModel,WorkflowSummeryResultModel> createWorkflowOverviewForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowSummeryFilterModel> fCtrl = new WorkflowSummeryFilterController(this); 
		FxmlForm<FilterController<WorkflowSummeryFilterModel>> filterForm = new FxmlForm<>(fCtrl, messageProvider);
		
		FilterResultController<WorkflowSummeryFilterModel,WorkflowSummeryResultModel> resCtrl = new WorkflowSummeryResultController(guiCopperDataProvider,this);
		FxmlForm<FilterResultController<WorkflowSummeryFilterModel,WorkflowSummeryResultModel>> resultForm = new FxmlForm<>(resCtrl, messageProvider);
		
		return new EngineFilterAbleform<>(messageProvider.getText("workflowsummery.title"),messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<WorkflowInstanceFilterModel,WorkflowInstanceResultModel> createWorkflowInstanceForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowInstanceFilterModel> fCtrl = new WorkflowInstanceFilterController(); 
		FxmlForm<FilterController<WorkflowInstanceFilterModel>> filterForm = new FxmlForm<>(fCtrl, messageProvider);
		
		FilterResultController<WorkflowInstanceFilterModel,WorkflowInstanceResultModel> resCtrl = new WorkflowInstanceResultController(guiCopperDataProvider,this);
		FxmlForm<FilterResultController<WorkflowInstanceFilterModel,WorkflowInstanceResultModel>> resultForm = new FxmlForm<>(resCtrl, messageProvider);
		
		return new EngineFilterAbleform<>(messageProvider.getText("workflowInstance.title"),messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<AuditTrailFilterModel,AuditTrailResultModel> createAudittrailForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<AuditTrailFilterModel> fCtrl = new AuditTrailFilterController(); 
		FxmlForm<FilterController<AuditTrailFilterModel>> filterForm = new FxmlForm<>(fCtrl, messageProvider);
		
		FilterResultController<AuditTrailFilterModel,AuditTrailResultModel> resCtrl = new AuditTrailResultController(guiCopperDataProvider, settingsModelSinglton, codeMirrorFormatterSingelton);
		FxmlForm<FilterResultController<AuditTrailFilterModel,AuditTrailResultModel>> resultForm = new FxmlForm<>(resCtrl, messageProvider);
		
		return new FilterAbleForm<>(messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel> createWorkflowInstanceDetailForm(String workflowInstanceId){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowInstanceDetailFilterModel> fCtrl = new WorkflowInstanceDetailFilterController(workflowInstanceId); 
		
		FxmlForm<FilterController<WorkflowInstanceDetailFilterModel>> filterForm = new FxmlForm<>(fCtrl, messageProvider);
		
		FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>> resultForm = createWorkflowinstanceDetailResultForm(new EmptyShowFormStrategie());
		
		FilterAbleForm<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel> filterAbleForm = new FilterAbleForm<>(messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
		filterAbleForm.dynamicTitleProperty().bind(new SimpleStringProperty("Details Id:").concat(fCtrl.getFilter().workflowInstanceId));
		return filterAbleForm;
	}
	
	public FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>> createWorkflowinstanceDetailResultForm(ShowFormStrategy<?> showFormStrategy) {
		FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel> resCtrl = new WorkflowInstanceDetailResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel>> resultForm = new FxmlForm<>("workflowInstanceDetail.title",
				resCtrl, messageProvider, showFormStrategy );
		return resultForm;
	}

	public FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>> createWorkflowinstanceDetailResultForm(BorderPane target) {
		return createWorkflowinstanceDetailResultForm(new BorderPaneShowFormStrategie(target));
	}
	
	public FilterAbleForm<EngineLoadFilterModel,WorkflowStateSummery> createEngineLoadForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<EngineLoadFilterModel> fCtrl = new EngineLoadFilterController(); 
		FxmlForm<FilterController<EngineLoadFilterModel>> filterForm = new FxmlForm<>(fCtrl, messageProvider);
		
		FilterResultController<EngineLoadFilterModel,WorkflowStateSummery> resCtrl = new EngineLoadResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<EngineLoadFilterModel,WorkflowStateSummery>> resultForm = new FxmlForm<>(resCtrl, messageProvider);
		
		return new EngineFilterAbleform<>(messageProvider.getText("engineLoad.title"),messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public Form<SettingsController> createSettingsForm(){
		if (settingsForSingleton==null){
			settingsForSingleton = new FxmlForm<>("",new SettingsController(settingsModelSinglton), messageProvider,  new TabPaneShowFormStrategie(mainTabPane));
		}
		return settingsForSingleton;
	}
	
	public FilterAbleForm<SqlFilterModel,SqlResultModel> createSqlForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<SqlFilterModel> fCtrl = new SqlFilterController(codeMirrorFormatterSingelton); 
		FxmlForm<FilterController<SqlFilterModel>> filterForm = new FxmlForm<>(fCtrl, messageProvider);
		
		FilterResultController<SqlFilterModel,SqlResultModel> resCtrl = new SqlResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<SqlFilterModel,SqlResultModel>> resultForm = new FxmlForm<>(resCtrl, messageProvider);
		
		return new FilterAbleForm<>(messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<ResourceFilterModel,SystemResourcesInfo> createRessourceForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<ResourceFilterModel> fCtrl = new ResourceFilterController(); 
		FxmlForm<FilterController<ResourceFilterModel>> filterForm = new FxmlForm<>(fCtrl, messageProvider);
		
		FilterResultController<ResourceFilterModel,SystemResourcesInfo> resCtrl = new RessourceResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<ResourceFilterModel,SystemResourcesInfo>> resultForm = new FxmlForm<>(resCtrl, messageProvider);
		
		return new FilterAbleForm<>(messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<EmptyFilterModel,DashboardResultModel> createDashboardForm(){
		if (dasboardFormSingleton==null){
			FilterController<EmptyFilterModel> fCtrl = new EmptyFilterController(); 
			FxmlForm<FilterController<EmptyFilterModel>> filterForm = new FxmlForm<>(fCtrl, messageProvider);
			
			FilterResultController<EmptyFilterModel,DashboardResultModel> resCtrl = new DashboardResultController(guiCopperDataProvider,this);
			FxmlForm<FilterResultController<EmptyFilterModel,DashboardResultModel>> resultForm = new FxmlForm<>(resCtrl, messageProvider);
			
			dasboardFormSingleton = new FilterAbleForm<>(messageProvider,
					new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
		}
		return dasboardFormSingleton;
	}
	
	public Form<ProccessorPoolController> createPoolForm(TabPane tabPane, ProcessingEngineInfo engine, ProcessorPoolInfo pool, DashboardResultModel model){
		return new FxmlForm<>(pool.getId(), new ProccessorPoolController(engine,pool,model), messageProvider, new TabPaneShowFormStrategie(tabPane));
	}
	
	public Form<ProcessingEngineController> createEngineForm(TabPane tabPane, ProcessingEngineInfo engine, DashboardResultModel model){
		return new FxmlForm<>(engine.getId(), new ProcessingEngineController(engine,model,this), messageProvider, new TabPaneShowFormStrategie(tabPane));
	}
}
