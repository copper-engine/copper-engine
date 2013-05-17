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

import java.util.Collection;

/**
 * Interface for the {@link DefaultWorkflowPersistencePlugin}. This is a convenience implementation capable of handling primitive structural dependencies. Factories implementing this interface can be used in {@link DefaultPersistenceContextFactoryConfiguration}. 
 * @author Roland Scheel
 *
 * @param <E> The entity class this persister factory handles.
 * @param <P> The persister class this persister factory handles.
 */
public interface DefaultEntityPersisterFactory<E, P extends DefaultEntityPersister<E>> {
	
	Class<E> getEntityClass();
	
	/**
	 * Creates a persister. The workers are guaranteed to origin from a preceeding call to {@link #createSelectionWorker()}, {@link #createInsertionWorker()}, {@link #createUpdateWorker()} and {@link #createDeletionWorker()}.  
	 * @param workflow The workflow that this persister is handed out to in one of the {@link PersistentWorkflow#doOnDelete(PersistenceContext)}, {@link PersistentWorkflow#doOnLoad(PersistenceContext)} and {@link PersistentWorkflow#doOnSave(PersistenceContext)} methods.
	 * @param selectionWorker
	 * @param insertionWorker
	 * @param updateWorker
	 * @param deletionWorker
	 * @return
	 */
	P createPersister(PersistentWorkflow<?> workflow, DefaultPersistenceWorker<E, P> selectionWorker, DefaultPersistenceWorker<E, P> insertionWorker,
		DefaultPersistenceWorker<E, P> updateWorker,
		DefaultPersistenceWorker<E, P> deletionWorker
	);
	
	DefaultPersistenceWorker<E, P> createSelectionWorker();
	DefaultPersistenceWorker<E, P> createInsertionWorker();
	DefaultPersistenceWorker<E, P> createUpdateWorker();
	DefaultPersistenceWorker<E, P> createDeletionWorker();
	
	/**
	 * @return the entities that this factorie's assigned entity is depending on. It is guaranteed that the entities this factorier's assigned entity is depending on are inserted first and deleted afterwards.
	 */
	Collection<Class<?>> getEntityClassesDependingOn();

}
