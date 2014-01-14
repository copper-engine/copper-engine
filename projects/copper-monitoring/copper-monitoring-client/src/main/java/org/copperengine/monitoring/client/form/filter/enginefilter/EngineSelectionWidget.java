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
package org.copperengine.monitoring.client.form.filter.enginefilter;

import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import org.copperengine.monitoring.client.form.Widget;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo.EngineTyp;
import org.copperengine.monitoring.core.model.ProcessorPoolInfo;

public class EngineSelectionWidget implements Widget {
    private final EnginePoolFilterModel model;
    private final List<ProcessingEngineInfo> engineList;

    public EngineSelectionWidget(EnginePoolFilterModel model, List<ProcessingEngineInfo> engineList) {
        this.model = model;
        this.engineList = engineList;
    }

    @Override
    public Node createContent() {
        HBox pane = new HBox();
        pane.setAlignment(Pos.CENTER_LEFT);
        pane.setSpacing(3);
        final ChoiceBox<ProcessingEngineInfo> engineChoicebox = createEngineChoicebox();

        final ChoiceBox<ProcessorPoolInfo> poolChoicebox = createPoolChoicebox();
        pane.getChildren().add(new Label("Engine"));
        pane.getChildren().add(engineChoicebox);
        pane.getChildren().add(new Label("Pool"));
        pane.getChildren().add(poolChoicebox);

        engineChoicebox.getSelectionModel().selectFirst();
        return pane;
    }

    public ChoiceBox<ProcessingEngineInfo> createEngineChoicebox() {
        final ChoiceBox<ProcessingEngineInfo> engineChoicebox = new ChoiceBox<ProcessingEngineInfo>();
        engineChoicebox.setTooltip(new Tooltip("ProcessingEngine"));
        for (ProcessingEngineInfo engineFilter : engineList) {
            engineChoicebox.getItems().add(engineFilter);
        }
        engineChoicebox.setConverter(new StringConverter<ProcessingEngineInfo>() {
            @Override
            public String toString(ProcessingEngineInfo object) {
                return object.getId() + "(" + (object.getTyp() == EngineTyp.PERSISTENT ? "P" : "T") + ")";
            }

            @Override
            public ProcessingEngineInfo fromString(String string) {
                return null;
            }
        });
        engineChoicebox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ProcessingEngineInfo>() {
            @Override
            public void changed(ObservableValue<? extends ProcessingEngineInfo> observable, ProcessingEngineInfo oldValue,
                    ProcessingEngineInfo newValue) {
                model.selectedEngine.setValue(newValue);
            }
        });
        model.selectedEngine.addListener(new ChangeListener<ProcessingEngineInfo>() {
            @Override
            public void changed(ObservableValue<? extends ProcessingEngineInfo> observable, ProcessingEngineInfo oldValue,
                    ProcessingEngineInfo newValue) {
                updateEngineChoicebox(engineChoicebox, newValue);
            }
        });
        updateEngineChoicebox(engineChoicebox, model.selectedEngine.get());

        return engineChoicebox;
    }

    public ChoiceBox<ProcessorPoolInfo> createPoolChoicebox() {
        final ChoiceBox<ProcessorPoolInfo> poolChoicebox = new ChoiceBox<ProcessorPoolInfo>();
        poolChoicebox.setTooltip(new Tooltip("ProcessorPool"));

        model.selectedEngine.addListener(new ChangeListener<ProcessingEngineInfo>() {
            @Override
            public void changed(ObservableValue<? extends ProcessingEngineInfo> observable, ProcessingEngineInfo oldValue, ProcessingEngineInfo newValue) {
                updatePoolChoiceBox(poolChoicebox, newValue);
            }
        });
        updatePoolChoiceBox(poolChoicebox, model.selectedEngine.get());

        poolChoicebox.setConverter(new StringConverter<ProcessorPoolInfo>() {
            @Override
            public String toString(ProcessorPoolInfo object) {
                if (object == null)
                    return "any";
                return object.getId();
            }

            @Override
            public ProcessorPoolInfo fromString(String string) {
                return null;
            }
        });

        poolChoicebox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ProcessorPoolInfo>() {
            @Override
            public void changed(ObservableValue<? extends ProcessorPoolInfo> observable, ProcessorPoolInfo oldValue,
                    ProcessorPoolInfo newValue) {
                model.selectedPool.setValue(newValue);
            }
        });

        model.selectedPool.addListener(new ChangeListener<ProcessorPoolInfo>() {
            @Override
            public void changed(ObservableValue<? extends ProcessorPoolInfo> observable, ProcessorPoolInfo oldValue,
                    ProcessorPoolInfo newValue) {
                updatePoolChoicebox(poolChoicebox, newValue);
            }
        });

        updatePoolChoicebox(poolChoicebox, model.selectedPool.get());
        poolChoicebox.getSelectionModel().select(null);
        return poolChoicebox;
    }

    private void updateEngineChoicebox(final ChoiceBox<ProcessingEngineInfo> engineChoicebox, ProcessingEngineInfo newValue) {
        if (newValue != null) {
            for (ProcessingEngineInfo processingEngineInfo : engineChoicebox.getItems()) {
                if (processingEngineInfo.getId() != null && processingEngineInfo.getId().equals(newValue.getId())) {
                    engineChoicebox.getSelectionModel().select(processingEngineInfo);
                }
            }
        } else {
            engineChoicebox.getSelectionModel().clearSelection();
        }
    }

    private void updatePoolChoicebox(final ChoiceBox<ProcessorPoolInfo> poolChoicebox, ProcessorPoolInfo newValue) {
        if (newValue != null) {
            for (ProcessorPoolInfo pool : poolChoicebox.getItems()) {
                if (pool != null && pool.getId() != null && pool.getId().equals(newValue.getId())) {
                    poolChoicebox.getSelectionModel().select(pool);
                }
            }
        } else {
            poolChoicebox.getSelectionModel().clearSelection();
        }
    }

    private void updatePoolChoiceBox(final ChoiceBox<ProcessorPoolInfo> poolChoicebox, ProcessingEngineInfo newValue) {
        if (newValue != null) {
            poolChoicebox.getItems().clear();
            poolChoicebox.getItems().add(null);
            for (ProcessorPoolInfo processorPoolInfo : newValue.getPools()) {
                poolChoicebox.getItems().add(processorPoolInfo);
            }
            model.selectedPool.set(null);
            if (!newValue.getPools().isEmpty()) {
                model.selectedPool.set(newValue.getPools().get(0));
            }
        }
    }
}
