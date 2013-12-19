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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @param <T>
 * @param <R>
 *            aggregate result typ
 */
public class StatisticCreator<T, R extends Serializable> implements Serializable {
    private static final long serialVersionUID = -8510844252874340757L;

    private TimeframeGroup<T, R> currentGroupFunction;

    public StatisticCreator(TimeframeGroup<T, R> firstGroupFunctions) {
        super();
        this.currentGroupFunction = firstGroupFunctions;
        usedGroups.add(currentGroupFunction);
    }

    List<TimeframeGroup<T, R>> usedGroups = new ArrayList<TimeframeGroup<T, R>>();

    /**
     * first value must be in the first group
     * 
     * @param listvalue
     */
    public void add(T listvalue) {
        if (currentGroupFunction.isInGroup(listvalue)) {
            currentGroupFunction.addToGroup(listvalue);
        } else {
            currentGroupFunction.doAggregateAndSaveResult();
            currentGroupFunction.clear();
            currentGroupFunction = currentGroupFunction.nextGroup();
            if (currentGroupFunction != null) {
                usedGroups.add(currentGroupFunction);
                add(listvalue);

            }
        }
    };

    public List<R> getAggregatedResult() {
        ArrayList<R> result = new ArrayList<R>();
        for (TimeframeGroup<T, R> groupFunction : usedGroups) {
            if (!groupFunction.isAggregated()) {// for last group
                groupFunction.doAggregateAndSaveResult();
                groupFunction.clear();
            }
            result.add(groupFunction.getAggregate());
        }
        return result;
    }

}