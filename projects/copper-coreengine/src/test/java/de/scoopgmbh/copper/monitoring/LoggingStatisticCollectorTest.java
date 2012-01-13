/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.monitoring;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

public class LoggingStatisticCollectorTest extends TestCase {

	public final void testPrint() {
		LoggingStatisticCollector collector = new LoggingStatisticCollector();
		collector.submit("insertIntoA", 100, 20, TimeUnit.MILLISECONDS);
		collector.submit("insertIntoA", 101, 21, TimeUnit.MILLISECONDS);
		collector.submit("insertIntoA", 102, 22, TimeUnit.MILLISECONDS);
		collector.submit("insertIntoB45", 102, 0, TimeUnit.MILLISECONDS);
		
		String x = collector.print();
		System.out.println(x);
	}

}
