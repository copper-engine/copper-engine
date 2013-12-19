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
package org.copperengine.monitoring.core.statistic;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.copperengine.monitoring.core.statistic.converter.TimeConverter;
import org.junit.Test;

public class TimeframeGroupTest {

    @Test
    public void test_factory_method() {
        TimeframeGroup<Date, TimeValuePair<Double>> group = TimeframeGroup.<Date, TimeValuePair<Double>> createGroups(3, new Date(0), new Date(12), new CountAggregateFunction<Date>(), new TimeConverter<Date>() {
            private static final long serialVersionUID = -4045708565200297994L;

            @Override
            public Date getTime(Date value) {
                return value;
            }
        });
        assertEquals(0, group.from.getTime());
        assertEquals(4, group.to.getTime());

        group = group.nextGroup();
        assertEquals(4, group.from.getTime());
        assertEquals(8, group.to.getTime());

    }

}
