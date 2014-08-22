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
package org.copperengine.monitoring.client.ui.configuration.result;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import org.copperengine.monitoring.client.form.Form;
import org.copperengine.monitoring.client.ui.configuration.result.engines.ProcessingEnginesController;
import org.copperengine.monitoring.client.ui.configuration.result.provider.ProviderController;
import org.copperengine.monitoring.core.model.MonitoringDataProviderInfo;

public interface ConfigurationDependencyFactory {

    public Form<ProcessingEnginesController> createEnginesForm(Pane target);

    public Form<ProviderController> createMonitoringDataProviderForm(MonitoringDataProviderInfo monitoringDataProviderInfo, BorderPane target);
}
