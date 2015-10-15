/*
 * Copyright 2002-2015 SCOOP Software GmbH
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

/**
 * Interface for use in {@link PersistentWorkflow#onLoad(PersistenceContext)},
 * {@link PersistentWorkflow#onSave(PersistenceContext)} and {@link PersistentWorkflow#onDelete(PersistenceContext)}
 * 
 * @author Roland Scheel
 */
public interface PersistenceContext {

    /**
     * retrieves the persister assigned to this entity class. Throws a runtime exception when no persister can be
     * returned
     * 
     * @param entityClass
     *            The entity class to look up the persister for
     * @return the persister
     */
    <T> EntityPersister<T> getPersister(Class<? extends T> entityClass);

    <T> T getMapper(Class<T> mapperInterface);

    PersistentWorkflow<?> getWorkflow();

    PersistenceContext NULL_CONTEXT = new PersistenceContext() {

        @Override
        public <T> EntityPersister<T> getPersister(Class<? extends T> entityClass) {
            throw new RuntimeException("You did not configure a WorkflowPersistencePlugin.");
        }

        @Override
        public <T> T getMapper(Class<T> mapperInterface) {
            throw new RuntimeException("You did not configure a WorkflowPersistencePlugin.");
        }

        @Override
        public PersistentWorkflow<?> getWorkflow() {
            throw new RuntimeException("You did not configure a WorkflowPersistencePlugin.");
        }

    };

}
