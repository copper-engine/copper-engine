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

import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.FormGroup;
import de.scoopgmbh.copper.gui.form.FxmlForm;
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
	public FormContext(BorderPane mainPane, GuiCopperDataProvider guiCopperDataProvider, MessageProvider messageProvider, SettingsModel settingsModelSinglton) {
		this.mainTabPane = new TabPane();
		this.messageProvider = messageProvider;
		this.guiCopperDataProvider = guiCopperDataProvider;
		this.mainPane = mainPane;
		this.settingsModelSinglton = settingsModelSinglton;
		
		ArrayList<Form<?>> maingroup = new ArrayList<>();
		maingroup.add(createDashboardForm());
		maingroup.add(createWorkflowOverviewForm());
		maingroup.add(createWorkflowInstanceForm());
		maingroup.add(createAudittrailForm());
		
		ArrayList<Form<?>> loadgroup = new ArrayList<>();
		loadgroup.add(createEngineLoadForm());
		loadgroup.add(createRessourceForm());
		maingroup.add(new FormGroup(loadgroup,"loadGroup.title",messageProvider));
		
		maingroup.add(createSqlForm());
		maingroup.add(createSettingsForm());
		formGroup = new FormGroup(maingroup,"",messageProvider);
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
		return new WorkflowClassesTreeForm("workflowClassesTreeForm.title", messageProvider,new WorkflowClassesTreeController(guiCopperDataProvider,filterController));
	}
	
	
	public FilterAbleForm<WorkflowSummeryFilterModel,WorkflowSummeryResultModel> createWorkflowOverviewForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowSummeryFilterModel> fCtrl = new WorkflowSummeryFilterController(this); 
		FxmlForm<FilterController<WorkflowSummeryFilterModel>> filterForm = new FxmlForm<>("workflowsummeryFilter.title",
				fCtrl, messageProvider);
		
		FilterResultController<WorkflowSummeryFilterModel,WorkflowSummeryResultModel> resCtrl = new WorkflowSummeryResultController(guiCopperDataProvider,this);
		FxmlForm<FilterResultController<WorkflowSummeryFilterModel,WorkflowSummeryResultModel>> resultForm = new FxmlForm<>("workflowsummeryFilter.title",
				resCtrl, messageProvider);
		
		return new EngineFilterAbleform<>("workflowsummery.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<WorkflowInstanceFilterModel,WorkflowInstanceResultModel> createWorkflowInstanceForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowInstanceFilterModel> fCtrl = new WorkflowInstanceFilterController(); 
		FxmlForm<FilterController<WorkflowInstanceFilterModel>> filterForm = new FxmlForm<>("workflowsummeryFilter.title",
				fCtrl, messageProvider);
		
		FilterResultController<WorkflowInstanceFilterModel,WorkflowInstanceResultModel> resCtrl = new WorkflowInstanceResultController(guiCopperDataProvider,this);
		FxmlForm<FilterResultController<WorkflowInstanceFilterModel,WorkflowInstanceResultModel>> resultForm = new FxmlForm<>("workflowsummeryFilter.title",
				resCtrl, messageProvider);
		
		return new EngineFilterAbleform<>("workflowInstance.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<AuditTrailFilterModel,AuditTrailResultModel> createAudittrailForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<AuditTrailFilterModel> fCtrl = new AuditTrailFilterController(); 
		FxmlForm<FilterController<AuditTrailFilterModel>> filterForm = new FxmlForm<>("workflowsummeryFilter.title",
				fCtrl, messageProvider);
		
		FilterResultController<AuditTrailFilterModel,AuditTrailResultModel> resCtrl = new AuditTrailResultController(guiCopperDataProvider, settingsModelSinglton, codeMirrorFormatterSingelton);
		FxmlForm<FilterResultController<AuditTrailFilterModel,AuditTrailResultModel>> resultForm = new FxmlForm<>("workflowsummeryFilter.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("audittrail.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel> createWorkflowInstanceDetailForm(String workflowInstanceId){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowInstanceDetailFilterModel> fCtrl = new WorkflowInstanceDetailFilterController(workflowInstanceId); 
		FxmlForm<FilterController<WorkflowInstanceDetailFilterModel>> filterForm = new FxmlForm<>("workflowInstanceDetail.title",
				fCtrl, messageProvider);
		
		FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel> resCtrl = new WorkflowInstanceDetailResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel,WorkflowInstanceDetailResultModel>> resultForm = new FxmlForm<>("workflowInstanceDetail.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("workflowInstanceDetail.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<EngineLoadFilterModel,WorkflowStateSummery> createEngineLoadForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<EngineLoadFilterModel> fCtrl = new EngineLoadFilterController(); 
		FxmlForm<FilterController<EngineLoadFilterModel>> filterForm = new FxmlForm<>("engineLoad.title",
				fCtrl, messageProvider);
		
		FilterResultController<EngineLoadFilterModel,WorkflowStateSummery> resCtrl = new EngineLoadResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<EngineLoadFilterModel,WorkflowStateSummery>> resultForm = new FxmlForm<>("engineLoad.title",
				resCtrl, messageProvider);
		
		return new EngineFilterAbleform<>("engineLoad.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public Form<SettingsController> createSettingsForm(){
		return new FxmlForm<>("settings.title", new SettingsController(settingsModelSinglton), messageProvider,  new TabPaneShowFormStrategie(mainTabPane));
	}
	
	public FilterAbleForm<SqlFilterModel,SqlResultModel> createSqlForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<SqlFilterModel> fCtrl = new SqlFilterController(codeMirrorFormatterSingelton); 
		FxmlForm<FilterController<SqlFilterModel>> filterForm = new FxmlForm<>("engineLoad.title",
				fCtrl, messageProvider);
		
		FilterResultController<SqlFilterModel,SqlResultModel> resCtrl = new SqlResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<SqlFilterModel,SqlResultModel>> resultForm = new FxmlForm<>("sql.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("sql.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<ResourceFilterModel,SystemResourcesInfo> createRessourceForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<ResourceFilterModel> fCtrl = new ResourceFilterController(); 
		FxmlForm<FilterController<ResourceFilterModel>> filterForm = new FxmlForm<>("engineLoad.title",
				fCtrl, messageProvider);
		
		FilterResultController<ResourceFilterModel,SystemResourcesInfo> resCtrl = new RessourceResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<ResourceFilterModel,SystemResourcesInfo>> resultForm = new FxmlForm<>("resource.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("resource.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<EmptyFilterModel,DashboardResultModel> createDashboardForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<EmptyFilterModel> fCtrl = new EmptyFilterController(); 
		FxmlForm<FilterController<EmptyFilterModel>> filterForm = new FxmlForm<>("engineLoad.title",
				fCtrl, messageProvider);
		
		FilterResultController<EmptyFilterModel,DashboardResultModel> resCtrl = new DashboardResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<EmptyFilterModel,DashboardResultModel>> resultForm = new FxmlForm<>("resource.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("dashboard.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
}
