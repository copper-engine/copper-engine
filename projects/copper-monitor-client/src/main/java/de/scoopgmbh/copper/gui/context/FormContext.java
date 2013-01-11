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

import javafx.scene.control.Button;
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
import de.scoopgmbh.copper.gui.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.gui.form.filter.FilterController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.audittrail.filter.AuditTrailFilterController;
import de.scoopgmbh.copper.gui.ui.audittrail.filter.AuditTrailFilterModel;
import de.scoopgmbh.copper.gui.ui.audittrail.result.AuditTrailResultController;
import de.scoopgmbh.copper.gui.ui.audittrail.result.AuditTrailResultModel;
import de.scoopgmbh.copper.gui.ui.load.filter.EngineLoadFilterController;
import de.scoopgmbh.copper.gui.ui.load.filter.EngineLoadFilterModel;
import de.scoopgmbh.copper.gui.ui.load.result.EngineLoadResultController;
import de.scoopgmbh.copper.gui.ui.settings.SettingsController;
import de.scoopgmbh.copper.gui.ui.settings.SettingsModel;
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
import de.scoopgmbh.copper.gui.util.MessageProvider;
import de.scoopgmbh.copper.monitor.adapter.model.CopperLoadInfo;

public class FormContext {
	private final TabPane mainTabPane;
	private final BorderPane mainPane;
	private FormGroup formGroup;
	private final MessageProvider messageProvider;
	private final SettingsModel settingsModelSinglton;

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
		
		ArrayList<Form<?>> group = new ArrayList<>();
		group.add(createWorkflowSummeryForm());
		group.add(createWorkflowInstanceForm());
		group.add(createAudittrailForm());
		group.add(createEngineLoadForm());
		group.add(createLoginForm());
		formGroup = new FormGroup(group);
	}
	
	public void setupGUIStructure(){
		mainPane.setCenter(mainTabPane);
		mainPane.setTop(createToolbar());
	}

	public MenuBar createMenueBar(){
		final MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(formGroup.createMenue());
		return menuBar;
	}
	
	public ToolBar createToolbar() {
		ToolBar toolBar = new ToolBar();
		Region spacer = new Region();
		spacer.getStyleClass().setAll("spacer");

		HBox buttonBar = new HBox();
		buttonBar.getStyleClass().setAll("segmented-button-bar");

		List<Button> buttons = formGroup.createButtonList();
		buttons.get(0).getStyleClass().addAll("first");
		buttons.get(buttons.size() - 1).getStyleClass().addAll("last", "capsule");

		buttonBar.getChildren().addAll(buttons);
		toolBar.getItems().addAll(spacer, buttonBar);

		return toolBar;
	}
	
	public WorkflowClassesTreeForm createWorkflowClassesTreeForm(WorkflowSummeryFilterController filterController){
		return new WorkflowClassesTreeForm("workflowClassesTreeForm.title", messageProvider,new WorkflowClassesTreeController(guiCopperDataProvider,filterController));
	}
	
	
	public FilterAbleForm<WorkflowSummeryFilterModel,WorkflowSummeryResultModel> createWorkflowSummeryForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowSummeryFilterModel> fCtrl = new WorkflowSummeryFilterController(this); 
		FxmlForm<FilterController<WorkflowSummeryFilterModel>> filterForm = new FxmlForm<>("workflowsummeryFilter.title",
				fCtrl, messageProvider);
		
		FilterResultController<WorkflowSummeryFilterModel,WorkflowSummeryResultModel> resCtrl = new WorkflowSummeryResultController(guiCopperDataProvider,this);
		FxmlForm<FilterResultController<WorkflowSummeryFilterModel,WorkflowSummeryResultModel>> resultForm = new FxmlForm<>("workflowsummeryFilter.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("workflowsummery.title", messageProvider,
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
		
		return new FilterAbleForm<>("workflowInstance.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<AuditTrailFilterModel,AuditTrailResultModel> createAudittrailForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<AuditTrailFilterModel> fCtrl = new AuditTrailFilterController(); 
		FxmlForm<FilterController<AuditTrailFilterModel>> filterForm = new FxmlForm<>("workflowsummeryFilter.title",
				fCtrl, messageProvider);
		
		FilterResultController<AuditTrailFilterModel,AuditTrailResultModel> resCtrl = new AuditTrailResultController(guiCopperDataProvider, settingsModelSinglton);
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
	
	public FilterAbleForm<EngineLoadFilterModel,CopperLoadInfo> createEngineLoadForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<EngineLoadFilterModel> fCtrl = new EngineLoadFilterController(); 
		FxmlForm<FilterController<EngineLoadFilterModel>> filterForm = new FxmlForm<>("engineLoad.title",
				fCtrl, messageProvider);
		
		FilterResultController<EngineLoadFilterModel,CopperLoadInfo> resCtrl = new EngineLoadResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<EngineLoadFilterModel,CopperLoadInfo>> resultForm = new FxmlForm<>("engineLoad.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("engineLoad.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public Form<SettingsController> createLoginForm(){
		return new FxmlForm<>("settings.title", new SettingsController(settingsModelSinglton), messageProvider,  new TabPaneShowFormStrategie(mainTabPane));
	}
}
