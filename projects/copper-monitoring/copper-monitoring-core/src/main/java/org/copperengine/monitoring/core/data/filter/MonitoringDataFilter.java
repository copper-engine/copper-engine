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
package org.copperengine.monitoring.core.data.filter;

import java.io.Serializable;

public abstract class MonitoringDataFilter<T> implements Serializable {

    private static final long serialVersionUID = -5171173614183598375L;

    Class<T> clazz;

    public MonitoringDataFilter(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    public boolean isValid(Object value) {
        if (value != null && value.getClass().isAssignableFrom(clazz)) {
            return isValidImpl(clazz.cast(value));
        } else {
            return false;
        }
    }

    public T castValid(Object value) {
        return clazz.cast(value);
    }

    protected abstract boolean isValidImpl(T value);

}
