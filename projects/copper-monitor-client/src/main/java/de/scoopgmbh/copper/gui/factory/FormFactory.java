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
import de.scoopgmbh.copper.gui.ui.dynamicworkflow.DynamicWorkflowController;
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
		group.add(createDynamicWorkflowForm());
		group.add(createWorkflowClassesTreeForm());
		group.add(createWorkflowSummeryForm());
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
	
	public FilterAbleForm<WorkflowSummeryFilterModel> createWorkflowSummeryForm(){
		//same hacks are needed cause java cant handle generics as expected
		
		FilterController<WorkflowSummeryFilterModel> fCtrl = new WorkflowSummeryFilterController(); 
		FxmlForm<FilterController<WorkflowSummeryFilterModel>> filterForm = new FxmlForm<>("workflowsummeryFilter.title",
				fCtrl, messageProvider);
		
		FilterResultController<WorkflowSummeryFilterModel> resCtrl = new WorkflowSummeryResultController(guiCopperDataProvider);
		FxmlForm<FilterResultController<WorkflowSummeryFilterModel>> resultForm = new FxmlForm<>("workflowsummeryFilter.title",
				resCtrl, messageProvider);
		
		return new FilterAbleForm<WorkflowSummeryFilterModel>("workflowsummery.title", messageProvider,
				new TabPaneShowFormStrategie(mainTabPane), filterForm, resultForm);
	}
	
	public Form<DynamicWorkflowController> createWorkflowClassesTreeForm(){
		return new FxmlForm<>("dynamicworkflow.title", new DynamicWorkflowController(guiCopperDataProvider), messageProvider,  new TabPaneShowFormStrategie(mainTabPane));
	}
	
	public Form<DynamicWorkflowController> createDynamicWorkflowForm(){
		return new FxmlForm<>("dynamicworkflow.title",
				new DynamicWorkflowController(guiCopperDataProvider), messageProvider,  new TabPaneShowFormStrategie(mainTabPane));
	}
}
