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
package org.copperengine.monitoring.client.ui.logs.result;

import java.util.Date;
import java.util.List;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.copperengine.monitoring.core.model.LogEvent;

public class LogsResultModel {

    public static class LogsRowModel {
        public final SimpleObjectProperty<Date> time;
        public final SimpleStringProperty message;
        public final SimpleStringProperty level;
        public final SimpleStringProperty locationInformation;

        public LogsRowModel(LogEvent logEvent) {
            time = new SimpleObjectProperty<Date>(logEvent.getTimeStamp());
            message = new SimpleStringProperty(logEvent.getMessage());
            level = new SimpleStringProperty(logEvent.getLevel());
            locationInformation = new SimpleStringProperty(logEvent.getLocationInformation());
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            fillString(builder);
            return builder.toString();
        }

        public void fillString(StringBuilder builder) {
            builder.append(time.get());
            builder.append("\t");
            builder.append(level.get());
            builder.append("\t");
            builder.append(String.format("%-100s", message.get()));
            builder.append("\t|");
            builder.append(locationInformation.get());
            builder.append("\n");
        }
    }

    public final SimpleStringProperty config;
    public final ObservableList<LogsRowModel> logs = FXCollections.observableArrayList();

    public LogsResultModel(String config, List<LogEvent> logevents) {
        super();
        this.config = new SimpleStringProperty(config);
        for (LogEvent logEvent : logevents) {
            logs.add(new LogsRowModel(logEvent));
        }
    }

}
