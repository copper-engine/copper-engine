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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.copperengine.core.persistent.EntityPersister.PostSelectedCallback;

/**
 * An inmplementation for the {@link DefaultWorkflowPersistencePlugin}. The workers are assumed to be created via
 * {@link DefaultEntityPersisterFactory}.
 *
 * @param <E>
 * @param <P>
 * @author Roland Scheel
 */
public abstract class DefaultPersistenceWorker<E, P extends EntityPersister<E>> {

    public static enum OperationType {
        INSERT,
        UPDATE,
        SELECT,
        DELETE
    }

    final OperationType operationType;

    public DefaultPersistenceWorker(OperationType operationType) {
        if (operationType == null)
            throw new NullPointerException("operationType");
        this.operationType = operationType;
    }

    protected abstract void doExec(
            Connection connection, List<WorkflowAndEntity<E>> theWork) throws SQLException;

    public static class WorkflowAndEntity<E> {
        public WorkflowAndEntity(final PersistentWorkflow<?> workflow, final E entity, final PostSelectedCallback<E> callback) {
            this.workflow = workflow;
            this.entity = entity;
            this.callback = callback;
        }

        public final PersistentWorkflow<?> workflow;
        public final E entity;
        public final PostSelectedCallback<E> callback;
    }

    private ArrayList<WorkflowAndEntity<E>> theWork = new ArrayList<WorkflowAndEntity<E>>();

    public void addSelect(final PersistentWorkflow<?> workflow, final E entity, final PostSelectedCallback<E> callback) {
        theWork.add(new WorkflowAndEntity<E>(workflow, entity, callback));
    }

    public void addDml(final PersistentWorkflow<?> workflow, final E entity) {
        theWork.add(new WorkflowAndEntity<E>(workflow, entity, null));
    }

    public void flush(Connection connection) throws SQLException {
        if (!theWork.isEmpty())
            doExec(connection, Collections.unmodifiableList(theWork));
        theWork.clear();
    }

    public OperationType getOperationType() {
        return operationType;
    }
}
