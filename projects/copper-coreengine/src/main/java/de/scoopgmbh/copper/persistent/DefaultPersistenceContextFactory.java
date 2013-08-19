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
import java.util.List;
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
	public DefaultPersistenceContextFactory(DefaultPersistenceContextFactoryConfiguration configuration,Connection con) {
		this.configuration = configuration;
		this.connection = con;
	}
	
	HashMap<DefaultEntityPersisterFactory<?,?>, DefaultPersisterSharedRessources<?,?>> persisterSharedRessources = new HashMap<DefaultEntityPersisterFactory<?,?>, DefaultPersisterSharedRessources<?,?>>();  
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
			final PersistentWorkflow<?> wf) {
		return new PersistenceContext() {
			
			final PersistentWorkflow<?> workflow = wf;

			@SuppressWarnings({ "unchecked" })
			@Override
			public <T> EntityPersister<T> getPersister(Class<? extends T> entityClass) {
				
				DefaultEntityPersisterFactory<?, DefaultEntityPersister<T>> persisterFactory = (DefaultEntityPersisterFactory<?, DefaultEntityPersister<T>>) configuration.getPersisterFactory(entityClass);
				if (persisterFactory == null)
					throw new RuntimeException("No persister configured for class '"+entityClass.getCanonicalName()+"'");
				
				return (EntityPersister<T>)createPersister(persisterFactory);
			}

			@SuppressWarnings({ "unchecked" })
			@Override
			public <T> T getMapper(Class<T> mapperInterface) {
				DefaultEntityPersisterFactory<?, DefaultEntityPersister<?>> persisterFactory = (DefaultEntityPersisterFactory<?, DefaultEntityPersister<?>>) configuration.getMapper(mapperInterface);
				if (persisterFactory == null)
					throw new RuntimeException("No mapper configured for interface '"+mapperInterface.getCanonicalName()+"'");
				
				return (T)createPersister(persisterFactory);
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			private EntityPersister<?> createPersister(
					DefaultEntityPersisterFactory<?, ?> persisterFactory) {
				DefaultPersisterSharedRessources sharedRessources = persisterSharedRessources.get(persisterFactory);
				if (sharedRessources == null) {
					sharedRessources = persisterFactory.createSharedRessources();
					persisterSharedRessources.put(persisterFactory, sharedRessources);
					List<DefaultEntityPersisterFactory<?, ?>> orderedPersisterFactory = configuration.getDependencyOrderedPersisterFactories();
					int numberOfPersisterFactories = orderedPersisterFactory.size();
					double precedence = orderedPersisterFactory.indexOf(persisterFactory);
					//selection, insertion and updates operate on master entities before child entities
					//deletions have the reverse order
					int i = 3;
					//compiler thinks, getWorkers() delivers Iterable<Object>, no idea.
					for (Object obj : sharedRessources.getWorkers()) {
						DefaultPersistenceWorker<?,?> worker = (DefaultPersistenceWorker<?,?>)obj;
						double position = precedence+1d/i++;
						switch (worker.getOperationType()) {
						case INSERT: break;
						case UPDATE: position += 1*numberOfPersisterFactories; break;
						case SELECT: position += 2*numberOfPersisterFactories; break;
						case DELETE: position = 4*numberOfPersisterFactories-position; break;
						default: throw new RuntimeException("Unsupported operationType: "+worker.getOperationType());
						}
						orderedWorkers.put(position,  worker);
					}
				}
				EntityPersister<?> persister = persisterFactory.createPersister(workflow, sharedRessources);
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
