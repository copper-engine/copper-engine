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

import java.sql.SQLException;

/**
 * Interface for custom persistence of {@link PersistentWorkflow} members. For every worklow, during saving and loading
 * from and to storage a {@link PersistenceContext} is being created. This context is passed to
 * {@link PersistentWorkflow#onLoad(PersistenceContext)}, {@link PersistentWorkflow#onSave(PersistenceContext)} and
 * {@link PersistentWorkflow#onDelete(PersistenceContext)}.
 * 
 * @author Roland Scheel
 * @param <T>
 *         subtype of PersistenceContext
 */
public interface PersistenceContextFactory<T extends PersistenceContext> {

    /**
     * Creates a persistence context for use during loading of workflows.
     * 
     * @param workflow
     *        the workflow for which the context is created
     * @return the created context
     */
    T createPersistenceContextForLoading(PersistentWorkflow<?> workflow);

    /**
     * Creates a persistence context for use during saving of workflows.
     * 
     * @param workflow
     *        the workflow for which the context is created
     * @return the created context
     */
    T createPersistenceContextForSaving(PersistentWorkflow<?> workflow);

    /**
     * Creates a persistence context for use during deletion of workflows.
     * 
     * @param workflow
     *        the workflow for which the context is created
     * @return the created context
     */
    T createPersistenceContextForDeletion(PersistentWorkflow<?> workflow);

    /**
     * Flushes all operations that were passed to the created {@link PersistenceContext}s.
     * 
     * @throws SQLException
     *         for any SQL exception happening during this operation
     */
    void flush() throws SQLException;

}
