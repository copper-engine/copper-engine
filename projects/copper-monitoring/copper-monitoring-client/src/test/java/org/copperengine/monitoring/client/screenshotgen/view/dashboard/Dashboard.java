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
package org.copperengine.monitoring.client.screenshotgen.view.dashboard;

import javafx.scene.layout.BorderPane;

import org.copperengine.monitoring.client.screenshotgen.view.fixture.ScreenshotPageBase;
import org.copperengine.monitoring.client.screenshotgen.view.fixture.TestFormContext;
import org.jemmy.fx.control.LabeledDock;
import org.jemmy.fx.control.TabPaneDock;
import org.jemmy.fx.control.TextInputControlDock;

public class Dashboard extends ScreenshotPageBase {

    @Override
    public void initGui(BorderPane pane, TestFormContext testFormContext) {
        testFormContext.createDashboardForm().show();

    }

    public int setPoolSize(String size) {
        TextInputControlDock textInput = new TextInputControlDock(scene.asParent(), "nummerNew");
        textInput.type(size);
        LabeledDock setButton = new LabeledDock(scene.asParent(), "nummerbutton");
        setButton.mouse().click();
        return testDataProvider.numberOfThreads;
    }

    public int setBatcherPoolSize(String size) {
        TextInputControlDock textInput = new TextInputControlDock(scene.asParent(), "batcherNewNum");
        textInput.type(size);
        LabeledDock setButton = new LabeledDock(scene.asParent(), "batcherNumSet");
        setButton.mouse().click();
        return testDataProvider.numerOfThreadsBatcher;
    }

    public int setPriority(String size) {
        TextInputControlDock textInput = new TextInputControlDock(scene.asParent(), "prioNew");
        textInput.type(size);
        LabeledDock setButton = new LabeledDock(scene.asParent(), "prioButton");
        setButton.mouse().click();
        return testDataProvider.threadPriority;
    }

    public int getPoolsTabsCount() {
        TabPaneDock tabpane = new TabPaneDock(scene.asParent(), "pools");
        return tabpane.getTabs().size();
    }

    @Override
    public String getTitle() {
        return "Dashboard";
    }

}