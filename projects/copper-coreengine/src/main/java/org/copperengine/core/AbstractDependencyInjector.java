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
package org.copperengine.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.copperengine.core.persistent.SavepointAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractDependencyInjector implements DependencyInjector {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractDependencyInjector.class);

	private ProcessingEngine engine;
	
	public AbstractDependencyInjector() {
	}
	
	private static final class InjectionDescription {
		final Method method;
		final String beanId;
		public InjectionDescription(Method method, String beanId) {
			this.method = method;
			this.beanId = beanId;
		}
	}
	
	private final Map<Class<?>, List<InjectionDescription>> map = new ConcurrentHashMap<Class<?>, List<InjectionDescription>>();
	private static List<InjectionDescription> create(Class<?> c) {
		List<InjectionDescription> list = new ArrayList<InjectionDescription>();
		for (Method m : c.getMethods()) {
			AutoWire annotation = m.getAnnotation(AutoWire.class);
			if (annotation != null) {
				String id = "".equals(annotation.beanId()) ? m.getName().substring(3,4).toLowerCase() + m.getName().substring(4) : annotation.beanId();
				list.add(new InjectionDescription(m,id));
			}
		}
		return list;
	}
	
	@Override
	public void inject(Workflow<?> workflow) {
		workflow.setEngine(engine);
		final Class<?> c = workflow.getClass();
		List<InjectionDescription> list = null;
		list = map.get(c);
		// the following double checked locking shall prevent to create the InjectionDescription list more than once by two or more concurrent threads
		// Ignore FindBugs warning
		if (list == null) {
			synchronized (map) {
				list = map.get(c);
				if (list == null) {
					list = create(c);
					map.put(c, list);
				}
			}
		}
		for (InjectionDescription injectionDescription : list) {
			try {
				final Object bean = getBean(injectionDescription.beanId);
				logger.trace("injecting bean with beanId {} to method {}", injectionDescription.beanId, injectionDescription.method);
				injectionDescription.method.invoke(workflow, bean);
				if (bean instanceof SavepointAware) {
					workflow.registerSavepointAware((SavepointAware)bean);
				}
			}
			catch(RuntimeException e) {
				throw e;
			}
			catch(Exception e) {
				throw new CopperRuntimeException(e);
			}
		}
	}

	@Override
	public void setEngine(ProcessingEngine e) {
		engine = e;
	}

	protected abstract Object getBean(String beanId);
}

