package de.scoopgmbh.copper.gui.context;

import java.util.ArrayList;

import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
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
import de.scoopgmbh.copper.gui.ui.load.filter.EngineLoadFilterController;
import de.scoopgmbh.copper.gui.ui.load.filter.EngineLoadFilterModel;
import de.scoopgmbh.copper.gui.ui.load.result.EngineLoadResultController;
import de.scoopgmbh.copper.gui.ui.workflowclasssesctree.WorkflowClassesTreeController;
import de.scoopgmbh.copper.gui.ui.workflowclasssesctree.WorkflowClassesTreeForm;
import de.scoopgmbh.copper.gui.ui.workflowinstance.filter.WorkflowInstanceFilterController;
import de.scoopgmbh.copper.gui.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowinstance.result.WorkflowInstanceResultController;
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterController;
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowsummery.result.WorkflowSummeryResultController;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterController;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultController;
import de.scoopgmbh.copper.gui.util.MessageProvider;

public class FormContext {
	private final TabPane mainTabPane;
	private final BorderPane mainPane;
	private FormGroup formGroup;
	private MessageProvider messageProvider;

	public TabPane getMainTabPane() {
		return mainTabPane;
	}

	GuiCopperDataProvider guiCopperDataProvider;
	public FormContext(BorderPane mainPane, GuiCopperDataProvider guiCopperDataProvider, MessageProvider messageProvider) {
		this.mainTabPane = new TabPane();
		this.messageProvider = messageProvider;
		this.guiCopperDataProvider = guiCopperDataProvider;
		this.mainPane = mainPane;
		
		ArrayList<Form<?>> group = new ArrayList<>();
		group.add(createWorkflowSummeryForm());
		group.add(createWorkflowInstanceForm());
		group.add(createAudittrailForm());
		group.add(createEngineLoadForm());
		formGroup = new FormGroup(group);
	}
	
	public void setupGUIStructure(){
		mainPane.setCenter(mainTabPane);
		mainPane.setTop(createMenueBar());
	}

	public MenuBar createMenueBar(){
		final MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(formGroup.createMenue());
		return menuBar;
	}
	
	public WorkflowClassesTreeForm createWorkflowClassesTreeForm(WorkflowSummeryFilterController filterController){
		return new WorkflowClassesTreeForm("workflowClassesTreeForm.title", messageProvider,new WorkflowClassesTreeController(guiCopperDataProvider,filterController));
	}
	
	
	public FilterAbleForm<WorkflowSummeryFilterModel> createWorkflowSummeryForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowSummeryFilterModel> fCtrl = new WorkflowSummeryFilterController(this); 
		FxmlForm<FilterController<WorkflowSummeryFilterModel>> filterForm = new FxmlForm<>("workflowsummeryFilter.title",
				fCtrl, messageProvider);
		
		FilterResultController<WorkflowSummeryFilterModel> resCtrl = new WorkflowSummeryResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<WorkflowSummeryFilterModel>> resultForm = new FxmlForm<>("workflowsummeryFilter.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("workflowsummery.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<WorkflowInstanceFilterModel> createWorkflowInstanceForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowInstanceFilterModel> fCtrl = new WorkflowInstanceFilterController(); 
		FxmlForm<FilterController<WorkflowInstanceFilterModel>> filterForm = new FxmlForm<>("workflowsummeryFilter.title",
				fCtrl, messageProvider);
		
		FilterResultController<WorkflowInstanceFilterModel> resCtrl = new WorkflowInstanceResultController(guiCopperDataProvider,this);
		FxmlForm<FilterResultController<WorkflowInstanceFilterModel>> resultForm = new FxmlForm<>("workflowsummeryFilter.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("workflowInstance.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<AuditTrailFilterModel> createAudittrailForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<AuditTrailFilterModel> fCtrl = new AuditTrailFilterController(); 
		FxmlForm<FilterController<AuditTrailFilterModel>> filterForm = new FxmlForm<>("workflowsummeryFilter.title",
				fCtrl, messageProvider);
		
		FilterResultController<AuditTrailFilterModel> resCtrl = new AuditTrailResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<AuditTrailFilterModel>> resultForm = new FxmlForm<>("workflowsummeryFilter.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("audittrail.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<WorkflowInstanceDetailFilterModel> createWorkflowInstanceDetailForm(String workflowInstanceId){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowInstanceDetailFilterModel> fCtrl = new WorkflowInstanceDetailFilterController(workflowInstanceId); 
		FxmlForm<FilterController<WorkflowInstanceDetailFilterModel>> filterForm = new FxmlForm<>("workflowInstanceDetail.title",
				fCtrl, messageProvider);
		
		FilterResultController<WorkflowInstanceDetailFilterModel> resCtrl = new WorkflowInstanceDetailResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel>> resultForm = new FxmlForm<>("workflowInstanceDetail.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("workflowInstanceDetail.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
	
	public FilterAbleForm<EngineLoadFilterModel> createEngineLoadForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<EngineLoadFilterModel> fCtrl = new EngineLoadFilterController(); 
		FxmlForm<FilterController<EngineLoadFilterModel>> filterForm = new FxmlForm<>("engineLoad.title",
				fCtrl, messageProvider);
		
		FilterResultController<EngineLoadFilterModel> resCtrl = new EngineLoadResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<EngineLoadFilterModel>> resultForm = new FxmlForm<>("engineLoad.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<>("engineLoad.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm,guiCopperDataProvider);
	}
}
