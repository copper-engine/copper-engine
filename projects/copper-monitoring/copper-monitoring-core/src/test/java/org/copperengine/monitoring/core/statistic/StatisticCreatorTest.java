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
package org.copperengine.monitoring.core.statistic;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.copperengine.monitoring.core.statistic.converter.DoubleConverter;
import org.copperengine.monitoring.core.statistic.converter.TimeConverter;
import org.junit.Test;

public class StatisticCreatorTest {

    private class Pair extends TimeValuePair<Double> {
        private static final long serialVersionUID = 5132976891911560776L;

        public Pair(Date date, Double value) {
            super(date, value);
        }
    }

    @Test
    public void test_group_avg() {

        final DoubleConverter<TimeValuePair<Double>> doubleConverter = new DoubleConverter<TimeValuePair<Double>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public double getDouble(TimeValuePair<Double> value) {
                return value.value;
            }
        };

        AvgAggregateFunction<TimeValuePair<Double>> avg = new AvgAggregateFunction<TimeValuePair<Double>>(doubleConverter);
        final TimeConverter<TimeValuePair<Double>> dateConverter = new TimeConverter<TimeValuePair<Double>>() {
            private static final long serialVersionUID = -5711345254694347322L;

            @Override
            public Date getTime(TimeValuePair<Double> value) {
                return value.date;
            }
        };

        TimeframeGroup<TimeValuePair<Double>, TimeValuePair<Double>> group = new TimeframeGroup<TimeValuePair<Double>, TimeValuePair<Double>>(avg, new Date(0), new Date(5), dateConverter);

        StatisticCreator<TimeValuePair<Double>, TimeValuePair<Double>> statisticCreator = new StatisticCreator<TimeValuePair<Double>, TimeValuePair<Double>>(group);

        statisticCreator.add(new Pair(new Date(0), 2.0));
        statisticCreator.add(new Pair(new Date(0), 2.0));
        statisticCreator.add(new Pair(new Date(5), 6.0));
        statisticCreator.add(new Pair(new Date(5), 6.0));

        List<TimeValuePair<Double>> result = statisticCreator.getAggregatedResult();
        assertEquals(2, result.size());

        assertEquals(2.0, result.get(0).value, 0.000001);
        assertEquals(6.0, result.get(1).value, 0.000001);
    }

    @Test
    public void test_group_count() {
        CountAggregateFunction<Pair> count = new CountAggregateFunction<Pair>();
        TimeframeGroup<Pair, TimeValuePair<Double>> group = new TimeframeGroup<Pair, TimeValuePair<Double>>(count, new Date(0), new Date(5), new TimeConverter<Pair>() {
            private static final long serialVersionUID = 8575974750093890171L;

            @Override
            public Date getTime(Pair value) {
                return value.date;
            }
        });

        StatisticCreator<Pair, TimeValuePair<Double>> statisticCreator = new StatisticCreator<Pair, TimeValuePair<Double>>(group);

        statisticCreator.add(new Pair(new Date(0), 2.0));
        statisticCreator.add(new Pair(new Date(0), 2.0));
        statisticCreator.add(new Pair(new Date(5), 6.0));
        statisticCreator.add(new Pair(new Date(5), 6.0));
        statisticCreator.add(new Pair(new Date(5), 6.0));

        List<TimeValuePair<Double>> result = statisticCreator.getAggregatedResult();
        assertEquals(2, result.size());

        assertEquals(2l, result.get(0).value.longValue());
        assertEquals(3l, result.get(1).value.longValue());
    }

    @Test
    public void test_empty_groups() {
        CountAggregateFunction<Pair> count = new CountAggregateFunction<Pair>();
        TimeframeGroup<Pair, TimeValuePair<Double>> group = new TimeframeGroup<Pair, TimeValuePair<Double>>(count, new Date(0), new Date(5), new TimeConverter<Pair>() {
            private static final long serialVersionUID = 4477205822827368748L;

            @Override
            public Date getTime(Pair value) {
                return value.date;
            }
        });

        StatisticCreator<Pair, TimeValuePair<Double>> statisticCreator = new StatisticCreator<Pair, TimeValuePair<Double>>(group);

        statisticCreator.add(new Pair(new Date(0), 2.0));
        // 5-10 empty
        // 10-15 empty
        // 15-20 empty
        statisticCreator.add(new Pair(new Date(20), 6.0));
        statisticCreator.add(new Pair(new Date(20), 6.0));

        List<TimeValuePair<Double>> result = statisticCreator.getAggregatedResult();
        assertEquals(5, result.size());

        assertEquals(1l, result.get(0).value.longValue());
        assertEquals(0l, result.get(1).value.longValue());
        assertEquals(0l, result.get(2).value.longValue());
        assertEquals(0l, result.get(3).value.longValue());
        assertEquals(2l, result.get(4).value.longValue());
    }

}
