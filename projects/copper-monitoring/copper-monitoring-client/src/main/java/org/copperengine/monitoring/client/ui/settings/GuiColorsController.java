/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.ui.settings;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.copperengine.monitoring.client.ui.settings.CssConfig.Type;

public class GuiColorsController implements Initializable {
    private static final double SCALE = 0.75;

    private final Scene guiScene;
    private final SettingsModel settingsModel;
    private CssConfig currentCssConfig;
    private CssConfig cssLightConfig = CssConfig.createLight();
    private CssConfig cssDarkConfig = CssConfig.createDark();
    private CssConfig cssUrlConfig = CssConfig.createUrl();

    public GuiColorsController(Scene guiScene, SettingsModel settingsModel) {
        this.guiScene = guiScene;
        this.settingsModel = settingsModel;
    }

    @FXML
    // fx:id="radioLight"
    private RadioButton radioLight; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="radioDark"
    private RadioButton radioDark; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="radioCss"
    private RadioButton radioCss; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="cssColorGroup"
    private Group cssColorGroup; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="cssUrlGroup"
    private Group cssUrlGroup; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="picker1"
    private ColorPicker picker1; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="picker2"
    private ColorPicker picker2; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="picker3"
    private ColorPicker picker3; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="butDefaultColors"
    private Button butDefaultColors; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="cssUrl"
    private TextField cssUrl; // Value injected by FXMLLoader

    @FXML
    // fx:id="butFile"
    private Button butFile; // Value injected by FXMLLoader

    @FXML
    // fx:id="butSave"
    private Button butSave; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="butCancel"
    private Button butCancel; // Value injected by FXMLLoader

    @FXML
    // fx:id="previewPane"
    private VBox previewPane; // Value injected by FXMLLoader

    @FXML
    // fx:id="allControlsGroup"
    private Group allControlsGroup; // Value injected by FXMLLoader
    

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert radioLight != null : "fx:id=\"radioLight\" was not injected: check your FXML file 'Settings.fxml'.";
        assert radioDark != null : "fx:id=\"radioDark\" was not injected: check your FXML file 'Settings.fxml'.";
        assert radioCss != null : "fx:id=\"radioCss\" was not injected: check your FXML file 'Settings.fxml'.";
        assert cssColorGroup != null : "fx:id=\"cssColorGroup\" was not injected: check your FXML file 'Settings.fxml'.";
        assert cssUrlGroup != null : "fx:id=\"cssUrlGroup\" was not injected: check your FXML file 'Settings.fxml'.";
        assert picker1 != null : "fx:id=\"picker1\" was not injected: check your FXML file 'Settings.fxml'.";
        assert picker2 != null : "fx:id=\"picker2\" was not injected: check your FXML file 'Settings.fxml'.";
        assert picker3 != null : "fx:id=\"picker3\" was not injected: check your FXML file 'Settings.fxml'.";
        assert butDefaultColors != null : "fx:id=\"butDefaultColors\" was not injected: check your FXML file 'Settings.fxml'.";
        assert cssUrl != null : "fx:id=\"cssUrl\" was not injected: check your FXML file 'Settings.fxml'.";
        assert butFile != null : "fx:id=\"butFile\" was not injected: check your FXML file 'Settings.fxml'.";
        assert butSave != null : "fx:id=\"butSave\" was not injected: check your FXML file 'Settings.fxml'.";
        assert butCancel != null : "fx:id=\"butCancel\" was not injected: check your FXML file 'Settings.fxml'.";
        assert allControlsGroup != null : "fx:id=\"allControlsGroup\" was not injected: check your FXML file 'Settings.fxml'.";

        cssUrl.textProperty().bindBidirectional(cssUrlConfig.getCssUrlProperty());
        
        initPreview();
        initColorControls();
        updatePreview();
    }

    private void initColorControls() {
        currentCssConfig = new CssConfig(settingsModel.cssUri.get());
        Type type = currentCssConfig.getType();
        switch(type) {
            case CSS: cssUrlConfig = currentCssConfig; radioCss.setSelected(true); break;
            case DARK: cssDarkConfig = currentCssConfig; radioDark.setSelected(true); break;
            case LIGHT: cssLightConfig = currentCssConfig; radioLight.setSelected(true); break;
            default: throw new AssertionError("Unknown type: " + type);
        }

        updateColorControls();
    }
    
    private void initPreview() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Preview.fxml"));
        try {
            VBox preview = (VBox)loader.load();
            double width = preview.getPrefWidth();
            double height = preview.getPrefHeight();

            FlowPane flowPane = new FlowPane();
            flowPane.getChildren().add(preview);

            flowPane.setScaleX(SCALE);
            flowPane.setScaleY(SCALE);

            double translateX = -width * (1 - SCALE) / 2;
            double translateY = -height * (1 - SCALE) / 2;
            flowPane.setTranslateX(translateX);
            flowPane.setTranslateY(translateY);
            
            
            previewPane.getChildren().add(flowPane);
            
            
            double scaledWidth = width * SCALE;
            double scaledHeight = height * SCALE;
            previewPane.setPrefSize(scaledWidth, scaledHeight);

            previewPane.getScene();
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                  Window dialog = previewPane.getScene().getWindow();
                  
                  double currWidth = dialog.getWidth();
                  double currHeight = dialog.getHeight();
                  dialog.setWidth(Math.max(currWidth, 20 + previewPane.getLayoutX() + previewPane.getPrefWidth()));
                  dialog.setHeight(20 + currHeight + previewPane.getPrefHeight());
                  updatePreview();
                }
                
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void updatePreview() {
        if(previewPane != null && previewPane.getScene() != null) {
            try {
                CssConfigurator configurator = new CssConfigurator(currentCssConfig);            
                ObservableList<String> stylesheets = previewPane.getScene().getStylesheets();
                configurator.configure(stylesheets);
            } catch (IOException e) {
                e.printStackTrace();
            }        
        }
    }
    
    private void unbindColorProperties() {
        ObjectProperty<Color>[] lightProps = cssLightConfig.getColorProperties();
        picker1.valueProperty().unbindBidirectional(lightProps[0]);
        picker2.valueProperty().unbindBidirectional(lightProps[1]);
        picker3.valueProperty().unbindBidirectional(lightProps[2]);

        ObjectProperty<Color>[] darkProps = cssDarkConfig.getColorProperties();
        picker1.valueProperty().unbindBidirectional(darkProps[0]);
        picker2.valueProperty().unbindBidirectional(darkProps[1]);
        picker3.valueProperty().unbindBidirectional(darkProps[2]);            
    }
    
    private void refreshPickers() {
        // javafx bug workaround else color choosers won't update (http://javafx-jira.kenai.com/browse/RT-26633)
        picker1.fireEvent(new ActionEvent());
        picker2.fireEvent(new ActionEvent());
        picker3.fireEvent(new ActionEvent());        
    }

    public void updateColorControls() {
        Type type = currentCssConfig.getType();
        unbindColorProperties();
        if(type == Type.CSS) {
            cssUrlGroup.setVisible(true);
            cssColorGroup.setVisible(false);
            
            cssUrl.setText(currentCssConfig.getCssUrl());        
        } else {
            cssColorGroup.setVisible(true);
            cssUrlGroup.setVisible(false);

            ObjectProperty<Color>[] props = currentCssConfig.getColorProperties();
            picker1.valueProperty().bindBidirectional(props[0]);
            picker2.valueProperty().bindBidirectional(props[1]);
            picker3.valueProperty().bindBidirectional(props[2]);            
            refreshPickers();
        }
    }
    
    public void useLightType() {
        currentCssConfig = cssLightConfig;
        updateColorControls();        
        updatePreview();
    }
    
    public void useDarkType() {
        currentCssConfig = cssDarkConfig;
        updateColorControls();        
        updatePreview();
    }
    
    public void useCssType() {
        currentCssConfig = cssUrlConfig;
        updateColorControls();        
        updatePreview();
    }
    
    public void useDefaultColors() {
        Type type = currentCssConfig.getType();
        if(type == Type.CSS) return;
        Color[] defaultColors = (type == Type.LIGHT) ? CssConfig.DEFAULT_LIGHT_COLORS : CssConfig.DEFAULT_DARK_COLORS;
        ObjectProperty<Color>[] colorProps = currentCssConfig.getColorProperties();
        for(int i=0; i<CssConfig.COLOR_COUNT; i++) {
            colorProps[i].setValue(defaultColors[i]);
        }
        refreshPickers();
    }
    
    public void chooseCssFile() {
        File cssDir;
        try {
            URL currUrl = new URL(cssUrl.textProperty().getValue());
            String path = currUrl.getPath();
            File file = new File(path);
            cssDir = file.getParentFile();
        } catch (Exception e) {
            cssDir = null;
        }
        if(cssDir == null) {
            cssDir = new File(System.getProperty("user.dir"));
        }
        FileChooser fileChooser = new FileChooser();
        if(cssDir.isDirectory()) {
            fileChooser.setInitialDirectory(cssDir);
        }
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                String url = file.toURI().toURL().toExternalForm();
                cssUrl.textProperty().setValue(url);
                updatePreview();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }            
    }
    
    public void saveColorSettings() {
        settingsModel.cssUri.setValue(currentCssConfig.toString());
        
        CssConfigurator cssConfigurator = new CssConfigurator(currentCssConfig);
        try {
            cssConfigurator.configure(guiScene.getStylesheets());
        } catch (IOException e) {
            e.printStackTrace();
        }
        close();
    }
    
    public void useCurrentTheme() {
        initColorControls();
    }
    
    public void cancelColorSettings() {
        close();
    }
    
    public void close() {
        previewPane.getScene().getWindow().hide();
    }
}
