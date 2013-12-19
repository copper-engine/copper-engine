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
package de.scoopgmbh.copper.monitoring.core.statistic;

import java.util.List;

import de.scoopgmbh.copper.monitoring.core.statistic.converter.DoubleConverter;

public class AvgAggregateFunction<T> implements AggregateFunction<T, TimeValuePair<Double>> {
    private static final long serialVersionUID = 4882013677988826331L;

    private final DoubleConverter<T> doubleConverter;

    public AvgAggregateFunction(DoubleConverter<T> doubleConverter) {
        super();
        this.doubleConverter = doubleConverter;
    }

    @Override
    public TimeValuePair<Double> doAggregate(List<T> group, TimeframeGroup<T, TimeValuePair<Double>> usedGroup) {
        double sum = 0;
        for (T value : group) {
            sum += doubleConverter.getDouble(value);
        }
        int size = group.size();
        if (size == 0) {
            size = 1;
        }
        return new TimeValuePair<Double>(usedGroup.from, sum / size);
    }

}