/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import de.scoopgmbh.copper.DependencyInjector;
import de.scoopgmbh.copper.EngineIdProvider;
import de.scoopgmbh.copper.EngineIdProviderBean;
import de.scoopgmbh.copper.PersistentProcessingEngine;
import de.scoopgmbh.copper.batcher.RetryingTxnBatchRunner;
import de.scoopgmbh.copper.batcher.impl.BatcherImpl;
import de.scoopgmbh.copper.common.DefaultProcessorPoolManager;
import de.scoopgmbh.copper.common.JdkRandomUUIDFactory;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.persistent.DatabaseDialect;
import de.scoopgmbh.copper.persistent.DerbyDbDialect;
import de.scoopgmbh.copper.persistent.OracleDialect;
import de.scoopgmbh.copper.persistent.PersistentPriorityProcessorPool;
import de.scoopgmbh.copper.persistent.PersistentProcessorPool;
import de.scoopgmbh.copper.persistent.PersistentScottyEngine;
import de.scoopgmbh.copper.persistent.ScottyDBStorage;
import de.scoopgmbh.copper.persistent.txn.CopperTransactionController;
import de.scoopgmbh.copper.wfrepo.FileBasedWorkflowRepository;

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
			throw new Error("No dialect available for DBMS "+name);
		}
		finally {
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
