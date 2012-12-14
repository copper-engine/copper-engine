package de.scoopgmbh.copper.gui.factory;

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
import de.scoopgmbh.copper.gui.ui.workflowclasssesctree.WorkflowClassesTreeController;
import de.scoopgmbh.copper.gui.ui.workflowclasssesctree.WorkflowClassesTreeForm;
import de.scoopgmbh.copper.gui.ui.workflowinstance.filter.WorkflowInstanceFilterController;
import de.scoopgmbh.copper.gui.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowinstance.result.WorkflowInstanceResultController;
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterController;
import de.scoopgmbh.copper.gui.ui.workflowsummery.filter.WorkflowSummeryFilterModel;
import de.scoopgmbh.copper.gui.ui.workflowsummery.result.WorkflowSummeryResultController;
import de.scoopgmbh.copper.gui.util.MessageProvider;

public class FormFactory {
	private final TabPane mainTabPane;
	private final BorderPane mainPane;
	private FormGroup formGroup;
	private MessageProvider messageProvider;

	public TabPane getMainTabPane() {
		return mainTabPane;
	}

	GuiCopperDataProvider guiCopperDataProvider;
	public FormFactory(BorderPane mainPane, GuiCopperDataProvider guiCopperDataProvider, MessageProvider messageProvider) {
		this.mainTabPane = new TabPane();
		this.messageProvider = messageProvider;
		this.guiCopperDataProvider = guiCopperDataProvider;
		this.mainPane = mainPane;
		
		ArrayList<Form<?>> group = new ArrayList<>();
		group.add(createWorkflowSummeryForm());
		group.add(createWorkflowInstanceForm());
		group.add(createAudittrailForm());
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
		
		FilterResultController<WorkflowInstanceFilterModel> resCtrl = new WorkflowInstanceResultController(guiCopperDataProvider);
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
}
