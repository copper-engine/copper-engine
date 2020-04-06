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
 * Default implementation for the {@link WorkflowPersistencePlugin}. This implementation handles primitive persistence
 * for {@link PersistentWorkflow} members.
 * It is not capable of handling data-based dependencies between entities concerning insertion and deletion order.
 *
 * @author Roland Scheel
 */
public class DefaultWorkflowPersistencePlugin implements WorkflowPersistencePlugin {

    DefaultPersistenceContextFactoryConfiguration configuration;

    public DefaultWorkflowPersistencePlugin(DefaultPersistenceContextFactoryConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onWorkflowsLoaded(Connection con,
            Iterable<? extends PersistentWorkflow<?>> workflows) throws SQLException {
        PersistenceContextFactory<?> ctxFactory = createPersistenceContextFactory(con);
        for (PersistentWorkflow<?> wf : workflows) {
            PersistenceContext pctx = ctxFactory.createPersistenceContextForLoading(wf);
            wf.onLoad(pctx);
        }
        ctxFactory.flush();
    }

    @Override
    public void onWorkflowsSaved(Connection con,
            Iterable<? extends PersistentWorkflow<?>> workflows) throws SQLException {
        PersistenceContextFactory<?> ctxFactory = createPersistenceContextFactory(con);
        for (PersistentWorkflow<?> wf : workflows) {
            PersistenceContext pctx = ctxFactory.createPersistenceContextForSaving(wf);
            wf.onSave(pctx);
        }
        ctxFactory.flush();
    }

    @Override
    public void onWorkflowsDeleted(Connection con,
            Iterable<? extends PersistentWorkflow<?>> workflows) throws SQLException {
        PersistenceContextFactory<?> ctxFactory = createPersistenceContextFactory(con);
        for (PersistentWorkflow<?> wf : workflows) {
            PersistenceContext pctx = ctxFactory.createPersistenceContextForDeletion(wf);
            wf.onDelete(pctx);
        }
        ctxFactory.flush();
    }

    protected DefaultPersistenceContextFactoryConfiguration configuration() {
        return configuration;
    }

    protected PersistenceContextFactory<?> createPersistenceContextFactory(Connection con) {
        return new DefaultPersistenceContextFactory(configuration, con);
    }

}
