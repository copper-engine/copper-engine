/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.ui.custommeasurepoint.result;

import java.util.List;

import org.copperengine.monitoring.core.statistic.TimeValuePair;

public class CustomMeasurePointResultModel {

    public final List<TimeValuePair<Double>> avg;
    public final List<TimeValuePair<Double>> count;
    public final List<TimeValuePair<Double>> quantil50;
    public final List<TimeValuePair<Double>> quantil90;
    public final List<TimeValuePair<Double>> quantil99;
    public final List<TimeValuePair<Double>> avgCpuCreator;

    public CustomMeasurePointResultModel(List<TimeValuePair<Double>> avg, List<TimeValuePair<Double>> count,
            List<TimeValuePair<Double>> quantil50, List<TimeValuePair<Double>> quantil90, List<TimeValuePair<Double>> quantil99, List<TimeValuePair<Double>> avgCpuCreator) {
        super();
        this.avg = avg;
        this.count = count;
        this.quantil50 = quantil50;
        this.quantil90 = quantil90;
        this.quantil99 = quantil99;
        this.avgCpuCreator = avgCpuCreator;
    }

}
