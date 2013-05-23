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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import de.scoopgmbh.copper.AbstractDependencyInjector;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataCollector;

/**
 * Add Monitoring for DependencyInjector
 * @author hbrackmann
 *
 */
public class MonitoringDependencyInjector extends AbstractDependencyInjector{
	
	private final AbstractDependencyInjector abstractDependencyInjector;
	private final MonitoringDataCollector monitoringDataCollector;
	
	public MonitoringDependencyInjector(AbstractDependencyInjector abstractDependencyInjector, MonitoringDataCollector monitoringDataCollector) {
		super();
		this.abstractDependencyInjector = abstractDependencyInjector;
		this.monitoringDataCollector = monitoringDataCollector;
	}

	@Override
	protected Object getBean(String beanId) {
		//protected Workaround
		Object adapter;
		try {
			final Method method = abstractDependencyInjector.getClass().getDeclaredMethod("getBean", String.class);
			method.setAccessible(true);
			adapter = method.invoke(abstractDependencyInjector,beanId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	    if (adapter!=null && adapter.getClass().getInterfaces().length>0){
	    	return java.lang.reflect.Proxy.newProxyInstance(adapter.getClass().getClassLoader(), adapter.getClass().getInterfaces(), new DependencyHandler(adapter,monitoringDataCollector));
	    } else {
	    	return adapter;
	    }
	}	

	private static class DependencyHandler implements InvocationHandler {
		private final Object adapter;
		private final MonitoringDataCollector monitoringDataCollector;
		
		public DependencyHandler(Object adapter, MonitoringDataCollector monitoringDataCollector) {
			this.adapter = adapter;
			this.monitoringDataCollector = monitoringDataCollector;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			monitoringDataCollector.submitAdapterCalls(method, args, adapter);
			Object result = method.invoke(this.adapter, args);
			return result;
		}
	}

  

}
