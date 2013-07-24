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
package de.scoopgmbh.copper.monitoring.server.monitoring;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import de.scoopgmbh.copper.monitoring.core.data.MonitoringDataAdder;
import de.scoopgmbh.copper.monitoring.server.testfixture.LogbackFixture;
import de.scoopgmbh.copper.monitoring.server.testfixture.LogbackFixture.LogContentAssertion;
import de.scoopgmbh.copper.monitoring.server.testfixture.LogbackFixture.MessageAndLogLevel;
import de.scoopgmbh.copper.monitoring.server.testfixture.MonitoringFixture;


public class MonitoringDataCollectorTest {
	@Test
	public void test_too_many_data_adds_schould_be_ignored_and_not_block_the_submitting_thread(){
		MonitoringDataAccessQueue monitoringQueue = new MonitoringDataAccessQueue(10,null,Mockito.mock(MonitoringDataAdder.class));
		final MonitoringDataCollector monitoringDataCollector = new MonitoringDataCollector(monitoringQueue);
		
		new LogbackFixture().assertLogContent(new LogContentAssertion() {
			
			@Override
			public void executeLogCreatingAction() {
				for (int i=0;i<1000;i++){
					monitoringDataCollector.submitAdapterWfLaunch("", new Object());
				}
			}

			@Override
			public void assertLogContent(List<MessageAndLogLevel> logContent) {
				assertTrue(logContent.size()>0);
				assertTrue(logContent.get(0).message.contains(MonitoringDataAccessQueue.IGNORE_WARN_TEXT));
			}
		});

		
		new MonitoringFixture().waitUntilMonitoringDataProcessed(monitoringQueue);
		assertTrue(monitoringQueue.ignored.get()>0);
	}
}
