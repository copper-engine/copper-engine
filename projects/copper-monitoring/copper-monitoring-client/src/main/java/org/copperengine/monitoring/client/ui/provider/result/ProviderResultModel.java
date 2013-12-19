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
package org.copperengine.monitoring.client.ui.provider.result;

import java.util.Date;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import org.copperengine.monitoring.core.model.GenericMonitoringData;
import org.copperengine.monitoring.core.model.GenericMonitoringData.ContentType;

public class ProviderResultModel {

    public final SimpleObjectProperty<Date> timeStamp;
    public final SimpleStringProperty content;
    public final SimpleObjectProperty<ContentType> contentType;
    public final SimpleStringProperty creatorId;

    public ProviderResultModel(GenericMonitoringData genericMonitoringData) {
        timeStamp = new SimpleObjectProperty<Date>(genericMonitoringData.getTimeStamp());
        content = new SimpleStringProperty(genericMonitoringData.getContent());
        contentType = new SimpleObjectProperty<ContentType>(genericMonitoringData.getContentType());
        creatorId = new SimpleStringProperty(genericMonitoringData.getCreatorId());
    }

}
