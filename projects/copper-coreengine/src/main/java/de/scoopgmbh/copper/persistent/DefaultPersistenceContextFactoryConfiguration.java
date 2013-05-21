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
package de.scoopgmbh.copper.persistent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration used in {@link DefaultPersistenceContextFactory} and {@link DefaultWorkflowPersistencePlugin}. Users should not create this class directly but use {@link DefaultPersistenceContextFactoryConfigurationBuilder}.
 * @author Roland Scheel
 *
 */
public class DefaultPersistenceContextFactoryConfiguration {

	public DefaultPersistenceContextFactoryConfiguration(Map<Class<?>, DefaultEntityPersisterFactory<?,?>> persisterFactories) {
		this.persisterFactories =  
			    new HashMap<Class<?>, DefaultEntityPersisterFactory<?,?>>(persisterFactories);
		orderedFactories  = new ArrayList<DefaultEntityPersisterFactory<?,?>>(persisterFactories.values());
		Collections.sort(orderedFactories, new Comparator<DefaultEntityPersisterFactory<?,?>>() {

			@Override
			public int compare(DefaultEntityPersisterFactory<?, ?> o1,
					DefaultEntityPersisterFactory<?, ?> o2) {
				return levels(o1.getEntityClass(),o1)-levels(o2.getEntityClass(),o2);
			}

			private int levels(Class<?> clazz, DefaultEntityPersisterFactory<?, ?> factory) {
				if (factory.getEntityClassesDependingOn().contains(clazz))
					throw new RuntimeException("Cycle in dependency graph for "+clazz.getCanonicalName()+". Last step: "+factory.getEntityClass().getCanonicalName()+".");
				int level = 0;
				for (Class<?> e : factory.getEntityClassesDependingOn()) {
					DefaultEntityPersisterFactory<?, ?> dependingOnFactory = DefaultPersistenceContextFactoryConfiguration.this.persisterFactories.get(e);
					if (dependingOnFactory == null)
						continue;
					level = Math.max(level, levels(clazz,dependingOnFactory)+1);
				}
				return level;
			}});
	}

	final ArrayList<DefaultEntityPersisterFactory<?,?>> orderedFactories;
	final Map<Class<?>, DefaultEntityPersisterFactory<?,?>> persisterFactories;

	public DefaultEntityPersisterFactory<?,?> getPersisterFactory(Class<?> entityClass) {
		return persisterFactories.get(entityClass);
	}

	public <T> DefaultEntityPersisterFactory<?,?> getMapper(Class<T> mapperClass) {
		for (DefaultEntityPersisterFactory<?,?> factory : orderedFactories) {
			if (mapperClass.isAssignableFrom(factory.getPersisterClass()))
				return factory;
		}
		return null;
	}

	public List<DefaultEntityPersisterFactory<?,?>> getDependencyOrderedPersisterFactories() {
		return orderedFactories;
	}
		
}