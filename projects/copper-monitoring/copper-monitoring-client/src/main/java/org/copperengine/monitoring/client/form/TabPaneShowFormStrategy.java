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
package org.copperengine.monitoring.client.form;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class TabPaneShowFormStrategy extends ShowFormsStrategy<TabPane> {

    public TabPaneShowFormStrategy(TabPane component) {
        super(component);
    }

    @Override
    public void showImpl(final Form<?> form) {

        Tab formTab=null;
        for (Tab tab: component.getTabs()){
            if (tab.getContent()==form.getCachedContent()){
                formTab=tab;
            }
        }

        if (formTab==null){
            formTab = new Tab();
            formTab.textProperty().bind(form.displayedTitleProperty());
            formTab.setContent(form.getCachedContent());
            component.getTabs().add(formTab);
        }

        component.getSelectionModel().select(formTab);

        final Tab formTabFinal=formTab;
        formTab.setOnClosed(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                // workaround for javafx memeoryleaks RT-25652, RT-32087
                formTabFinal.setContent(null);
                formTabFinal.textProperty().unbind();
                formTabFinal.setOnClosed(null);

                form.close();
            }
        });

        formTab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if (((Tab)event.getSource()).isSelected()){
                    form.show();
                }
            }
        });

    }

    @Override
    protected void closeImpl(Form<?> form) {
        Tab formTab=null;
        for (Tab tab: component.getTabs()){
            if (tab.getContent()==form.getCachedContent()){
                formTab=tab;
            }
        }
        if (formTab!=null){
            component.getTabs().remove(formTab);
        }
    }
}