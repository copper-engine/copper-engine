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
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import de.scoopgmbh.copper.gui.context.ApplicationContext;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.monitor.adapter.ServerLogin;

public class LoginController implements Initializable, FxmlController {
	private final ApplicationContext mainFactory;
	public LoginController(ApplicationContext mainFactory) {
		super();
		this.mainFactory=mainFactory;
	}

    @FXML //  fx:id="jmxRadioButton"
    private RadioButton jmxRadioButton; // Value injected by FXMLLoader

    @FXML //  fx:id="password"
    private PasswordField password; // Value injected by FXMLLoader

    @FXML //  fx:id="serverAdress"
    private TextField serverAdress; // Value injected by FXMLLoader

    @FXML //  fx:id="serverRadioButton"
    private RadioButton serverRadioButton; // Value injected by FXMLLoader

    @FXML //  fx:id="startButton"
    private Button startButton; // Value injected by FXMLLoader

    @FXML //  fx:id="user"
    private TextField user; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert jmxRadioButton != null : "fx:id=\"jmxRadioButton\" was not injected: check your FXML file 'Login.fxml'.";
        assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'Login.fxml'.";
        assert serverAdress != null : "fx:id=\"serverAdress\" was not injected: check your FXML file 'Login.fxml'.";
        assert serverRadioButton != null : "fx:id=\"serverRadioButton\" was not injected: check your FXML file 'Login.fxml'.";
        assert startButton != null : "fx:id=\"startButton\" was not injected: check your FXML file 'Login.fxml'.";
        assert user != null : "fx:id=\"user\" was not injected: check your FXML file 'Login.fxml'.";
		
		ToggleGroup groupConnection = new ToggleGroup();
		jmxRadioButton.setToggleGroup(groupConnection);
		jmxRadioButton.setSelected(true);
		serverRadioButton.setToggleGroup(groupConnection);
		
		startButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override 
		    public void handle(ActionEvent event) {
				Registry registry;
				try {
					registry = LocateRegistry.getRegistry("localhost",Registry.REGISTRY_PORT);
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
