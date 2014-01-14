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
package org.copperengine.core.monitoring;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.copperengine.core.monitoring.LoggingStatisticCollector.Filter;

public class RegexStatisticsFilter implements Filter {
    private final Pattern pattern;

    public RegexStatisticsFilter(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public RegexStatisticsFilter(String regex, int flags) {
        this.pattern = Pattern.compile(regex, flags);
    }

    @Override
    public boolean accept(String measurePointId, int elementCount, long elapsedTime, TimeUnit timeUnit) {
        return pattern.matcher(measurePointId).matches();
    }
}
