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
package org.copperengine.monitoring.core.statistic;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.copperengine.monitoring.core.statistic.converter.DoubleConverter;

public class QuantilAggregateFunction<T> implements AggregateFunction<T, TimeValuePair<Double>> {
    private static final long serialVersionUID = -714208820060517375L;
    private DoubleConverter<T> doubleConverter;
    private double quantil;

    /**
     * 0 bis 1 e.g 0.5 median
     *
     * @param quantil
     */
    public QuantilAggregateFunction(double quantil, DoubleConverter<T> doubleConverter) {
        super();
        this.doubleConverter = doubleConverter;
        this.quantil = quantil;
    }

    @Override
    public TimeValuePair<Double> doAggregate(List<T> groupContent, TimeframeGroup<T, TimeValuePair<Double>> usedGroup) {
        if (groupContent.isEmpty()) {
            return new TimeValuePair<Double>(usedGroup.to, 0d);
        }
        Collections.sort(groupContent, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return Double.compare(doubleConverter.getDouble(o1), doubleConverter.getDouble(o2));
            }
        });
        double result = doubleConverter.getDouble(groupContent.get((int) (groupContent.size() * quantil)));
        return new TimeValuePair<Double>(usedGroup.to, result);
    }

}