/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.core.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.copperengine.core.DependencyInjector;
import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.batcher.RetryingTxnBatchRunner;
import org.copperengine.core.batcher.impl.BatcherImpl;
import org.copperengine.core.common.DefaultProcessorPoolManager;
import org.copperengine.core.common.JdkRandomUUIDFactory;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.persistent.DatabaseDialect;
import org.copperengine.core.persistent.DerbyDbDialect;
import org.copperengine.core.persistent.OracleDialect;
import org.copperengine.core.persistent.PersistentPriorityProcessorPool;
import org.copperengine.core.persistent.PersistentProcessorPool;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.ScottyDBStorage;
import org.copperengine.core.persistent.txn.CopperTransactionController;
import org.copperengine.core.wfrepo.FileBasedWorkflowRepository;

public class PersistentEngineFactory {

    private DatabaseDialect createDialect(DataSource ds, WorkflowRepository wfRepository, EngineIdProvider engineIdProvider) throws SQLException {
        Connection c = ds.getConnection();
        try {
            String name = c.getMetaData().getDatabaseProductName();
            if ("oracle".equalsIgnoreCase(name)) {
                OracleDialect dialect = new OracleDialect();
                dialect.setWfRepository(wfRepository);
                dialect.setEngineIdProvider(engineIdProvider);
                dialect.setMultiEngineMode(false);
                return dialect;
            }
            if ("Apache Derby".equalsIgnoreCase(name)) {
                DerbyDbDialect dialect = new DerbyDbDialect();
                dialect.setDataSource(ds);
                dialect.setWfRepository(wfRepository);
                return dialect;
            }
            throw new Error("No dialect available for DBMS " + name);
        } finally {
            c.close();
        }
    }

    public PersistentProcessingEngine createEngine(DataSource dataSource, String wfRepoSourceDir, String wfRepoTargetDir, DependencyInjector dependencyInjector) throws Exception {
        EngineIdProvider engineIdProvider = new EngineIdProviderBean("default");
        FileBasedWorkflowRepository wfRepository = new FileBasedWorkflowRepository();
        wfRepository.setSourceDirs(Collections.singletonList(wfRepoSourceDir));
        wfRepository.setTargetDir(wfRepoTargetDir);
        wfRepository.start();

        CopperTransactionController txnController = new CopperTransactionController();
        txnController.setDataSource(dataSource);

        DatabaseDialect dialect = createDialect(dataSource, wfRepository, engineIdProvider);
        dialect.startup();

        @SuppressWarnings("rawtypes")
        RetryingTxnBatchRunner batchRunner = new RetryingTxnBatchRunner();
        batchRunner.setDataSource(dataSource);
        BatcherImpl batcher = new BatcherImpl(4);
        batcher.setBatchRunner(batchRunner);
        batcher.startup();

        ScottyDBStorage dbStorage = new ScottyDBStorage();
        dbStorage.setDialect(dialect);
        dbStorage.setTransactionController(txnController);
        dbStorage.setBatcher(batcher);

        PersistentPriorityProcessorPool ppool = new PersistentPriorityProcessorPool(PersistentProcessorPool.DEFAULT_POOL_ID, txnController);
        List<PersistentProcessorPool> pools = new ArrayList<PersistentProcessorPool>();
        pools.add(ppool);
        DefaultProcessorPoolManager<PersistentProcessorPool> processorPoolManager = new DefaultProcessorPoolManager<PersistentProcessorPool>();
        processorPoolManager.setProcessorPools(pools);

        PersistentScottyEngine engine = new PersistentScottyEngine();
        engine.setDbStorage(dbStorage);
        engine.setWfRepository(wfRepository);
        engine.setEngineIdProvider(engineIdProvider);
        engine.setIdFactory(new JdkRandomUUIDFactory());
        engine.setProcessorPoolManager(processorPoolManager);
        engine.setDependencyInjector(dependencyInjector);

        return engine;
    }

}
