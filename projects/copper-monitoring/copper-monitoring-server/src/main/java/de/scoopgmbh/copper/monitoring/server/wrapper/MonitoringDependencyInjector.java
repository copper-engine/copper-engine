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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.copperengine.core.AbstractDependencyInjector;
import org.copperengine.core.Workflow;

import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceInfo;
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
	public String getType() {
		return (abstractDependencyInjector != null) ? abstractDependencyInjector.getType() : "MONITORING";
	}
	
	WorkflowInstanceInfo lastWorkflow;
	@Override
	public void inject(Workflow<?> workflow) {
		lastWorkflow = new WorkflowInstanceInfo();
		lastWorkflow.setId(workflow.getId());
		lastWorkflow.setClassname(workflow.getClass().getName());
		super.inject(workflow);
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
	    	return java.lang.reflect.Proxy.newProxyInstance(adapter.getClass().getClassLoader(), adapter.getClass().getInterfaces(), new DependencyHandler(adapter,monitoringDataCollector,lastWorkflow));
	    } else {
	    	return adapter;
	    }
	}	

	private static class DependencyHandler implements InvocationHandler {
		private final Object adapter;
		private final MonitoringDataCollector monitoringDataCollector;
		private final WorkflowInstanceInfo workflow;
		
		public DependencyHandler(Object adapter, MonitoringDataCollector monitoringDataCollector, WorkflowInstanceInfo workflow) {
			this.adapter = adapter;
			this.monitoringDataCollector = monitoringDataCollector;
			this.workflow = workflow;
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			monitoringDataCollector.submitAdapterCalls(method, args, adapter,workflow);
			return monitoringDataCollector.<Object>measureTimePeriod(adapter.getClass()+"#"+method.getName(), new Callable<Object>() {
				@Override
				public Object call() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
					return method.invoke(adapter, args);
				}
			});
		}
	}
}
