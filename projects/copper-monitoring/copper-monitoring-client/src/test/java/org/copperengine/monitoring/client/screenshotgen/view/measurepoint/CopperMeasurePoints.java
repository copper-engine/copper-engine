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
package org.copperengine.monitoring.client.screenshotgen.view.measurepoint;

import javafx.scene.layout.BorderPane;

import org.copperengine.monitoring.client.screenshotgen.view.fixture.ScreenshotPageBase;
import org.copperengine.monitoring.client.screenshotgen.view.fixture.TestFormContext;

public class CopperMeasurePoints extends ScreenshotPageBase {

    @Override
    public void initGui(BorderPane pane, TestFormContext testFormContext) {
        testFormContext.createMeasurePointForm().show();
    }

    @Override
    public String getTitle() {
        return "Copper measurepoints";
    }

}