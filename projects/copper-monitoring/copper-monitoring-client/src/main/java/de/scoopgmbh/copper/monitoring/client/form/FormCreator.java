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

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;

public class FormCreator {
    public final List<FormCreator> childFormCreators;
    public final String staticTitle;

    public FormCreator(String staticTitle) {
        this(staticTitle, null);
    }

    public FormCreator(String staticTitle, List<FormCreator> childFormCreators) {
        this.staticTitle = staticTitle;
        this.childFormCreators = childFormCreators;
    }

    private Form<?> createFormInternal() {
        Form<?> form = createForm();
        form.displayedTitleProperty().set(staticTitle);
        form.setStaticTitle(staticTitle);
        return form;
    }

    public MenuItem createShowFormMenuItem() {
        MenuItem menuItem = new MenuItem();
        menuItem.setText(staticTitle);
        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createFormInternal().show();
            }
        });
        return menuItem;
    }

    public ButtonBase createShowFormButton() {
        Button button = new Button(staticTitle);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createFormInternal().show();
            }
        });
        return button;
    }

    public void show() {
        createFormInternal().show();
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

    public void createMenu(Menu menu) {
        if (menu.getText().isEmpty()) {
            menu.setText(staticTitle);
        }
        if (childFormCreators != null) {
            for (final FormCreator form : childFormCreators) {
                form.createMenu(menu);
            }
        } else {
            menu.setDisable(!isEnabled());
            menu.getItems().add(createShowFormMenuItem());
        }
    }

    public void createMenuBar(MenuBar menuBar) {
        if (childFormCreators != null) {
            for (final FormCreator form : childFormCreators) {
                Menu menu = new Menu();
                menu.setDisable(!isEnabled());
                form.createMenu(menu);
                menuBar.getMenus().add(menu);
            }
        }
    }

    public List<Node> createButtonList() {
        ArrayList<Node> result = new ArrayList<Node>();
        for (final FormCreator form : childFormCreators) {
            Control createShowFormButton = form.createShowFormButton();
            createShowFormButton.setDisable(!form.isEnabled());
            createShowFormButton.setTooltip(form.getTooltip());

            if (!form.isEnabled()) {/*
                                     * workaround disabled button must be wrapped in split pane to show tooltip
                                     * https://javafx-jira.kenai.com/browse/RT-28850
                                     */
                SplitPane wrapper = new SplitPane();
                wrapper.getItems().add(createShowFormButton);
                createShowFormButton = wrapper;
                wrapper.setTooltip(form.getTooltip());
            }

            result.add(createShowFormButton);
        }
        return result;
    }

    public Form<?> createForm() {
        return null;
    }

}