package de.scoopgmbh.copper.gui.factory;

import java.util.Arrays;
import java.util.ResourceBundle;

import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.BorderPaneShowFormStrategie;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.FormManager;
import de.scoopgmbh.copper.gui.form.FxmlForm;
import de.scoopgmbh.copper.gui.form.TabPaneShowFormStrategie;
import de.scoopgmbh.copper.gui.ui.dynamicworkflow.DynamicWorkflowController;
import de.scoopgmbh.copper.gui.ui.login.LoginController;
import de.scoopgmbh.copper.gui.util.MessageProvider;

public class MainFactory {

	private TabPane mainTabPane;
	private BorderPane mainPane;
	private FormManager formManager;
	MessageProvider messageProvider;
	private final Stage primaryStage;

	public TabPane getMainTabPane() {
		return mainTabPane;
	}

	public BorderPane getMainPane() {
		return mainPane;
	}

	public MainFactory(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.mainTabPane = new TabPane();
		this.mainPane = new BorderPane();
		messageProvider = new MessageProvider(ResourceBundle.getBundle("de.scoopgmbh.copper.gui.message"));
		
		formManager = new FormManager(Arrays.asList(createDynamicWorkflowForm()));
	}

	public Form<TabPane> createDynamicWorkflowForm(){
		return new FxmlForm<TabPane>(mainTabPane, "dynamicworkflow.title", "/de/scoopgmbh/copper/gui/ui/dynamicworkflow/DynamicWorkflow.fxml",
				new DynamicWorkflowController(new GuiCopperDataProvider(null)), messageProvider,  new TabPaneShowFormStrategie());
	}
	
	public Form<BorderPane> createLoginForm(){
		return new FxmlForm<BorderPane>(mainPane, "login.title", "/de/scoopgmbh/copper/gui/ui/login/Login.fxml",
				new LoginController(this), messageProvider,  new BorderPaneShowFormStrategie());
	}
	
	
	public MenuBar createMenueBar(){
		final MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(formManager.createMenue());
		menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
		return menuBar;
	}
}
