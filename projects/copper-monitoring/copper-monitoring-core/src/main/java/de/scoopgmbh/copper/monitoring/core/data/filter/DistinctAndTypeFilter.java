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
package de.scoopgmbh.copper.monitoring.core.data.filter;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class DistinctAndTypeFilter<T> extends MonitoringDataFilter<T> {
    private static final long serialVersionUID = 2601396965721427244L;

    Set<T> found;

    public DistinctAndTypeFilter(Class<T> clazz, Comparator<T> comparator) {
        super(clazz);
        found = new TreeSet<T>(comparator);
    }

    @Override
    protected boolean isValidImpl(T value) {
        return found.add(value);
    }

}
