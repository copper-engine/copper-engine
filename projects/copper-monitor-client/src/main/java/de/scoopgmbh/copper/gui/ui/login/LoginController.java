package de.scoopgmbh.copper.gui.ui.login;

import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import de.scoopgmbh.copper.gui.factory.MainFactory;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.monitor.adapter.ServerLogin;

public class LoginController implements Initializable, FxmlController {
	private final MainFactory mainFactory;


    @FXML //  fx:id="allRadioButton"
    private RadioButton allRadioButton; // Value injected by FXMLLoader

    @FXML //  fx:id="jmxRadioButton"
    private RadioButton jmxRadioButton; // Value injected by FXMLLoader

    @FXML //  fx:id="readonlyRadioButton"
    private RadioButton readonlyRadioButton; // Value injected by FXMLLoader

    @FXML //  fx:id="serverRadioButton"
    private RadioButton serverRadioButton; // Value injected by FXMLLoader

    @FXML //  fx:id="startButton"
    private Button startButton; // Value injected by FXMLLoader

	
	public LoginController(MainFactory mainFactory) {
		super();
		this.mainFactory=mainFactory;
	}

	@Override
	public void initialize(final URL url, final ResourceBundle rb) {
		
		ToggleGroup groupConnection = new ToggleGroup();
		jmxRadioButton.setToggleGroup(groupConnection);
		jmxRadioButton.setSelected(true);
		serverRadioButton.setToggleGroup(groupConnection);
		
		ToggleGroup groupAccessmode = new ToggleGroup();
		allRadioButton.setToggleGroup(groupAccessmode);
		allRadioButton.setSelected(true);
		readonlyRadioButton.setToggleGroup(groupAccessmode);
		
		startButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent event) {
		    	
				Registry registry;
				try {
					registry = LocateRegistry.getRegistry("172.23.193.107",Registry.REGISTRY_PORT);
					ServerLogin serverLogin = (ServerLogin) registry.lookup(ServerLogin.class.getSimpleName());
					mainFactory.setGuiCopperDataProvider(serverLogin.login("", ""));
					mainFactory.getFormFactory().setupGUIStructure();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
		    }
		});
		
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("Login.fxml");
	}
}
