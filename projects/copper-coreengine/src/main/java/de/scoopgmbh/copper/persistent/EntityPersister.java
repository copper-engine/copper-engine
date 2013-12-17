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
 * Base interface for persistent entities as members of {@link PersistentWorkflow}..
 * @author Roland Scheel
 *
 * @param <E>
 */
public interface EntityPersister<E> {

	interface PostSelectedCallback<E> {
		void entitySelected(E e);
		void entityNotFound(E e);
	}

	/**
	 * Issues a select for the entity that will be executed later. The entity has to contain the identifying data used to select it. Later on, the callback is called either via {@link PostSelectedCallback#entitySelected(E) entitySelected} or {@link PostSelectedCallback#entityNotFound(E) entityNotFound}
	 * @param e the entity to select. The identifier has to be set.
	 * @param callback the callback to call with the selection results
	 */
	void select(E e, PostSelectedCallback<E> callback);
	
	/**
	 * Issues an insert for the entity. The insertion will be effectively committed when the workflow instance is committed too. 
	 * @param e the entity to insert.
	 */
	void insert(E e);

	/**
	 * Issues an update for the entity. The update will be effectively committed when the workflow instance is committed too. 
	 * @param e the entity to insert.
	 */
	void update(E e);

	/**
	 * Issues a delete for the entity. The deletion will be effectively committed when the workflow instance is committed too. 
	 * @param e the entity to insert.
	 */
	void delete(E e);

}
