/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;

public class FormCreatorGroup {
    public final List<FormCreatorGroup> childFormGroupCreators;
    public final List<FormCreator> childFormCreators;

    public final String title;

    public FormCreatorGroup(String title, List<FormCreatorGroup> childFormGroupCreators, List<FormCreator> childFormCreators) {
        this.title = title;
        this.childFormCreators = childFormCreators;
        this.childFormGroupCreators = childFormGroupCreators;
    }

    public FormCreatorGroup(FormCreator formCreator) {
        this(formCreator.getTitle(),null, Arrays.asList(formCreator));
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

    public Menu createMenu() {
        Menu menu = new Menu();
        if (menu.getText().isEmpty()) {
            menu.setText(title);
        }
        if (childFormCreators != null) {
            for (final FormCreator form : childFormCreators) {
                menu.getItems().add(form.createShowFormMenuItem());
            }
        }
        menu.setDisable(!isEnabled());
        return menu;
    }

    public MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        if (childFormGroupCreators != null) {
            for (final FormCreatorGroup form : childFormGroupCreators) {
                menuBar.getMenus().add(form.createMenu());
            }
        }
        return menuBar;
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

}