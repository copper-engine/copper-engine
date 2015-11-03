package org.copperengine.core.persistent.cassandra.loadtest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.common.DefaultProcessorPoolManager;
import org.copperengine.core.common.JdkRandomUUIDFactory;
import org.copperengine.core.monitoring.LoggingStatisticCollector;
import org.copperengine.core.persistent.PersistentPriorityProcessorPool;
import org.copperengine.core.persistent.PersistentProcessorPool;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.copperengine.core.persistent.cassandra.CassandraSessionManagerImpl;
import org.copperengine.core.persistent.cassandra.CassandraStorage;
import org.copperengine.core.persistent.hybrid.DefaultTimeoutManager;
import org.copperengine.core.persistent.hybrid.HybridDBStorage;
import org.copperengine.core.persistent.hybrid.HybridTransactionController;
import org.copperengine.core.persistent.hybrid.Storage;
import org.copperengine.core.util.Backchannel;
import org.copperengine.core.util.BackchannelDefaultImpl;
import org.copperengine.core.util.PojoDependencyInjector;
import org.copperengine.ext.wfrepo.classpath.ClasspathWorkflowRepository;

public class CassandraEngineFactory {

    protected CassandraSessionManagerImpl cassandraSessionManager;
    protected Backchannel backchannel;
    protected PersistentScottyEngine engine;
    protected ExecutorService executor;
    protected LoggingStatisticCollector statisticCollector;

    public void createEngine(boolean truncate) throws Exception {
        statisticCollector = createStatisticsLogger();

        cassandraSessionManager = createCassandraSessionManager();

        if (truncate) {
            cassandraSessionManager.getSession().execute("truncate COP_WORKFLOW_INSTANCE");
            cassandraSessionManager.getSession().execute("truncate COP_EARLY_RESPONSE");
        }

        backchannel = createBackchannel();

        executor = createExecutorService();

        engine = createTestEngine();
    }

    protected ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    protected BackchannelDefaultImpl createBackchannel() {
        return new BackchannelDefaultImpl();
    }

    protected CassandraSessionManagerImpl createCassandraSessionManager() {
        CassandraSessionManagerImpl x =
                // new CassandraSessionManagerImpl(Collections.singletonList("localhost"), null, "copper");
                new CassandraSessionManagerImpl(Collections.singletonList("nuc1.scoop-gmbh.de"), null, "copper");
        x.startup();
        return x;
    }

    protected LoggingStatisticCollector createStatisticsLogger() {
        LoggingStatisticCollector statisticCollector = new LoggingStatisticCollector();
        statisticCollector.setLoggingIntervalSec(30);
        statisticCollector.setResetAfterLogging(true);
        statisticCollector.start();
        return statisticCollector;
    }

    public void destroyEngine() {
        if (engine != null)
            engine.shutdown();

        if (cassandraSessionManager != null)
            cassandraSessionManager.shutdown();

        if (executor != null)
            executor.shutdown();

        if (statisticCollector != null)
            statisticCollector.shutdown();
    }

    protected PersistentScottyEngine createTestEngine() {
        EngineIdProvider engineIdProvider = new EngineIdProviderBean("default");
        ClasspathWorkflowRepository wfRepository = new ClasspathWorkflowRepository("org.copperengine.core.persistent.cassandra.loadtest.workflows");
        wfRepository.start();

        Storage cassandra = createCassandraStorage();

        HybridDBStorage storage = createStorage(wfRepository, cassandra);
        PersistentPriorityProcessorPool ppool = new PersistentPriorityProcessorPool(PersistentProcessorPool.DEFAULT_POOL_ID, new HybridTransactionController(), Runtime.getRuntime().availableProcessors() * 4);
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

        DummyResponseSender dummyResponseSender = new DummyResponseSender(Executors.newScheduledThreadPool(4), engine);
        pojoDependencyInjector.register("dummyResponseSender", dummyResponseSender);
        pojoDependencyInjector.register("backchannel", backchannel);

        engine.startup();

        return engine;
    }

    protected HybridDBStorage createStorage(ClasspathWorkflowRepository wfRepository, Storage cassandra) {
        return new HybridDBStorage(new StandardJavaSerializer(), wfRepository, cassandra, new DefaultTimeoutManager().startup(), executor);
    }

    protected CassandraStorage createCassandraStorage() {
        return new CassandraStorage(cassandraSessionManager, executor, statisticCollector);
    }

}
