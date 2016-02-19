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
package org.copperengine.monitoring.client.form;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;

public abstract class FormCreator<T extends Form> {
    public final String title;


    public FormCreator(String title) {
        this.title = title;
    }

    public FormCreator() {
        this("");
    }

    public MenuItem createShowFormMenuItem() {
        MenuItem menuItem = new MenuItem();
        menuItem.setText(title);
        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createForm().show();
            }
        });
        menuItem.setDisable(!enabled);
        return menuItem;
    }

    public ButtonBase createShowFormButton() {
        Button button = new Button(title);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createForm().show();
            }
        });
        return button;
    }

    public void show() {
        createForm().show();
    }

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }

    private Tooltip tooltip = null;

    public void setTooltip(Tooltip tooltip) {
        this.tooltip = tooltip;
    }

    public Tooltip getTooltip() {
        return tooltip;
    }

    public String getTitle(){
        return title;
    }

    protected abstract T createFormImpl();

    public T createForm(){
        T form = createFormImpl();
        form.displayedTitleProperty().set(title);
        form.setAllTitle(title);
        return form;
    }

    /**
     *
      * @param <C> Controller
     */
    public static class NonDisplayableFormCreator<C>{
        private final FormCreator<Form<C>> formCreator;

        public NonDisplayableFormCreator(FormCreator<Form<C>> formCreator) {
            this.formCreator = formCreator;
        }

        public Form.NonDisplayableForm<C> createForm(){
            return new Form.NonDisplayableForm(formCreator.createForm());
        }
    }
}