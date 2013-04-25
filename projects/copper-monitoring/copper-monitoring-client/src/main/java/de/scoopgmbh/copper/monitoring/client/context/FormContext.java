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
import de.scoopgmbh.copper.monitoring.client.form.BorderPaneShowFormStrategie;
import de.scoopgmbh.copper.monitoring.client.form.EmptyShowFormStrategie;
import de.scoopgmbh.copper.monitoring.client.form.Form;
import de.scoopgmbh.copper.monitoring.client.form.FormCreator;
import de.scoopgmbh.copper.monitoring.client.form.FormGroup;
import de.scoopgmbh.copper.monitoring.client.form.FxmlForm;
import de.scoopgmbh.copper.monitoring.client.form.ShowFormStrategy;
import de.scoopgmbh.copper.monitoring.client.form.TabPaneShowFormStrategie;
import de.scoopgmbh.copper.monitoring.client.form.enginefilter.EngineFilterAbleform;
import de.scoopgmbh.copper.monitoring.client.form.enginefilter.EngineFilterModelBase;
import de.scoopgmbh.copper.monitoring.client.form.filter.EmptyFilterModel;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterController;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultController;
import de.scoopgmbh.copper.monitoring.client.form.filter.GenericFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.filter.AuditTrailFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.filter.AuditTrailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.result.AuditTrailResultController;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.result.AuditTrailResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.dashboard.result.DashboardResultController;
import de.scoopgmbh.copper.monitoring.client.ui.dashboard.result.DashboardResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.dashboard.result.engine.ProcessingEngineController;
import de.scoopgmbh.copper.monitoring.client.ui.dashboard.result.pool.ProccessorPoolController;
import de.scoopgmbh.copper.monitoring.client.ui.hotfix.HotfixController;
import de.scoopgmbh.copper.monitoring.client.ui.hotfix.HotfixModel;
import de.scoopgmbh.copper.monitoring.client.ui.load.filter.EngineLoadFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.load.filter.EngineLoadFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.load.result.EngineLoadResultController;
import de.scoopgmbh.copper.monitoring.client.ui.measurepoint.result.MeasurePointResultController;
import de.scoopgmbh.copper.monitoring.client.ui.repository.filter.WorkflowRepositoryFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.repository.filter.WorkflowRepositoryFilterModel;
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
import de.scoopgmbh.copper.monitoring.client.ui.workflowhistory.filter.WorkflowHistoryFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowhistory.filter.WorkflowHistoryFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowhistory.result.WorkflowHistoryResultController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowhistory.result.WorkflowHistoryResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.filter.WorkflowInstanceFilterController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
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
		
		maingroup.add(new FormCreator(messageProvider.getText(MessageKey.workflowRepository_title)) {
			@Override
			public Form<?> createForm() {
				return createWorkflowRepositoryForm();
			}
		});
		maingroup.add(new FormCreator(messageProvider.getText(MessageKey.workflowHistory_title)) {
			@Override
			public Form<?> createForm() {
				return createWorkflowHistoryForm();
			}
		});
		maingroup.add(new FormCreator(messageProvider.getText(MessageKey.audittrail_title)) {
			@Override
			public Form<?> createForm() {
				return createAudittrailForm();
			}
		});
		
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
	
	public ArrayList<FormCreator> createWorkflowGroup() {
		ArrayList<FormCreator> workflowgroup = new ArrayList<FormCreator>();
		workflowgroup.add(new FormCreator(messageProvider.getText(MessageKey.workflowoverview_title)) {
			@Override
			public Form<?> createForm() {
				return createWorkflowOverviewForm();
			}
		});
		workflowgroup.add(new FormCreator(messageProvider.getText(MessageKey.workflowInstance_title)) {
			@Override
			public Form<?> createForm() {
				return createWorkflowInstanceForm();
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
		return loadgroup;
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

	public WorkflowClassesTreeController createWorkflowClassesTreeController(TreeView<DisplayWorkflowClassesModel> workflowView) {
		WorkflowClassesTreeController workflowClassesTreeController = new WorkflowClassesTreeController(workflowView);
		return workflowClassesTreeController;
	}
	
	public FilterAbleForm<WorkflowSummaryFilterModel,WorkflowSummaryResultModel> createWorkflowOverviewForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowSummaryFilterModel> fCtrl = new WorkflowSummaryFilterController(this); 
		FxmlForm<FilterController<WorkflowSummaryFilterModel>> filterForm = new FxmlForm<FilterController<WorkflowSummaryFilterModel>>(fCtrl, messageProvider);
		
		FilterResultController<WorkflowSummaryFilterModel,WorkflowSummaryResultModel> resCtrl = new WorkflowSummaryResultController(guiCopperDataProvider,this);
		FxmlForm<FilterResultController<WorkflowSummaryFilterModel,WorkflowSummaryResultModel>> resultForm =
				new FxmlForm<FilterResultController<WorkflowSummaryFilterModel,WorkflowSummaryResultModel>>(resCtrl, messageProvider);
		
		return new EngineFilterAbleform<WorkflowSummaryFilterModel,WorkflowSummaryResultModel>(messageProvider.getText(MessageKey.workflowoverview_title),messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<WorkflowInstanceFilterModel,WorkflowInstanceResultModel> createWorkflowInstanceForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowInstanceFilterModel> fCtrl = new WorkflowInstanceFilterController(); 
		FxmlForm<FilterController<WorkflowInstanceFilterModel>> filterForm = new FxmlForm<FilterController<WorkflowInstanceFilterModel>>(fCtrl, messageProvider);
		
		FilterResultController<WorkflowInstanceFilterModel,WorkflowInstanceResultModel> resCtrl = new WorkflowInstanceResultController(guiCopperDataProvider,this);
		FxmlForm<FilterResultController<WorkflowInstanceFilterModel,WorkflowInstanceResultModel>> resultForm = 
				new FxmlForm<FilterResultController<WorkflowInstanceFilterModel,WorkflowInstanceResultModel>>(resCtrl, messageProvider);
		
		return new EngineFilterAbleform<WorkflowInstanceFilterModel,WorkflowInstanceResultModel>(messageProvider.getText(MessageKey.workflowInstance_title),messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<WorkflowHistoryFilterModel,WorkflowHistoryResultModel> createWorkflowHistoryForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowHistoryFilterModel> fCtrl = new WorkflowHistoryFilterController(); 
		FxmlForm<FilterController<WorkflowHistoryFilterModel>> filterForm = new FxmlForm<FilterController<WorkflowHistoryFilterModel>>(fCtrl, messageProvider);
		
		FilterResultController<WorkflowHistoryFilterModel,WorkflowHistoryResultModel> resCtrl = new WorkflowHistoryResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<WorkflowHistoryFilterModel,WorkflowHistoryResultModel>> resultForm = 
				new FxmlForm<FilterResultController<WorkflowHistoryFilterModel,WorkflowHistoryResultModel>>(resCtrl, messageProvider);
		
		return new EngineFilterAbleform<WorkflowHistoryFilterModel,WorkflowHistoryResultModel>(messageProvider.getText(MessageKey.workflowHistory_title),messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<WorkflowRepositoryFilterModel,WorkflowVersion> createWorkflowRepositoryForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowRepositoryFilterModel> fCtrl = new WorkflowRepositoryFilterController(); 
		FxmlForm<FilterController<WorkflowRepositoryFilterModel>> filterForm = new FxmlForm<FilterController<WorkflowRepositoryFilterModel>>(fCtrl, messageProvider);
		
		FilterResultController<WorkflowRepositoryFilterModel,WorkflowVersion> resCtrl = new WorkflowRepositoryResultController(guiCopperDataProvider,this);
		FxmlForm<FilterResultController<WorkflowRepositoryFilterModel,WorkflowVersion>> resultForm = 
				new FxmlForm<FilterResultController<WorkflowRepositoryFilterModel,WorkflowVersion>>(resCtrl, messageProvider);
		
		return new EngineFilterAbleform<WorkflowRepositoryFilterModel,WorkflowVersion>(messageProvider.getText(MessageKey.workflowRepository_title),messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<AuditTrailFilterModel,AuditTrailResultModel> createAudittrailForm(){
		FilterController<AuditTrailFilterModel> fCtrl = new AuditTrailFilterController(); 
		FxmlForm<FilterController<AuditTrailFilterModel>> filterForm = new FxmlForm<FilterController<AuditTrailFilterModel>>(fCtrl, messageProvider);
		
		FilterResultController<AuditTrailFilterModel,AuditTrailResultModel> resCtrl = new AuditTrailResultController(guiCopperDataProvider, settingsModelSinglton, codeMirrorFormatterSingelton);
		FxmlForm<FilterResultController<AuditTrailFilterModel,AuditTrailResultModel>> resultForm = 
				new FxmlForm<FilterResultController<AuditTrailFilterModel,AuditTrailResultModel>>(resCtrl, messageProvider);
		
		return new FilterAbleForm<AuditTrailFilterModel,AuditTrailResultModel>(messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel> createWorkflowInstanceDetailForm(String workflowInstanceId){
		FilterController<WorkflowInstanceDetailFilterModel> fCtrl = new WorkflowInstanceDetailFilterController(workflowInstanceId); 
		
		FxmlForm<FilterController<WorkflowInstanceDetailFilterModel>> filterForm = new FxmlForm<FilterController<WorkflowInstanceDetailFilterModel>>(fCtrl, messageProvider);
		
		FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>> resultForm = createWorkflowinstanceDetailResultForm(new EmptyShowFormStrategie());
		
		FilterAbleForm<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel> filterAbleForm = new FilterAbleForm<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>(messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
		filterAbleForm.dynamicTitleProperty().bind(new SimpleStringProperty("Details Id:").concat(fCtrl.getFilter().workflowInstanceId));
		return filterAbleForm;
	}
	
	public FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>> createWorkflowinstanceDetailResultForm(ShowFormStrategy<?> showFormStrategy) {
		FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel> resCtrl = new WorkflowInstanceDetailResultController(guiCopperDataProvider);
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
			engineLoadFormSingelton = new EngineFilterAbleform<EngineLoadFilterModel,WorkflowStateSummary>(messageProvider.getText(MessageKey.engineLoad_title),messageProvider,
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
		FilterController<SqlFilterModel> fCtrl = new SqlFilterController(codeMirrorFormatterSingelton); 
		FxmlForm<FilterController<SqlFilterModel>> filterForm = new FxmlForm<FilterController<SqlFilterModel>>(fCtrl, messageProvider);
		
		FilterResultController<SqlFilterModel,SqlResultModel> resCtrl = new SqlResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<SqlFilterModel,SqlResultModel>> resultForm = 
				new FxmlForm<FilterResultController<SqlFilterModel,SqlResultModel>>(resCtrl, messageProvider);
		
		return new FilterAbleForm<SqlFilterModel,SqlResultModel>(messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	FilterAbleForm<ResourceFilterModel,SystemResourcesInfo> ressourceFormSingelton=null;
	public FilterAbleForm<ResourceFilterModel,SystemResourcesInfo> createRessourceForm(){
		FilterController<ResourceFilterModel> fCtrl = new ResourceFilterController(); 
		FxmlForm<FilterController<ResourceFilterModel>> filterForm = new FxmlForm<FilterController<ResourceFilterModel>>(fCtrl, messageProvider);
		
		FilterResultController<ResourceFilterModel,SystemResourcesInfo> resCtrl = new RessourceResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<ResourceFilterModel,SystemResourcesInfo>> resultForm = 
				new FxmlForm<FilterResultController<ResourceFilterModel,SystemResourcesInfo>>(resCtrl, messageProvider);
		
		if (ressourceFormSingelton==null){
			ressourceFormSingelton=new FilterAbleForm<ResourceFilterModel,SystemResourcesInfo>(messageProvider,
					new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
		}
		return ressourceFormSingelton; 
	}
	
	public FilterAbleForm<EmptyFilterModel,DashboardResultModel> createDashboardForm(){
		if (dasboardFormSingleton==null){
			FilterController<EmptyFilterModel> fCtrl = new GenericFilterController<EmptyFilterModel>(5000); 
			FxmlForm<FilterController<EmptyFilterModel>> filterForm = new FxmlForm<FilterController<EmptyFilterModel>>(fCtrl, messageProvider);
			
			FilterResultController<EmptyFilterModel,DashboardResultModel> resCtrl = new DashboardResultController(guiCopperDataProvider,this);
			FxmlForm<FilterResultController<EmptyFilterModel,DashboardResultModel>> resultForm = new FxmlForm<FilterResultController<EmptyFilterModel,DashboardResultModel>>(resCtrl, messageProvider);
			
			dasboardFormSingleton = new FilterAbleForm<EmptyFilterModel,DashboardResultModel>(messageProvider,
					new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
		}
		return dasboardFormSingleton;
	}
	
	public Form<ProccessorPoolController> createPoolForm(TabPane tabPane, ProcessingEngineInfo engine, ProcessorPoolInfo pool, DashboardResultModel model){
		return new FxmlForm<ProccessorPoolController>(pool.getId(), new ProccessorPoolController(engine,pool,guiCopperDataProvider), messageProvider, new TabPaneShowFormStrategie(tabPane,true));
	}
	
	public Form<ProcessingEngineController> createEngineForm(TabPane tabPane, ProcessingEngineInfo engine, DashboardResultModel model){
		return new FxmlForm<ProcessingEngineController>(engine.getId(), new ProcessingEngineController(engine,model,this,guiCopperDataProvider), messageProvider, new TabPaneShowFormStrategie(tabPane));
	}
	
	public FilterAbleForm<EngineFilterModelBase, MeasurePointData> createMeasurePointForm() {

		FilterController<EngineFilterModelBase> fCtrl = new GenericFilterController<EngineFilterModelBase>(new EngineFilterModelBase());
		FxmlForm<FilterController<EngineFilterModelBase>> filterForm = new FxmlForm<FilterController<EngineFilterModelBase>>(fCtrl, messageProvider);

		FilterResultController<EngineFilterModelBase, MeasurePointData> resCtrl = new MeasurePointResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<EngineFilterModelBase, MeasurePointData>> resultForm = new FxmlForm<FilterResultController<EngineFilterModelBase, MeasurePointData>>(resCtrl, messageProvider);

		return new EngineFilterAbleform<EngineFilterModelBase, MeasurePointData>(messageProvider.getText(MessageKey.measurePoint_title),messageProvider, new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,
				guiCopperDataProvider);
	}
	
}
