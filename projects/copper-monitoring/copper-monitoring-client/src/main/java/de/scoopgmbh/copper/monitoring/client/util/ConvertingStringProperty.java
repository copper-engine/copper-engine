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
package de.scoopgmbh.copper.monitoring.client.util;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;

public class ConvertingStringProperty<T> extends SimpleStringProperty {

    public ConvertingStringProperty(final Property<T> property, final StringConverter<T> converter) {
        this.set(converter.toString(property.getValue()));
        this.addListener(new ChangeListener<String>() { // TODO check performance impact
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                property.setValue(converter.fromString(newValue));
            }
        });
    }

    public ConvertingStringProperty(final T row, final StringConverter<T> converter) {
        this.set(converter.toString(row));
        this.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                converter.fromString(newValue);
            }
        });
    }

}
