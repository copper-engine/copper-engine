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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.copperengine.core.Interrupt;
import org.copperengine.core.instrument.Transformed;
import org.copperengine.core.persistent.DefaultPersistenceTest.WorkLogEntry.Type;
import org.copperengine.core.persistent.DefaultPersistenceWorker.OperationType;
import org.copperengine.core.persistent.DefaultPersistenceWorker.WorkflowAndEntity;
import org.junit.Assert;
import org.junit.Test;

public class DefaultPersistenceTest {

    public PersistentWorkflow<?> expectedWorkflow;

    static class MasterEntity {
    }

    static class ChildEntity {
    }

    abstract class PersisterFactoryBase<E, T extends DefaultEntityPersister<E>> implements DefaultEntityPersisterFactory<E, T> {

        final List<WorkLogEntry<?>> workLog;

        public PersisterFactoryBase(List<WorkLogEntry<?>> workLog) {
            this.workLog = workLog;
        }

        @Override
        public DefaultPersisterSimpleCRUDSharedRessources<E, T> createSharedRessources() {
            return new DefaultPersisterSimpleCRUDSharedRessources<E, T>(
                    createSelectionWorker(),
                    createInsertionWorker(),
                    createUpdateWorker(),
                    createDeletionWorker());
        }

        boolean selectionWorkerCreated = false;

        public DefaultPersistenceWorker<E, T> createSelectionWorker() {
            if (selectionWorkerCreated)
                Assert.fail("more than one selection worker created");
            selectionWorkerCreated = true;
            return new DefaultPersistenceWorker<E, T>(OperationType.SELECT) {
                @Override
                protected void doExec(
                        Connection connection,
                        List<WorkflowAndEntity<E>> theWork)
                        throws SQLException {
                    workLog.add(new WorkLogEntry<E>(WorkLogEntry.Type.SELECT, theWork));
                    for (WorkflowAndEntity<E> en : theWork) {
                        Assert.assertSame(expectedWorkflow, en.workflow);
                        en.callback.entitySelected(en.entity);
                    }
                }

            };
        }

        boolean insertionWorkerCreated = false;

        public DefaultPersistenceWorker<E, T> createInsertionWorker() {
            if (insertionWorkerCreated)
                Assert.fail("more than one insertion worker created");
            insertionWorkerCreated = true;
            return new DefaultPersistenceWorker<E, T>(OperationType.INSERT) {
                @Override
                protected void doExec(
                        Connection connection,
                        List<WorkflowAndEntity<E>> theWork)
                        throws SQLException {
                    workLog.add(new WorkLogEntry<E>(WorkLogEntry.Type.INSERT, theWork));
                    for (WorkflowAndEntity<E> en : theWork) {
                        Assert.assertSame(expectedWorkflow, en.workflow);
                    }
                }

            };
        }

        boolean updateWorkerCreated = false;

        public DefaultPersistenceWorker<E, T> createUpdateWorker() {
            if (updateWorkerCreated)
                Assert.fail("more than one update worker created");
            updateWorkerCreated = true;
            return new DefaultPersistenceWorker<E, T>(OperationType.UPDATE) {
                @Override
                protected void doExec(
                        Connection connection,
                        List<WorkflowAndEntity<E>> theWork)
                        throws SQLException {
                    workLog.add(new WorkLogEntry<E>(WorkLogEntry.Type.UPDATE, theWork));
                    for (WorkflowAndEntity<E> en : theWork) {
                        Assert.assertSame(expectedWorkflow, en.workflow);
                    }
                }

            };
        }

        boolean deletionWorkerCreated = false;

        public DefaultPersistenceWorker<E, T> createDeletionWorker() {
            if (deletionWorkerCreated)
                Assert.fail("more than one Deletion worker created");
            deletionWorkerCreated = true;
            return new DefaultPersistenceWorker<E, T>(OperationType.DELETE) {
                @Override
                protected void doExec(
                        Connection connection,
                        List<WorkflowAndEntity<E>> theWork)
                        throws SQLException {
                    workLog.add(new WorkLogEntry<E>(WorkLogEntry.Type.DELETE, theWork));
                    for (WorkflowAndEntity<E> en : theWork) {
                        Assert.assertSame(expectedWorkflow, en.workflow);
                    }
                }

            };
        }

    }

    static class WorkLogEntry<E> {
        enum Type {
            SELECT, INSERT, UPDATE, DELETE;
        }

        final Type type;
        final List<WorkflowAndEntity<E>> entities;

        public WorkLogEntry(Type type, List<WorkflowAndEntity<E>> entities) {
            this.type = type;
            this.entities = new ArrayList<WorkflowAndEntity<E>>(entities);
        }
    }

    class MasterPersisterFactory extends PersisterFactoryBase<MasterEntity, MasterPersisterFactory.Persister> {

        public MasterPersisterFactory(List<WorkLogEntry<?>> workLog) {
            super(workLog);
        }

        class Persister extends DefaultEntityPersister<MasterEntity> {
            public Persister(
                    PersistentWorkflow<?> workflow,
                    DefaultPersisterSimpleCRUDSharedRessources<MasterEntity, MasterPersisterFactory.Persister> sharedRessources) {
                super(workflow, sharedRessources);
            }
        }

        @Override
        public Class<MasterEntity> getEntityClass() {
            return MasterEntity.class;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<Class<?>> getEntityClassesDependingOn() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Class<Persister> getPersisterClass() {
            return Persister.class;
        }

        @Override
        public Persister createPersister(
                PersistentWorkflow<?> workflow,
                DefaultPersisterSharedRessources<MasterEntity, Persister> sharedRessources) {
            return new Persister(workflow, (DefaultPersisterSimpleCRUDSharedRessources<MasterEntity, Persister>) sharedRessources);
        }

    }

    class ChildPersisterFactory extends PersisterFactoryBase<ChildEntity, ChildPersisterFactory.Persister> {

        public ChildPersisterFactory(List<WorkLogEntry<?>> workLog) {
            super(workLog);
        }

        class Persister extends DefaultEntityPersister<ChildEntity> {
            public Persister(
                    PersistentWorkflow<?> workflow,
                    DefaultPersisterSimpleCRUDSharedRessources<ChildEntity, ChildPersisterFactory.Persister> sharedRessources) {
                super(workflow, sharedRessources);
            }
        }

        @Override
        public Class<ChildEntity> getEntityClass() {
            return ChildEntity.class;
        }

        @Override
        public Collection<Class<?>> getEntityClassesDependingOn() {
            return Arrays.<Class<?>>asList(MasterEntity.class);
        }

        @Override
        public Class<Persister> getPersisterClass() {
            return Persister.class;
        }

        @Override
        public Persister createPersister(
                PersistentWorkflow<?> workflow,
                DefaultPersisterSharedRessources<ChildEntity, Persister> sharedRessources) {
            return new Persister(workflow, (DefaultPersisterSimpleCRUDSharedRessources<ChildEntity, Persister>) sharedRessources);
        }
    }

    public class MasterCyclePersisterFactory extends MasterPersisterFactory {

        public MasterCyclePersisterFactory(List<WorkLogEntry<?>> workLog) {
            super(workLog);
        }

        @Override
        public Collection<Class<?>> getEntityClassesDependingOn() {
            return Arrays.<Class<?>>asList(ChildEntity.class);
        }

    }

    public class DefaultPersistenceTestData {
        public MasterEntity selectedMaster = new MasterEntity();
        public MasterEntity selectedMasterResult;
        public MasterEntity insertedMaster = new MasterEntity();
        public MasterEntity updatedMaster = new MasterEntity();
        public MasterEntity deletedMaster = new MasterEntity();
        public ChildEntity selectedChild = new ChildEntity();
        public ChildEntity selectedChildResult;
        public ChildEntity insertedChild = new ChildEntity();
        public ChildEntity updatedChild = new ChildEntity();
        public ChildEntity deletedChild = new ChildEntity();
    }

    DefaultPersistenceTestData onLoad = new DefaultPersistenceTestData();
    DefaultPersistenceTestData onSave = new DefaultPersistenceTestData();
    DefaultPersistenceTestData onDelete = new DefaultPersistenceTestData();

    @SuppressWarnings("serial")
    @Transformed
    class TestWorkflow extends PersistentWorkflow<Serializable> {

        @Override
        public void main() throws Interrupt {
        }

        @Override
        public void onDelete(PersistenceContext pc) {
            testWork(pc, onDelete);
        }

        @Override
        public void onLoad(PersistenceContext pc) {
            testWork(pc, onLoad);
        }

        @Override
        public void onSave(PersistenceContext pc) {
            testWork(pc, onSave);
        }

        private void testWork(PersistenceContext pc, final DefaultPersistenceTestData data) {
            EntityPersister<MasterEntity> masterEntityPersister = pc.getPersister(MasterEntity.class);
            EntityPersister<ChildEntity> childEntityPersister = pc.getPersister(ChildEntity.class);
            masterEntityPersister.select(data.selectedMaster, new EntityPersister.PostSelectedCallback<DefaultPersistenceTest.MasterEntity>() {
                @Override
                public void entitySelected(MasterEntity e) {
                    data.selectedMasterResult = e;
                }

                @Override
                public void entityNotFound(MasterEntity e) {
                    Assert.fail("Entity " + e + " not found");
                }
            });
            masterEntityPersister.insert(data.insertedMaster);
            masterEntityPersister.update(data.updatedMaster);
            masterEntityPersister.delete(data.deletedMaster);
            childEntityPersister.select(data.selectedChild, new EntityPersister.PostSelectedCallback<DefaultPersistenceTest.ChildEntity>() {
                @Override
                public void entitySelected(ChildEntity e) {
                    data.selectedChildResult = e;
                }

                @Override
                public void entityNotFound(ChildEntity e) {
                    Assert.fail("Entity " + e + " not found");
                }
            });
            childEntityPersister.insert(data.insertedChild);
            childEntityPersister.update(data.updatedChild);
            childEntityPersister.delete(data.deletedChild);
        }

    }

    @Test
    public void testPersistence() {
        DefaultPersistenceContextFactoryConfigurationBuilder configurationBuilder = new DefaultPersistenceContextFactoryConfigurationBuilder();
        ArrayList<WorkLogEntry<?>> work = new ArrayList<WorkLogEntry<?>>();
        MasterPersisterFactory masterPersisterFactory = new MasterPersisterFactory(work);
        ChildPersisterFactory childPersisterFactory = new ChildPersisterFactory(work);
        configurationBuilder.addPersisterFactory(masterPersisterFactory);
        configurationBuilder.addPersisterFactory(childPersisterFactory);
        DefaultPersistenceContextFactoryConfiguration configuration = configurationBuilder.compile();
        DefaultWorkflowPersistencePlugin plugin = new DefaultWorkflowPersistencePlugin(configuration);
        expectedWorkflow = new TestWorkflow();
        try {
            plugin.onWorkflowsDeleted(null, Arrays.<PersistentWorkflow<?>>asList(expectedWorkflow, expectedWorkflow));
            checkOrder(work, onDelete);
        } catch (SQLException e) {
            Assert.fail();
        }
        expectedWorkflow = new TestWorkflow();
        masterPersisterFactory.deletionWorkerCreated = false;
        masterPersisterFactory.selectionWorkerCreated = false;
        masterPersisterFactory.insertionWorkerCreated = false;
        masterPersisterFactory.updateWorkerCreated = false;
        childPersisterFactory.deletionWorkerCreated = false;
        childPersisterFactory.selectionWorkerCreated = false;
        childPersisterFactory.insertionWorkerCreated = false;
        childPersisterFactory.updateWorkerCreated = false;
        work.clear();
        try {
            plugin.onWorkflowsSaved(null, Arrays.<PersistentWorkflow<?>>asList(expectedWorkflow, expectedWorkflow));
            checkOrder(work, onSave);
        } catch (SQLException e) {
            Assert.fail();
        }
        expectedWorkflow = new TestWorkflow();
        masterPersisterFactory.deletionWorkerCreated = false;
        masterPersisterFactory.selectionWorkerCreated = false;
        masterPersisterFactory.insertionWorkerCreated = false;
        masterPersisterFactory.updateWorkerCreated = false;
        childPersisterFactory.deletionWorkerCreated = false;
        childPersisterFactory.selectionWorkerCreated = false;
        childPersisterFactory.insertionWorkerCreated = false;
        childPersisterFactory.updateWorkerCreated = false;
        work.clear();
        try {
            plugin.onWorkflowsLoaded(null, Arrays.<PersistentWorkflow<?>>asList(expectedWorkflow, expectedWorkflow));
            checkOrder(work, onLoad);
        } catch (SQLException e) {
            Assert.fail();
        }

    }

    private void checkOrder(ArrayList<WorkLogEntry<?>> work,
            DefaultPersistenceTestData data) {
        Assert.assertEquals(8, work.size());
        Assert.assertSame(data.selectedChild, data.selectedChildResult);
        Assert.assertSame(data.selectedMaster, data.selectedMasterResult);
        assertData(work.get(0), WorkLogEntry.Type.INSERT, data.insertedMaster);
        assertData(work.get(1), WorkLogEntry.Type.INSERT, data.insertedChild);
        assertData(work.get(2), WorkLogEntry.Type.UPDATE, data.updatedMaster);
        assertData(work.get(3), WorkLogEntry.Type.UPDATE, data.updatedChild);
        assertData(work.get(4), WorkLogEntry.Type.SELECT, data.selectedMaster);
        assertData(work.get(5), WorkLogEntry.Type.SELECT, data.selectedChild);
        assertData(work.get(6), WorkLogEntry.Type.DELETE, data.deletedChild);
        assertData(work.get(7), WorkLogEntry.Type.DELETE, data.deletedMaster);

    }

    private void assertData(WorkLogEntry<?> workLogEntry, Type type, Object expectedEntity) {
        Assert.assertEquals(2, workLogEntry.entities.size());
        for (int i = 0; i < 2; ++i) {
            Assert.assertSame(expectedEntity, workLogEntry.entities.get(i).entity);
            if (type == Type.SELECT)
                Assert.assertNotNull(workLogEntry.entities.get(i).callback);
            else
                Assert.assertNull(workLogEntry.entities.get(i).callback);
            Assert.assertSame(expectedWorkflow, workLogEntry.entities.get(i).workflow);
        }
        Assert.assertSame(type, workLogEntry.type);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings("DE_MIGHT_IGNORE")
    @Test
    public void testBrokenConfiguration() {
        DefaultPersistenceContextFactoryConfigurationBuilder configurationBuilder = new DefaultPersistenceContextFactoryConfigurationBuilder();
        ArrayList<WorkLogEntry<?>> work = new ArrayList<WorkLogEntry<?>>();
        MasterCyclePersisterFactory masterPersisterFactory = new MasterCyclePersisterFactory(work);
        ChildPersisterFactory childPersisterFactory = new ChildPersisterFactory(work);
        configurationBuilder.addPersisterFactory(masterPersisterFactory);
        configurationBuilder.addPersisterFactory(childPersisterFactory);
        try {
            configurationBuilder.compile();
            Assert.fail("Expected failure due to cycle in configuration");
        } catch (Exception e) {
            // ok
        }
    }

}
