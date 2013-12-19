/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.monitoring.client.form;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;

public class TabPaneShowFormStrategie extends ShowFormStrategy<TabPane> {
    boolean transparentHeader = false;
    private Tab tab;
    private boolean forceTabSizeTocontentesize;

    public TabPaneShowFormStrategie(TabPane component) {
        super(component);

        tab = new Tab();
        tab.setText("new tab");
    }

    public TabPaneShowFormStrategie(TabPane component, boolean forceTabSizeTocontentesize) {
        this(component);
        this.forceTabSizeTocontentesize = forceTabSizeTocontentesize;
    }

    @Override
    public void show(final Form<?> form) {
        if (tab.getContent() == null) {
            tab.setContent(form.createContent());
            tab.textProperty().bind(form.displayedTitleProperty());
            tab.getContent().setStyle("-fx-background-color: transparent;");
            component.getTabs().add(tab);
        } else {
            if (!component.getTabs().contains(tab)) {
                component.getTabs().add(tab);
            }
        }
        if (forceTabSizeTocontentesize) {
            if (tab.getContent() instanceof Region) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        component.setMinWidth(((Region) tab.getContent()).getPrefWidth());
                        component.setMinHeight(((Region) tab.getContent()).getPrefHeight() + 42);
                    }
                });
            }
        }
        component.getSelectionModel().select(tab);

        tab.setOnClosed(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                // workaround for javafx memeoryleaks RT-25652, RT-32087
                onCloseListener.closed(form);
                onCloseListener = null;
                tab.setContent(null);
                tab.setContent(null);
                tab.textProperty().unbind();
                tab.setOnClosed(null);
            }
        });

    }
}