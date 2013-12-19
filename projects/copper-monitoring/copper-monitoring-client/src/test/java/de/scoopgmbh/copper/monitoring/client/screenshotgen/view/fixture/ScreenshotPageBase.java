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
package de.scoopgmbh.copper.monitoring.client.screenshotgen.view.fixture;

import javafx.scene.layout.BorderPane;

import org.jemmy.fx.SceneDock;

import de.scoopgmbh.copper.monitoring.client.screenshotgen.ScreenshotGeneratorMain;

/** Basic testcase for Concordion */
public abstract class ScreenshotPageBase {

    public abstract void initGui(BorderPane pane, TestFormContext testFormContext);

    public abstract String getTitle();

    protected SceneDock scene;
    protected TestDataProvider testDataProvider;

    public void setScene(SceneDock scene) {
        this.scene = scene;
    }

    public void setTestDataProvider(TestDataProvider testDataProvider) {
        this.testDataProvider = testDataProvider;
    }

    public ScreenshotPageBase() {
        super();
    }

    public long getWaitForInitGuiMs() {
        return ScreenshotGeneratorMain.LONG_WAIT_TIME;
    }

}