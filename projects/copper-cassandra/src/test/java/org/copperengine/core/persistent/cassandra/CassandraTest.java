package org.copperengine.core.persistent.cassandra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.common.DefaultProcessorPoolManager;
import org.copperengine.core.common.JdkRandomUUIDFactory;
import org.copperengine.core.persistent.PersistentPriorityProcessorPool;
import org.copperengine.core.persistent.PersistentProcessorPool;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.copperengine.core.persistent.hybrid.DefaultTimeoutManager;
import org.copperengine.core.persistent.hybrid.HybridDBStorage;
import org.copperengine.core.persistent.hybrid.HybridTransactionController;
import org.copperengine.core.persistent.hybrid.Storage;
import org.copperengine.core.util.Backchannel;
import org.copperengine.core.util.BackchannelDefaultImpl;
import org.copperengine.core.util.PojoDependencyInjector;
import org.copperengine.ext.wfrepo.classpath.ClasspathWorkflowRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CassandraTest {

    private static Backchannel backchannel;
    private static PersistentScottyEngine engine;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        backchannel = new BackchannelDefaultImpl();
        engine = createTestEngine();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        engine.shutdown();
    }

    @Test
    public void testParallel() throws Exception {
        List<String> cids = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final String cid = engine.createUUID();
            final WorkflowInstanceDescr<String> wfid = new WorkflowInstanceDescr<String>("org.copperengine.core.persistent.cassandra.workflows.TestWorkflow", cid, cid, 1, null);
            engine.run(wfid);
            cids.add(cid);
        }
        for (String cid : cids) {
            Object response = backchannel.wait(cid, 2000, TimeUnit.MILLISECONDS);
            org.junit.Assert.assertNotNull("no response for workflow instance " + cid, response);
        }
    }

    @Test
    public void testSerial() throws Exception {
        for (int i = 0; i < 3; i++) {
            final String cid = engine.createUUID();
            engine.run("org.copperengine.core.persistent.cassandra.workflows.TestWorkflow", cid);
            Object response = backchannel.wait(cid, 1000, TimeUnit.MILLISECONDS);
            org.junit.Assert.assertNotNull(response);
        }
    }

    private static PersistentScottyEngine createTestEngine() {
        CassandraSessionManagerImpl cassandraSessionManagerImpl = new CassandraSessionManagerImpl(Collections.singletonList("localhost"), null, "copper");
        cassandraSessionManagerImpl.startup();

        EngineIdProvider engineIdProvider = new EngineIdProviderBean("default");
        ClasspathWorkflowRepository wfRepository = new ClasspathWorkflowRepository("org.copperengine.core.persistent.cassandra.workflows");
        wfRepository.start();

        Storage cassandra;
        // cassandra = new CassandraMock();
        cassandra = new CassandraStorage(cassandraSessionManagerImpl);

        HybridDBStorage storage = new HybridDBStorage(new StandardJavaSerializer(), wfRepository, cassandra, new DefaultTimeoutManager().startup());
        PersistentPriorityProcessorPool ppool = new PersistentPriorityProcessorPool(PersistentProcessorPool.DEFAULT_POOL_ID, new HybridTransactionController());
        ppool.setEmptyQueueWaitMSec(2);
        ppool.setDequeueBulkSize(50);
        List<PersistentProcessorPool> pools = new ArrayList<PersistentProcessorPool>();
        pools.add(ppool);
        DefaultProcessorPoolManager<PersistentProcessorPool> processorPoolManager = new DefaultProcessorPoolManager<PersistentProcessorPool>();
        processorPoolManager.setProcessorPools(pools);

        PojoDependencyInjector pojoDependencyInjector = new PojoDependencyInjector();

        PersistentScottyEngine engine = new PersistentScottyEngine();
        engine.setDbStorage(storage);
        engine.setWfRepository(wfRepository);
        engine.setEngineIdProvider(engineIdProvider);
        engine.setIdFactory(new JdkRandomUUIDFactory());
        engine.setProcessorPoolManager(processorPoolManager);
        engine.setDependencyInjector(pojoDependencyInjector);

        DummyResponseSender dummyResponseSender = new DummyResponseSender(Executors.newSingleThreadScheduledExecutor(), engine);
        pojoDependencyInjector.register("dummyResponseSender", dummyResponseSender);
        pojoDependencyInjector.register("backchannel", backchannel);

        engine.startup();

        return engine;
    }

}
