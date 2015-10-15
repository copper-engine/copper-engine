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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface for plugging in additional persistence feature into {@link OracleDialect} and {@link AbstractSqlDialect}.
 * 
 * @author Roland Scheel
 */
public interface WorkflowPersistencePlugin {

    /**
     * Implementations have to call {@link PersistentWorkflow#onLoad(PersistenceContext)} for every workflow passed.
     * 
     * @param con
     *            The database connection the load operation is bound to.
     * @param workflows
     *            The workflows that are being loaded
     * @throws SQLException
     *             Implementations may pass SQL Exception to be handled by the database dialect
     */
    void onWorkflowsLoaded(Connection con, Iterable<? extends PersistentWorkflow<?>> workflows) throws SQLException;

    /**
     * Implementations have to call {@link PersistentWorkflow#onLoad(PersistenceContext)} for every workflow passed.
     * 
     * @param con
     *            The database connection the load operation is bound to.
     * @param workflows
     *            The workflows that are being loaded
     * @throws SQLException
     *             Implementations may pass SQL Exception to be handled by the database dialect
     */
    void onWorkflowsSaved(Connection con, Iterable<? extends PersistentWorkflow<?>> workflows) throws SQLException;

    /**
     * Implementations have to call {@link PersistentWorkflow#onDelete(PersistenceContext)} for every workflow passed.
     * 
     * @param con
     *            The database connection the load operation is bound to.
     * @throws SQLException
     *             Implementations may pass SQL Exception to be handled by the database dialect
     */
    void onWorkflowsDeleted(Connection con, Iterable<? extends PersistentWorkflow<?>> workflows) throws SQLException;

    final WorkflowPersistencePlugin NULL_PLUGIN = new WorkflowPersistencePlugin() {
        @Override
        public void onWorkflowsSaved(Connection con,
                Iterable<? extends PersistentWorkflow<?>> workflows) throws SQLException {
            for (PersistentWorkflow<?> wf : workflows) {
                wf.onSave(PersistenceContext.NULL_CONTEXT);
            }
        }

        @Override
        public void onWorkflowsLoaded(Connection con,
                Iterable<? extends PersistentWorkflow<?>> workflows) throws SQLException {
            for (PersistentWorkflow<?> wf : workflows) {
                wf.onLoad(PersistenceContext.NULL_CONTEXT);
            }
        }

        @Override
        public void onWorkflowsDeleted(Connection con,
                Iterable<? extends PersistentWorkflow<?>> workflows) throws SQLException {
            for (PersistentWorkflow<?> wf : workflows) {
                wf.onDelete(PersistenceContext.NULL_CONTEXT);
            }
        }
    };

}
