package org.copperengine.core.test.persistent.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.persistent.DataSourceFactory;
import org.copperengine.core.persistent.lock.PersistentLockManagerDialectSQL;
import org.copperengine.core.persistent.lock.PersistentLockManagerImpl;
import org.copperengine.core.util.BackchannelDefaultImpl;
import org.copperengine.core.util.PersistentEngineFactory;
import org.copperengine.core.util.PersistentEngineFactory.Engine;
import org.copperengine.core.util.PojoDependencyInjector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LockingWorkflowTest {

    private DataSource ds;
    private PersistentLockManagerImpl lockManager;
    private PojoDependencyInjector dependencyInjector = new PojoDependencyInjector();
    private PersistentProcessingEngine engine;
    private BackchannelDefaultImpl backchannel;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        ds = DataSourceFactory.createH2Datasource();

        Engine e = new PersistentEngineFactory().createEngine(ds, "src/workflow/java", "build/compiled_workflow", dependencyInjector);
        engine = e.engine;

        lockManager = new PersistentLockManagerImpl(engine, new PersistentLockManagerDialectSQL(), e.transactionController);

        backchannel = new BackchannelDefaultImpl();

        dependencyInjector.register("persistentLockManager", lockManager);
        dependencyInjector.register("backchannel", backchannel);

    }

    @After
    public void tearDown() throws Exception {
        engine.shutdown();
    }

    @Test
    public void testMain() throws Exception {
        List<WorkflowInstanceDescr<?>> wfid = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String wfId = engine.createUUID();
            wfid.add(new WorkflowInstanceDescr<String>("org.copperengine.core.test.persistent.lock.LockingWorkflow", "COPPER", wfId, null, null));
        }
        engine.runBatch(wfid);
        for (WorkflowInstanceDescr<?> x : wfid) {
            Boolean res = (Boolean) backchannel.wait(x.getId(), 10, TimeUnit.SECONDS);
            org.junit.Assert.assertNotNull(res);
            org.junit.Assert.assertTrue(res);
        }
    }
}
