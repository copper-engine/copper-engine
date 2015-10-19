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

package org.copperengine.core.persistent.cassandra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.common.DefaultProcessorPoolManager;
import org.copperengine.core.common.JdkRandomUUIDFactory;
import org.copperengine.core.persistent.PersistentPriorityProcessorPool;
import org.copperengine.core.persistent.PersistentProcessorPool;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.copperengine.core.util.Backchannel;
import org.copperengine.core.util.BackchannelDefaultImpl;
import org.copperengine.core.util.PojoDependencyInjector;
import org.copperengine.ext.wfrepo.classpath.ClasspathWorkflowRepository;
import org.junit.Test;

public class SimpleCassandraTest {

    private Backchannel backchannel = new BackchannelDefaultImpl();

    private PersistentScottyEngine createTestEngine() {
        CassandraSessionManagerImpl cassandraSessionManagerImpl = new CassandraSessionManagerImpl(Collections.singletonList("localhost"), null, "copper");
        cassandraSessionManagerImpl.startup();

        EngineIdProvider engineIdProvider = new EngineIdProviderBean("default");
        ClasspathWorkflowRepository wfRepository = new ClasspathWorkflowRepository("org.copperengine.core.persistent.cassandra.workflows");
        wfRepository.start();

        Cassandra cassandra;
        // cassandra = new CassandraMock();
        cassandra = new CassandraImpl(cassandraSessionManagerImpl);

        CassandraDBStorage storage = new CassandraDBStorage(new StandardJavaSerializer(), wfRepository, cassandra);
        PersistentPriorityProcessorPool ppool = new PersistentPriorityProcessorPool(PersistentProcessorPool.DEFAULT_POOL_ID, new CassandraTransactionController());
        ppool.setEmptyQueueWaitMSec(2);
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

    @Test
    public void testExec() throws Exception {
        PersistentScottyEngine engine = createTestEngine();
        try {
            final String cid = engine.createUUID();
            engine.run("org.copperengine.core.persistent.cassandra.workflows.TestWorkflow", cid);
            Object response = backchannel.wait(cid, 1000, TimeUnit.MILLISECONDS);
            org.junit.Assert.assertNotNull(response);
        } finally {
            engine.shutdown();
        }
    }
}
