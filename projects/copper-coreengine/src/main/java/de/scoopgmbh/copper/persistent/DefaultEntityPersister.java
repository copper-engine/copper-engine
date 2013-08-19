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

/**
 * Default implementation of the {@link EntityPersister} interface. This implementation plugs in DefaultPersistenceWorkers and is convenient to implement. On limitations see {@link DefaultWorkflowPersistencePlugin}.
 * @author Roland Scheel
 *
 * @param <E> The entity class this persister handles
 */
public class DefaultEntityPersister<E> implements EntityPersister<E> {

	final PersistentWorkflow<?> workflow;
	final DefaultPersistenceWorker<E, ? extends EntityPersister<E>> selectionWorker;
    final DefaultPersistenceWorker<E, ? extends EntityPersister<E>> insertionWorker;
    final DefaultPersistenceWorker<E, ? extends EntityPersister<E>> updateWorker;
    final DefaultPersistenceWorker<E, ? extends EntityPersister<E>> deletionWorker;

	public DefaultEntityPersister(PersistentWorkflow<?> workflow, DefaultPersistenceWorker<E, ? extends EntityPersister<E>> selectionWorker
            , DefaultPersistenceWorker<E, ? extends EntityPersister<E>> insertionWorker
            , DefaultPersistenceWorker<E, ? extends EntityPersister<E>> updateWorker
            , DefaultPersistenceWorker<E, ? extends EntityPersister<E>> deletionWorker) 
	{
		this.workflow = workflow;
		this.selectionWorker = selectionWorker;
		this.insertionWorker = insertionWorker;
		this.updateWorker = updateWorker;
		this.deletionWorker = deletionWorker;
	}

	public DefaultEntityPersister(PersistentWorkflow<?> workflow, DefaultPersisterSimpleCRUDSharedRessources<E, ? extends DefaultEntityPersister<E>> sharedRessources) 
	{
		this.workflow = workflow;
		this.selectionWorker = sharedRessources.selectWorker;
		this.insertionWorker = sharedRessources.insertWorker;
		this.updateWorker = sharedRessources.updateWorker;
		this.deletionWorker = sharedRessources.deleteWorker;
	}

	public static class EntityAndCallback<E extends PersistentEntity> {

		@SuppressWarnings("rawtypes")
		static final EntityPersister.PostSelectedCallback NULLCALLBACK = new EntityPersister.PostSelectedCallback() {
			@Override
			public void entitySelected(Object e) {
			}
			@Override
			public void entityNotFound(Object e) {
			}
		};

		@SuppressWarnings("unchecked")
		public EntityAndCallback(E entity, @SuppressWarnings("rawtypes") EntityPersister.PostSelectedCallback callback) {
			this.entity = entity;
			this.callback = callback == null?(EntityPersister.PostSelectedCallback<E>)NULLCALLBACK:callback;
		}
		public final E entity;
		public final EntityPersister.PostSelectedCallback<E> callback;
	}
	
	@Override
	public void select(E e,
			EntityPersister.PostSelectedCallback<E> callback) {
		if (selectionWorker == null)
			throw new UnsupportedOperationException();
		selectionWorker.addSelect(workflow, e, callback);
	}

	@Override
	public void insert(E e) {
		if (insertionWorker == null)
			throw new UnsupportedOperationException();
		insertionWorker.addDml(workflow, e);
	}

	@Override
	public void update(E e) {
		if (updateWorker == null)
			throw new UnsupportedOperationException();
		updateWorker.addDml(workflow, e);
	}

	@Override
	public void delete(E e) {
		if (deletionWorker == null)
			throw new UnsupportedOperationException();
		deletionWorker.addDml(workflow, e);
	}

}
