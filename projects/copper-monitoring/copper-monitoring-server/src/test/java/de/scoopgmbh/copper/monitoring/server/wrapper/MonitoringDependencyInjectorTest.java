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
package de.scoopgmbh.copper.monitoring.server.wrapper;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.copperengine.core.util.PojoDependencyInjector;
import org.junit.Test;
import org.mockito.Mockito;

import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataCollector;


public class MonitoringDependencyInjectorTest {

	@Test
	public void test_no_interface(){
		final PojoDependencyInjector pojoDependencyInjector = new PojoDependencyInjector();
		final Object bean = new Object();
		pojoDependencyInjector.register("test", bean);
		final MonitoringDataCollector mock = Mockito.mock(MonitoringDataCollector.class);
		MonitoringDependencyInjector monitoringDependencyInjector = new MonitoringDependencyInjector(pojoDependencyInjector, mock);
		monitoringDependencyInjector.getBean("test").toString();
	}	
	
	private static interface Test42{
		void abc();
	}
	
	@Test
	public void test_interface(){
		final PojoDependencyInjector pojoDependencyInjector = new PojoDependencyInjector();
		final Test42 bean = new Test42(){
			@Override
			public void abc() {
				//empty
			}
		};
		pojoDependencyInjector.register("test", bean);
		final MonitoringDataCollector mock = Mockito.mock(MonitoringDataCollector.class);
		MonitoringDependencyInjector monitoringDependencyInjector = new MonitoringDependencyInjector(pojoDependencyInjector, mock);
		((Test42)monitoringDependencyInjector.getBean("test")).abc();
		
		Mockito.verify(mock).submitAdapterCalls(Mockito.any(Method.class), Mockito.any(Object[].class), Mockito.any(),Mockito.any(WorkflowInstanceInfo.class));

	}
	
	@Test
	public void test_forward(){
		final PojoDependencyInjector pojoDependencyInjector = new PojoDependencyInjector();
		final Object bean = new Object();
		pojoDependencyInjector.register("test", bean);
		final MonitoringDataCollector mock = Mockito.mock(MonitoringDataCollector.class);
		MonitoringDependencyInjector monitoringDependencyInjector = new MonitoringDependencyInjector(pojoDependencyInjector, mock);
		assertEquals(bean, monitoringDependencyInjector.getBean("test"));
	}
	
}
