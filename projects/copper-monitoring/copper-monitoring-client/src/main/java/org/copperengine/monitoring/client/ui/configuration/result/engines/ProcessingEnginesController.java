/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.monitoring.client.ui.configuration.result.engines;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;

import org.copperengine.monitoring.client.form.Form;
import org.copperengine.monitoring.client.form.FormCreator;
import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.form.TabPaneShowFormStrategy;
import org.copperengine.monitoring.client.ui.configuration.result.engine.ProcessingEngineController;
import org.copperengine.monitoring.core.model.ConfigurationInfo;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;

public class ProcessingEnginesController implements Initializable, FxmlController {

    FormCreator.NonDisplayableFormCreator<ProcessingEngineController> processingEngineFormCreator;
    public ProcessingEnginesController(FormCreator.NonDisplayableFormCreator<ProcessingEngineController> processingEngineFormCreator) {
        super();
        this.processingEngineFormCreator = processingEngineFormCreator;
    }

    @FXML //  fx:id="engines"
    private TabPane engines; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert engines != null : "fx:id=\"engines\" was not injected: check your FXML file 'ProcessingEngines.fxml'.";

        engines.getStyleClass().add("floating");// transparent tabheader


    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("ProcessingEngines.fxml");
    }

    Map<String,Form<ProcessingEngineController>> idToProcessingEngines = new HashMap<String,Form<ProcessingEngineController>>();

    public void update(ConfigurationInfo configurationInfo){
        final TabPaneShowFormStrategy showFormStrategy = new TabPaneShowFormStrategy(engines);

        //remove no longer available
        Iterator<Map.Entry<String,Form<ProcessingEngineController>>> iterator=  idToProcessingEngines.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String,Form<ProcessingEngineController>> entry = iterator.next();
            String engineId = entry.getKey();
            boolean containsId=false;
            for (ProcessingEngineInfo processingEngineInfo: configurationInfo.getEngines()) {
                if (engineId.equals(processingEngineInfo.getId())){
                    containsId=true;
                    break;
                }
            }
            if (!containsId){
                entry.getValue().close();
                iterator.remove();
            }
        }

        //add new and update existing
        for (ProcessingEngineInfo processingEngineInfo: configurationInfo.getEngines()) {
            Form<ProcessingEngineController> processingEngineForm = idToProcessingEngines.get(processingEngineInfo.getId());
            if (processingEngineForm==null){
                processingEngineForm=processingEngineFormCreator.createForm().convertToDisplayAble(showFormStrategy);
                processingEngineForm.setAllTitle(processingEngineInfo.getId());
                idToProcessingEngines.put(processingEngineInfo.getId(),processingEngineForm);
                processingEngineForm.show();
            }
            processingEngineForm.getController().update(processingEngineInfo);
        }
    }

}
