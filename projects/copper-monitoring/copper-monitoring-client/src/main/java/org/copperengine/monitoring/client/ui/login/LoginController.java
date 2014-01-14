/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.ui.login;

import java.net.URL;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.copperengine.monitoring.client.context.ApplicationContext;
import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.ui.settings.SettingsModel;

public class LoginController implements Initializable, FxmlController {
    private final ApplicationContext mainFactory;
    private final SettingsModel settingsModelSingleton;

    public LoginController(ApplicationContext mainFactory, SettingsModel settingsModelSingleton) {
        super();
        this.mainFactory = mainFactory;
        this.settingsModelSingleton = settingsModelSingleton;
    }

    @FXML
    // fx:id="password"
    private PasswordField password; // Value injected by FXMLLoader

    @FXML
    // fx:id="serverAdress"
    private TextField serverAdress; // Value injected by FXMLLoader

    @FXML
    // fx:id="serverRadioButton"
    private RadioButton serverRadioButton; // Value injected by FXMLLoader

    @FXML
    // fx:id="startButton"
    private Button startButton; // Value injected by FXMLLoader

    @FXML
    // fx:id="user"
    private TextField user; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'Login.fxml'.";
        assert serverAdress != null : "fx:id=\"serverAdress\" was not injected: check your FXML file 'Login.fxml'.";
        assert serverRadioButton != null : "fx:id=\"serverRadioButton\" was not injected: check your FXML file 'Login.fxml'.";
        assert startButton != null : "fx:id=\"startButton\" was not injected: check your FXML file 'Login.fxml'.";
        assert user != null : "fx:id=\"user\" was not injected: check your FXML file 'Login.fxml'.";

        ToggleGroup groupConnection = new ToggleGroup();
        serverRadioButton.setSelected(true);
        serverRadioButton.setToggleGroup(groupConnection);

        user.disableProperty().bind(serverRadioButton.selectedProperty().not());
        password.disableProperty().bind(serverRadioButton.selectedProperty().not());
        serverAdress.disableProperty().bind(serverRadioButton.selectedProperty().not());

        startButton.disableProperty().bind(serverAdress.textProperty().isEqualTo(""));

        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if (serverRadioButton.isSelected()) {
                        cleanup();
                        mainFactory.setHttpGuiCopperDataProvider(serverAdress.getText(), user.getText(), password.getText());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        serverAdress.textProperty().bindBidirectional(settingsModelSingleton.lastConnectedServer);

    }

    private void cleanup() {
        serverAdress.textProperty().unbindBidirectional(settingsModelSingleton.lastConnectedServer);
    }

    public void addshorstcut() {
        EventHandler<KeyEvent> eventHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    if (!startButton.isDisabled()) {
                        startButton.fire();
                    }
                }
            }
        };
        user.setOnKeyReleased(eventHandler);
        password.setOnKeyReleased(eventHandler);
        serverAdress.setOnKeyReleased(eventHandler);
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("Login.fxml");
    }
}
