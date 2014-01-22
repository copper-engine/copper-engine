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
package org.copperengine.monitoring.client.screenshotgen.view.fixture;

import javafx.scene.layout.BorderPane;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.context.FormContext;
import org.copperengine.monitoring.client.form.BorderPaneShowFormStrategie;
import org.copperengine.monitoring.client.form.ShowFormsStrategy;
import org.copperengine.monitoring.client.form.dialog.DefaultInputDialogCreator;
import org.copperengine.monitoring.client.form.issuereporting.IssueReporter;
import org.copperengine.monitoring.client.ui.settings.SettingsModel;
import org.copperengine.monitoring.client.util.MessageProvider;

public class TestFormContext extends FormContext {

    public TestFormContext(BorderPane mainPane, GuiCopperDataProvider guiCopperDataProvider, MessageProvider messageProvider,
            SettingsModel settingsModelSingleton) {
        super(mainPane, guiCopperDataProvider, messageProvider, settingsModelSingleton, new IssueReporter() {

            @Override
            public void reportError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void reportError(String message, Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void reportError(String message, Throwable e, Runnable finishAction) {
                e.printStackTrace();
            }

            @Override
            public void reportWarning(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void reportWarning(String message, Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void reportWarning(String message, Throwable e, Runnable finishAction) {
                e.printStackTrace();
            }
        }, new DefaultInputDialogCreator(null));
    }

    @Override
    protected ShowFormsStrategy<?> getDefaultShowFormStrategy() {
        return new BorderPaneShowFormStrategie(mainPane);
    }

}
