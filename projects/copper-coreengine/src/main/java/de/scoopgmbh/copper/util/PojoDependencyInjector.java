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
package de.scoopgmbh.copper.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.scoopgmbh.copper.AbstractDependencyInjector;

public class PojoDependencyInjector extends AbstractDependencyInjector {

	private Map<String, Object> map = new ConcurrentHashMap<String, Object>();

	@Override
	protected Object getBean(String beanId) {
		return map.get(beanId);
	}
	
	public void register(String beanId, Object bean) {
		map.put(beanId, bean);
	}

}
