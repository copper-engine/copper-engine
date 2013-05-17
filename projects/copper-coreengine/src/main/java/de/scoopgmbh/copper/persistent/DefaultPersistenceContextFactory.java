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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Default implementation of the {@link PersistenceContextFactory} interface. 
 * @author Roland Scheel
 *
 */
public final class DefaultPersistenceContextFactory implements PersistenceContextFactory<PersistenceContext> {
	
	DefaultPersistenceContextFactoryConfiguration configuration;
	Connection connection;
	
	/**
	 * Constructs a DefaultPersistenceContextFactory.
	 * @param configuration The persister factory configuration
	 * @param con The connection that this persistence context factory will flush to. See {@link #flush()}.
	 */
	DefaultPersistenceContextFactory(DefaultPersistenceContextFactoryConfiguration configuration,Connection con) {
		this.configuration = configuration;
		this.connection = con;
	}
	
	@SuppressWarnings("rawtypes")
	HashMap<Class<?>, DefaultPersistenceWorker[]> workers = new HashMap<Class<?>, DefaultPersistenceWorker[]>();  
	@SuppressWarnings("rawtypes")
	TreeMap<Double, DefaultPersistenceWorker> orderedWorkers = new TreeMap<Double, DefaultPersistenceWorker>();  
	
	@Override
	public PersistenceContext createPersistenceContextForLoading(
			final PersistentWorkflow<?> workflow) {
		return createPersistenceContext(workflow);
	}

	@Override
	public PersistenceContext createPersistenceContextForSaving(
			PersistentWorkflow<?> workflow) {
		return createPersistenceContext(workflow);
	}

	@Override
	public PersistenceContext createPersistenceContextForDeletion(
			PersistentWorkflow<?> workflow) {
		return createPersistenceContext(workflow);
	}

	private PersistenceContext createPersistenceContext(
			final PersistentWorkflow<?> workflow) {
		return new PersistenceContext() {
			Map<Class<?>, DefaultEntityPersister<?>> map = new HashMap<Class<?>, DefaultEntityPersister<?>>();

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public <T> EntityPersister<T> getPersister(Class<T> entityClass) {
				DefaultEntityPersister persister = (DefaultEntityPersister) map.get(entityClass);
				if (persister != null)
					return persister;
				
				DefaultEntityPersisterFactory<?, ?> persisterFactory = configuration.getPersisterFactory(entityClass);
				if (persisterFactory == null)
					throw new RuntimeException("No persister configured for class '"+entityClass.getCanonicalName()+"'");
				
				DefaultPersistenceWorker[] workerArray = workers.get(entityClass);
				if (workerArray == null) {
					workerArray = new DefaultPersistenceWorker[] {
								persisterFactory.createSelectionWorker()
								,persisterFactory.createInsertionWorker()
								,persisterFactory.createUpdateWorker()
								,persisterFactory.createDeletionWorker()};
					workers.put(entityClass, workerArray);
					int precedence = configuration.getDependencyOrderedPersisterFactories().indexOf(persisterFactory)+2;
					//selection, insertion and updates operate on master entities before child entities
					//deletions have the reverse order
					orderedWorkers.put(1d-1d/precedence, workerArray[0]);
					orderedWorkers.put(2d-1d/precedence, workerArray[1]);
					orderedWorkers.put(3d-1d/precedence, workerArray[2]);
					orderedWorkers.put(3d+1d/precedence, workerArray[3]);
				}
				persister = persisterFactory.createPersister(workflow, workerArray[0], workerArray[1], workerArray[2], workerArray[3]);
				map.put(entityClass, persister);
				return persister;
			}

		};
	}
	
	@Override
	public void flush() throws SQLException {
		for (@SuppressWarnings("rawtypes") DefaultPersistenceWorker worker : orderedWorkers.values()) {
			worker.flush(connection);
		}
	}

}
