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
package org.copperengine.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.copperengine.core.AbstractDependencyInjector;


public class PojoDependencyInjector extends AbstractDependencyInjector {

	private Map<String, Provider<?>> map = new ConcurrentHashMap<String, Provider<?>>();

	@Override
	public String getType() {
		return "POJO";
	}
	
	@Override
	protected Object getBean(String beanId) {
		Provider<?> p = map.get(beanId);
		return p.get();
	}
	
	public <T> void register(String beanId, T bean) {
		register(beanId, new BeanProvider<T>(bean));
	}
	
	public <T> void register(String beanId, Provider<T> provider) {
		map.put(beanId, provider);
	}
	
	public static interface Provider<T> {

		public T get();
		
	}

	public static class BeanProvider<T> implements Provider<T> {
		
		T bean;
		
		public BeanProvider(T bean) {
			this.bean = bean;
		}

		public T get() {
			return bean;
		}
		
	}
	

	public static abstract class Factory<T> implements Provider<T> {

		@Override
		public T get() {
			return create();
		}

		public abstract T create();
		
	}
	
	public static class SimpleFactory<T> extends Factory<T> {
		
		Class<? extends T> clazz;
		
		public SimpleFactory(Class<? extends T> clazz) {
			this.clazz = clazz;
		}

		@Override
		public T create() {
			try {
				return clazz.newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
	}

}
