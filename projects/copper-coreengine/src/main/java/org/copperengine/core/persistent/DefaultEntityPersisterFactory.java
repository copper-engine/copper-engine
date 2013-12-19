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

import java.util.Collection;

/**
 * Interface for the {@link DefaultWorkflowPersistencePlugin}. This is a convenience implementation capable of handling
 * primitive structural dependencies. Factories implementing this interface can be used in
 * {@link DefaultPersistenceContextFactoryConfiguration}.
 * 
 * @param <E>
 *            The entity class this persister factory handles.
 * @param <P>
 *            The persister class this persister factory handles.
 * @author Roland Scheel
 */
public interface DefaultEntityPersisterFactory<E, P extends DefaultEntityPersister<E>> {

    Class<E> getEntityClass();

    /**
     * Needed to be able to return the created persister as a mapper (see {@link PersistenceContext#getMapper(Class)}).
     */
    Class<?> getPersisterClass();

    /**
     * Creates a persister.
     * 
     * @param workflow
     *            The workflow that this persister is handed out to in one of the
     *            {@link PersistentWorkflow#onDelete(PersistenceContext)},
     *            {@link PersistentWorkflow#onLoad(PersistenceContext)} and
     *            {@link PersistentWorkflow#onSave(PersistenceContext)} methods.
     * @param sharedRessources
     *            shared ressources; should come from a preceeding call to {@link #createSharedRessources()}
     */
    P createPersister(PersistentWorkflow<?> workflow, DefaultPersisterSharedRessources<E, P> sharedRessources);

    DefaultPersisterSharedRessources<E, P> createSharedRessources();

    /**
     * @return the entities that this factorie's assigned entity is depending on. It is guaranteed that the entities
     *         this factorier's assigned entity is depending on are inserted first and deleted afterwards.
     */
    Collection<Class<?>> getEntityClassesDependingOn();

}
