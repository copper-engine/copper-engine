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
package org.copperengine.core.persistent;

import java.util.HashMap;
import java.util.Map;

/**
 * A builder for {@link DefaultPersistenceContextFactoryConfiguration}.
 * @author Roland Scheel
 *
 */
public class DefaultPersistenceContextFactoryConfigurationBuilder {
	
	Map<Class<?>, DefaultEntityPersisterFactory<?,?>> persisterFactories 
	    = new HashMap<Class<?>, DefaultEntityPersisterFactory<?,?>>();
	
	public <E, F extends DefaultEntityPersisterFactory<E, ? extends DefaultEntityPersister<E>>> DefaultPersistenceContextFactoryConfigurationBuilder addPersisterFactory(
			F persisterFactory) {
		DefaultEntityPersisterFactory<?,?> oldValue = persisterFactories.put(persisterFactory.getEntityClass(), persisterFactory);
		if (oldValue != null && oldValue != persisterFactory) {
			throw new RuntimeException("Only one persister factory per entity class is allowed");
		}
		return this;
	}
	
	public DefaultPersistenceContextFactoryConfiguration compile() {
		return new DefaultPersistenceContextFactoryConfiguration(persisterFactories);
	}
	

}
