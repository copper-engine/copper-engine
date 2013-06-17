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
package de.scoopgmbh.copper.monitoring.client.context;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.context.FormBuilder.EngineFormBuilder;
import de.scoopgmbh.copper.monitoring.client.form.BorderPaneShowFormStrategie;
import de.scoopgmbh.copper.monitoring.client.form.EmptyShowFormStrategie;
import de.scoopgmbh.copper.monitoring.client.form.Form;
import de.scoopgmbh.copper.monitoring.client.form.FormCreator;
import de.scoopgmbh.copper.monitoring.client.form.FormGroup;
import de.scoopgmbh.copper.monitoring.client.form.FxmlForm;
import de.scoopgmbh.copper.monitoring.client.form.ShowFormStrategy;
import de.scoopgmbh.copper.monitoring.client.form.TabPaneShowFormStrategie;
import de.scoopgmbh.copper.monitoring.client.form.enginefilter.EngineFilterAbleForm;
import de.scoopgmbh.copper.monitoring.client.form.enginefilter.EngineFilterModelBase;
import de.scoopgmbh.copper.monitoring.client.form.filter.EmptyFilterModel;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterController;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultController;
import de.scoopgmbh.copper.monitoring.client.form.filter.GenericFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.fiter.AdapterMonitoringFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.fiter.AdapterMonitoringFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.AdapterMonitoringResultController;
import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.AdapterMonitoringResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.filter.AuditTrailFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.filter.AuditTrailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.result.AuditTrailResultController;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.result.AuditTrailResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.custommeasurepoint.filter.CustomMeasurePointFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.custommeasurepoint.filter.CustomMeasurePointFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.custommeasurepoint.result.CustomMeasurePointResultController;
import de.scoopgmbh.copper.monitoring.client.ui.dashboard.result.DashboardDependencyFactory;
import de.scoopgmbh.copper.monitoring.client.ui.dashboard.result.DashboardResultController;
import de.scoopgmbh.copper.monitoring.client.ui.dashboard.result.DashboardResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.dashboard.result.engine.ProcessingEngineController;
import de.scoopgmbh.copper.monitoring.client.ui.dashboard.result.pool.ProccessorPoolController;
import de.scoopgmbh.copper.monitoring.client.ui.hotfix.HotfixController;
import de.scoopgmbh.copper.monitoring.client.ui.hotfix.HotfixModel;
import de.scoopgmbh.copper.monitoring.client.ui.load.filter.EngineLoadFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.load.filter.EngineLoadFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.load.result.EngineLoadResultController;
import de.scoopgmbh.copper.monitoring.client.ui.logs.filter.LogsFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.logs.filter.LogsFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.logs.result.LogsResultController;
import de.scoopgmbh.copper.monitoring.client.ui.logs.result.LogsResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.measurepoint.result.MeasurePointResultController;
import de.scoopgmbh.copper.monitoring.client.ui.message.filter.MessageFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.message.filter.MessageFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.message.result.MessageResultController;
import de.scoopgmbh.copper.monitoring.client.ui.message.result.MessageResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.repository.filter.WorkflowRepositoryFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.repository.filter.WorkflowRepositoryFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.repository.result.WorkflowRepositoryDependencyFactory;
import de.scoopgmbh.copper.monitoring.client.ui.repository.result.WorkflowRepositoryResultController;
import de.scoopgmbh.copper.monitoring.client.ui.settings.SettingsController;
import de.scoopgmbh.copper.monitoring.client.ui.settings.SettingsModel;
import de.scoopgmbh.copper.monitoring.client.ui.sql.filter.SqlFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.sql.filter.SqlFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.sql.result.SqlResultController;
import de.scoopgmbh.copper.monitoring.client.ui.sql.result.SqlResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.systemresource.filter.ResourceFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.systemresource.filter.ResourceFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.systemresource.result.RessourceResultController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowclasssesctree.WorkflowClassesTreeController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowclasssesctree.WorkflowClassesTreeController.DisplayWorkflowClassesModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowclasssesctree.WorkflowClassesTreeForm;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.filter.WorkflowInstanceFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.result.WorkflowInstanceDependencyFactory;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.result.WorkflowInstanceResultController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.result.WorkflowInstanceResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowsummary.filter.WorkflowSummaryFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowsummary.filter.WorkflowSummaryFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowsummary.result.WorkflowSummaryResultController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowsummary.result.WorkflowSummaryResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultController;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultModel;
import de.scoopgmbh.copper.monitoring.client.util.CodeMirrorFormatter;
import de.scoopgmbh.copper.monitoring.client.util.MessageKey;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;
import de.scoopgmbh.copper.monitoring.client.util.WorkflowVersion;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitoring.core.model.ProcessorPoolInfo;
import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowStateSummary;

public class FormContext implements DashboardDependencyFactory, WorkflowInstanceDependencyFactory, WorkflowRepositoryDependencyFactory{
	protected final TabPane mainTabPane;
	protected final BorderPane mainPane;
	protected final FormGroup formGroup;
	protected final MessageProvider messageProvider;
	protected final GuiCopperDataProvider guiCopperDataProvider;
	protected final SettingsModel settingsModelSinglton;
	protected final CodeMirrorFormatter codeMirrorFormatterSingelton = new CodeMirrorFormatter();

	private FxmlForm<SettingsController> settingsForSingleton;
	private FxmlForm<HotfixController> hotfixFormSingleton;
	private FilterAbleForm<EmptyFilterModel, DashboardResultModel> dasboardFormSingleton;
	public FormContext(BorderPane mainPane, GuiCopperDataProvider guiCopperDataProvider, MessageProvider messageProvider, SettingsModel settingsModelSinglton) {
		this.mainTabPane = new TabPane();
		this.messageProvider = messageProvider;
		this.guiCopperDataProvider = guiCopperDataProvider;
		this.mainPane = mainPane;
		this.settingsModelSinglton = settingsModelSinglton;
		
		ArrayList<FormCreator> maingroup = new ArrayList<FormCreator>();
		maingroup.add(new FormCreator(messageProvider.getText(MessageKey.dashboard_title)) {
			@Override
			public Form<?> createForm() {
				return createDashboardForm();
			}
		});

		maingroup.add(new FormGroup(messageProvider.getText(MessageKey.workflowGroup_title),createWorkflowGroup()));
		
		maingroup.add(new FormCreator(messageProvider.getText(MessageKey.adapterMonitoring_title)) {
			@Override
			public Form<?> createForm() {
				return createAdapterMonitoringForm();
			}
		});
		
		maingroup.add(new FormCreator(messageProvider.getText(MessageKey.workflowRepository_title)) {
			@Override
			public Form<?> createForm() {
				return createWorkflowRepositoryForm();
			}
		});
		maingroup.add(new FormCreator(messageProvider.getText(MessageKey.message_title)) {
			@Override
			public Form<?> createForm() {
				return createMessageForm();
			}
		});
		maingroup.add(new FormGroup(messageProvider.getText(MessageKey.logsGroup_title),createLogGroup()));
		
		maingroup.add(new FormGroup(messageProvider.getText(MessageKey.loadGroup_title),createLoadGroup()));
		
		FormCreator sqlformcreator = new FormCreator(messageProvider.getText(MessageKey.sql_title)) {
			@Override
			public Form<?> createForm() {
				return createSqlForm();
			}
		};
		if (!guiCopperDataProvider.getInterfaceSettings().isCanExecuteSql()){
			sqlformcreator.setEnabled(false);
			sqlformcreator.setTooltip(new Tooltip("disabled in copper"));
		}
		maingroup.add(sqlformcreator);
		
		maingroup.add(new FormCreator(messageProvider.getText(MessageKey.hotfix_title)) {
			@Override
			public Form<?> createForm() {
				return createHotfixForm();
			}
		});
		maingroup.add(new FormCreator(messageProvider.getText(MessageKey.settings_title)) {
			@Override
			public Form<?> createForm() {
				return createSettingsForm();
			}
		});
		formGroup = new FormGroup("",maingroup);
	}
	
	public ArrayList<FormCreator> createLogGroup() {
		ArrayList<FormCreator> loggroup = new ArrayList<FormCreator>();
		loggroup.add(new FormCreator(messageProvider.getText(MessageKey.audittrail_title)) {
			@Override
			public Form<?> createForm() {
				return createAudittrailForm();
			}
		});
		loggroup.add(new FormCreator(messageProvider.getText(MessageKey.logs_title)) {
			@Override
			public Form<?> createForm() {
				return createLogsForm();
			}
		});
		return loggroup;
	}
	
	public ArrayList<FormCreator> createWorkflowGroup() {
		ArrayList<FormCreator> workflowgroup = new ArrayList<FormCreator>();
		workflowgroup.add(new FormCreator(messageProvider.getText(MessageKey.workflowOverview_title)) {
			@Override
			public Form<?> createForm() {
				return createWorkflowOverviewForm();
			}
		});
		workflowgroup.add(new FormCreator(messageProvider.getText(MessageKey.workflowInstance_title)) {
			@Override
			public Form<?> createForm() {
				return createWorkflowInstanceListForm();
			}
		});
		return workflowgroup;
	}

	public ArrayList<FormCreator> createLoadGroup() {
		ArrayList<FormCreator> loadgroup = new ArrayList<FormCreator>();
		loadgroup.add(new FormCreator(messageProvider.getText(MessageKey.engineLoad_title)) {
			@Override
			public Form<?> createForm() {
				return createEngineLoadForm();
			}
		});
		loadgroup.add(new FormCreator(messageProvider.getText(MessageKey.resource_title)) {
			@Override
			public Form<?> createForm() {
				return createRessourceForm();
			}
		});
		loadgroup.add(new FormCreator(messageProvider.getText(MessageKey.measurePoint_title)) {
			@Override
			public Form<?> createForm() {
				return createMeasurePointForm();
			}
		});
		loadgroup.add(new FormCreator(messageProvider.getText(MessageKey.customMeasurePoint_title)) {
			@Override
			public Form<?> createForm() {
				return createCustomMeasurePointForm();
			}
		});
		return loadgroup;
	}
	
	public void setupGUIStructure(){
		mainPane.setCenter(mainTabPane);
		mainPane.setTop(createToolbar());
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				new FormCreator(messageProvider.getText(MessageKey.dashboard_title)) {
					@Override
					public Form<?> createForm() {
						return createDashboardForm();
					}
				}.show();
			}
		});
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
		buttonBar.setAlignment(Pos.CENTER);
		HBox.setHgrow(buttonBar, Priority.ALWAYS);
		buttonBar.getStyleClass().setAll("segmented-button-bar");

		List<Node> buttons = formGroup.createButtonList();
		buttons.get(0).getStyleClass().addAll("first");
		buttons.get(buttons.size() - 1).getStyleClass().addAll("last", "capsule");

		buttonBar.getChildren().addAll(buttons);
		toolBar.getItems().addAll(/*spacer,*/ buttonBar);
		toolBar.setCache(true);
		return toolBar;
	}
	
	public WorkflowClassesTreeForm createWorkflowClassesTreeForm(WorkflowSummaryFilterController filterController){
		TreeView<DisplayWorkflowClassesModel> workflowView = new TreeView<DisplayWorkflowClassesModel>();
		WorkflowClassesTreeController workflowClassesTreeController = createWorkflowClassesTreeController(workflowView);
		return new WorkflowClassesTreeForm("",new EmptyShowFormStrategie(),workflowClassesTreeController,
				filterController, workflowView,guiCopperDataProvider);
	}

	@Override
	public WorkflowClassesTreeController createWorkflowClassesTreeController(TreeView<DisplayWorkflowClassesModel> workflowView) {
		WorkflowClassesTreeController workflowClassesTreeController = new WorkflowClassesTreeController(workflowView);
		return workflowClassesTreeController;
	}
	
	public FilterAbleForm<WorkflowSummaryFilterModel,WorkflowSummaryResultModel> createWorkflowOverviewForm(){
		return new EngineFormBuilder<WorkflowSummaryFilterModel,WorkflowSummaryResultModel,WorkflowSummaryFilterController,WorkflowSummaryResultController>(
				new WorkflowSummaryFilterController(this),
				new WorkflowSummaryResultController(guiCopperDataProvider,this),
				this
			).build();
	}
	
	public FilterAbleForm<WorkflowInstanceFilterModel,WorkflowInstanceResultModel> createWorkflowInstanceListForm(){
		return new EngineFormBuilder<WorkflowInstanceFilterModel,WorkflowInstanceResultModel,WorkflowInstanceFilterController,WorkflowInstanceResultController>(
					new WorkflowInstanceFilterController(),
					new WorkflowInstanceResultController(guiCopperDataProvider,this),
					this
				).build();
	}
	

	public FilterAbleForm<MessageFilterModel,MessageResultModel> createMessageForm(){
		return new EngineFormBuilder<MessageFilterModel,MessageResultModel,MessageFilterController,MessageResultController>(
				new MessageFilterController(),
				new MessageResultController(guiCopperDataProvider),
				this
			).build();
	}
	
	public FilterAbleForm<WorkflowRepositoryFilterModel,WorkflowVersion> createWorkflowRepositoryForm(){
		return new EngineFormBuilder<WorkflowRepositoryFilterModel,WorkflowVersion,WorkflowRepositoryFilterController,WorkflowRepositoryResultController>(
				new WorkflowRepositoryFilterController(),
				new WorkflowRepositoryResultController(guiCopperDataProvider,this,codeMirrorFormatterSingelton),
				this
			).build();
	}
	
	@Override
	public FilterAbleForm<AuditTrailFilterModel,AuditTrailResultModel> createAudittrailForm(){
		return new FormBuilder<AuditTrailFilterModel,AuditTrailResultModel,AuditTrailFilterController,AuditTrailResultController>(
				new AuditTrailFilterController(),
				new AuditTrailResultController(guiCopperDataProvider, settingsModelSinglton, codeMirrorFormatterSingelton),
				this
			).build();
	}
	
	@Override
	public EngineFilterAbleForm<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel> createWorkflowInstanceDetailForm(String workflowInstanceId, ProcessingEngineInfo engineInfo){
		FilterController<WorkflowInstanceDetailFilterModel> fCtrl = new WorkflowInstanceDetailFilterController(new WorkflowInstanceDetailFilterModel(workflowInstanceId,engineInfo)); 
		
		FxmlForm<FilterController<WorkflowInstanceDetailFilterModel>> filterForm = new FxmlForm<FilterController<WorkflowInstanceDetailFilterModel>>(fCtrl, messageProvider);
		
		FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>> resultForm = createWorkflowinstanceDetailResultForm(new EmptyShowFormStrategie());
		
		EngineFilterAbleForm<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel> filterAbleForm = new EngineFilterAbleForm<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>(messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
		filterAbleForm.displayedTitleProperty().bind(new SimpleStringProperty("Details Id:").concat(fCtrl.getFilter().workflowInstanceId));
		return filterAbleForm;
	}
	
	public FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>> createWorkflowinstanceDetailResultForm(ShowFormStrategy<?> showFormStrategy) {
		FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel> resCtrl = new WorkflowInstanceDetailResultController(guiCopperDataProvider, codeMirrorFormatterSingelton);
		FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel>> resultForm = 
				new FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel>>("workflowInstanceDetail.title",
				resCtrl, messageProvider, showFormStrategy );
		return resultForm;
	}

	public FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>> createWorkflowinstanceDetailResultForm(BorderPane target) {
		return createWorkflowinstanceDetailResultForm(new BorderPaneShowFormStrategie(target));
	}
	
	private FilterAbleForm<EngineLoadFilterModel,WorkflowStateSummary> engineLoadFormSingelton;
	public FilterAbleForm<EngineLoadFilterModel,WorkflowStateSummary> createEngineLoadForm(){
		FilterController<EngineLoadFilterModel> fCtrl = new EngineLoadFilterController(); 
		FxmlForm<FilterController<EngineLoadFilterModel>> filterForm = new FxmlForm<FilterController<EngineLoadFilterModel>>(fCtrl, messageProvider);
		
		FilterResultController<EngineLoadFilterModel,WorkflowStateSummary> resCtrl = new EngineLoadResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<EngineLoadFilterModel,WorkflowStateSummary>> resultForm = 
				new FxmlForm<FilterResultController<EngineLoadFilterModel,WorkflowStateSummary>>(resCtrl, messageProvider);
		
		if (engineLoadFormSingelton==null){
			engineLoadFormSingelton = new EngineFilterAbleForm<EngineLoadFilterModel,WorkflowStateSummary>(messageProvider,
					new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
		}
		return engineLoadFormSingelton;
	}
	
	public Form<SettingsController> createSettingsForm(){
		if (settingsForSingleton==null){
			settingsForSingleton = new FxmlForm<SettingsController>("",new SettingsController(settingsModelSinglton), messageProvider,  new TabPaneShowFormStrategie(mainTabPane));
		}
		return settingsForSingleton;
	}
	
	public Form<HotfixController> createHotfixForm(){
		if (hotfixFormSingleton==null){
			hotfixFormSingleton = new FxmlForm<HotfixController>("",new HotfixController(new HotfixModel(), guiCopperDataProvider), messageProvider,  new TabPaneShowFormStrategie(mainTabPane));
		}
		return hotfixFormSingleton;
	}
	
	public FilterAbleForm<SqlFilterModel,SqlResultModel> createSqlForm(){
		FilterAbleForm<SqlFilterModel, SqlResultModel> filterAbleForm = new FormBuilder<SqlFilterModel,SqlResultModel,SqlFilterController,SqlResultController>(
				new SqlFilterController(codeMirrorFormatterSingelton),
				new SqlResultController(guiCopperDataProvider),
				this
			).build();
		filterAbleForm.useVerticalRightButton();
		return filterAbleForm;
	}
	
	FilterAbleForm<ResourceFilterModel,SystemResourcesInfo> ressourceFormSingelton=null;
	public FilterAbleForm<ResourceFilterModel,SystemResourcesInfo> createRessourceForm(){
		if (ressourceFormSingelton==null){
			ressourceFormSingelton=new FormBuilder<ResourceFilterModel,SystemResourcesInfo,ResourceFilterController,RessourceResultController>(
					new ResourceFilterController(),
					new RessourceResultController(guiCopperDataProvider),
					this
				).build();
		}
		return ressourceFormSingelton; 
	}
	
	public FilterAbleForm<EmptyFilterModel,DashboardResultModel> createDashboardForm(){
		if (dasboardFormSingleton==null){
			dasboardFormSingleton = new FormBuilder<EmptyFilterModel,DashboardResultModel,GenericFilterController<EmptyFilterModel>,DashboardResultController>(
					new GenericFilterController<EmptyFilterModel>(5000),
					new DashboardResultController(guiCopperDataProvider,this),
					this
				).build();
		}
		return dasboardFormSingleton;
	}
	
	public Form<ProccessorPoolController> createPoolForm(TabPane tabPane, ProcessingEngineInfo engine, ProcessorPoolInfo pool, DashboardResultModel model){
		return new FxmlForm<ProccessorPoolController>(pool.getId(), new ProccessorPoolController(engine,pool,guiCopperDataProvider), messageProvider, new TabPaneShowFormStrategie(tabPane,true));
	}
	
	@Override
	public Form<ProcessingEngineController> createEngineForm(TabPane tabPane, ProcessingEngineInfo engine, DashboardResultModel model){
		return new FxmlForm<ProcessingEngineController>(engine.getId(), new ProcessingEngineController(engine,model,this,guiCopperDataProvider), messageProvider, new TabPaneShowFormStrategie(tabPane));
	}
	
	public FilterAbleForm<EngineFilterModelBase, MeasurePointData> createMeasurePointForm() {
		return new EngineFormBuilder<EngineFilterModelBase, MeasurePointData, GenericFilterController<EngineFilterModelBase>,MeasurePointResultController>(
				new GenericFilterController<EngineFilterModelBase>(new EngineFilterModelBase()),
				new MeasurePointResultController(guiCopperDataProvider),
				this
			).build();
	}
	
	public FilterAbleForm<AdapterMonitoringFilterModel, AdapterMonitoringResultModel> createAdapterMonitoringForm() {
		return new FormBuilder<AdapterMonitoringFilterModel, AdapterMonitoringResultModel, AdapterMonitoringFilterController,AdapterMonitoringResultController>(
				new AdapterMonitoringFilterController(),
				new AdapterMonitoringResultController(guiCopperDataProvider),
				this
			).build();
	}

	protected ShowFormStrategy<?> getDefaultShowFormStrategy() {
		return new TabPaneShowFormStrategie(mainTabPane);
	}
	
	public FilterAbleForm<CustomMeasurePointFilterModel, MeasurePointData> createCustomMeasurePointForm() {
		return new FormBuilder<CustomMeasurePointFilterModel, MeasurePointData, CustomMeasurePointFilterController,CustomMeasurePointResultController>(
				new CustomMeasurePointFilterController(guiCopperDataProvider),
				new CustomMeasurePointResultController(guiCopperDataProvider),
				this
			).build();
	}
	
	public FilterAbleForm<LogsFilterModel, LogsResultModel> createLogsForm() {
		return new FormBuilder<LogsFilterModel, LogsResultModel, LogsFilterController,LogsResultController>(
				new LogsFilterController(),
				new LogsResultController(guiCopperDataProvider),
				this
			).build();
	}
	
}
