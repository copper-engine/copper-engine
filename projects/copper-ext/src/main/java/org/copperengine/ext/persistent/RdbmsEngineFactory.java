/**
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
package org.copperengine.ext.persistent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.DependencyInjector;
import org.copperengine.core.EngineIdProvider;
import org.copperengine.core.batcher.Batcher;
import org.copperengine.core.batcher.RetryingTxnBatchRunner;
import org.copperengine.core.batcher.impl.BatcherImpl;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.persistent.DatabaseDialect;
import org.copperengine.core.persistent.DerbyDbDialect;
import org.copperengine.core.persistent.H2Dialect;
import org.copperengine.core.persistent.MySqlDialect;
import org.copperengine.core.persistent.OracleDialect;
import org.copperengine.core.persistent.OracleSimpleDialect;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.PostgreSQLDialect;
import org.copperengine.core.persistent.ScottyDBStorage;
import org.copperengine.core.persistent.ScottyDBStorageInterface;
import org.copperengine.core.persistent.txn.CopperTransactionController;
import org.copperengine.core.persistent.txn.TransactionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Utility class to create a RDBMS (e.g. oracle or mysql) {@link PersistentScottyEngine}.
 * <p>
 * Usage is quite simple, e.g. using a SupplierDependencyInjector:
 * 
 * <pre>
 * RdbmsEngineFactory&lt;SupplierDependencyInjector&gt; engineFactory = new
 *         RdbmsEngineFactory&lt;SupplierDependencyInjector&gt;(Arrays.asList(&quot;package.of.copper.workflow.classes&quot;)) {
 * 
 *             protected SupplierDependencyInjector createDependencyInjector() {
 *                 return new SupplierDependencyInjector();
 *             }
 * 
 *             protected DataSource createDataSource() {
 *                 return ...;
 *             }
 *         };
 * engineFactory.getEngine().startup();
 * </pre>
 * 
 * @author austermann
 *
 * @param <T>
 *        type of DependencyInjector which shall be used from the created engine
 */
public abstract class RdbmsEngineFactory<T extends DependencyInjector> extends AbstractPersistentEngineFactory<T> {

    private static final Logger logger = LoggerFactory.getLogger(RdbmsEngineFactory.class);

    protected final Supplier<DataSource> dataSource;
    protected final Supplier<BatcherImpl> batcher;

    private int numberOfBatcherThreads = 4;

    public RdbmsEngineFactory(List<String> wfPackges) {
        super(wfPackges);
        dataSource = Suppliers.memoize(new Supplier<DataSource>() {
            @Override
            public DataSource get() {
                logger.info("Creating DataSource...");
                return createDataSource();
            }
        });
        batcher = Suppliers.memoize(new Supplier<BatcherImpl>() {
            @Override
            public BatcherImpl get() {
                logger.info("Creating Batcher...");
                return createBatcher();
            }
        });
    }

    public void setNumberOfBatcherThreads(int numberOfBatcherThreads) {
        this.numberOfBatcherThreads = numberOfBatcherThreads;
    }

    protected abstract DataSource createDataSource();

    protected BatcherImpl createBatcher() {
        @SuppressWarnings("rawtypes")
        RetryingTxnBatchRunner batchRunner = new RetryingTxnBatchRunner();
        batchRunner.setDataSource(dataSource.get());
        BatcherImpl batcher = new BatcherImpl(numberOfBatcherThreads);
        batcher.setBatchRunner(batchRunner);
        batcher.startup();
        return batcher;
    }

    @Override
    protected TransactionController createTransactionController() {
        CopperTransactionController txnController = new CopperTransactionController();
        txnController.setDataSource(dataSource.get());
        return txnController;
    }

    @Override
    protected ScottyDBStorageInterface createDBStorage() {
        DatabaseDialect dialect = createDatabaseDialect();

        final ScottyDBStorage dbStorage = new ScottyDBStorage();
        dbStorage.setDialect(dialect);
        dbStorage.setTransactionController(transactionController.get());
        dbStorage.setBatcher(batcher.get());

        return dbStorage;
    }

    protected DatabaseDialect createDatabaseDialect() {
        DatabaseDialect dialect = createDialect(dataSource.get(), workflowRepository.get(), engineIdProvider.get());
        dialect.startup();
        return dialect;
    }

    protected DatabaseDialect createDialect(DataSource ds, WorkflowRepository wfRepository, EngineIdProvider engineIdProvider) {
        Connection c = null;
        try {
            c = ds.getConnection();
            String name = c.getMetaData().getDatabaseProductName();
            if ("oracle".equalsIgnoreCase(name)) {
                if (OracleDialect.schemaMatches(c)) {
                    OracleDialect dialect = new OracleDialect();
                    dialect.setWfRepository(wfRepository);
                    dialect.setEngineIdProvider(engineIdProvider);
                    dialect.setMultiEngineMode(false);
                    return dialect;
                }
                else {
                    OracleSimpleDialect dialect = new OracleSimpleDialect();
                    dialect.setWfRepository(wfRepository);
                    return dialect;
                }
            }
            if ("Apache Derby".equalsIgnoreCase(name)) {
                DerbyDbDialect dialect = new DerbyDbDialect();
                dialect.setDataSource(ds);
                dialect.setWfRepository(wfRepository);
                return dialect;
            }
            if ("H2".equalsIgnoreCase(name)) {
                H2Dialect dialect = new H2Dialect();
                dialect.setDataSource(ds);
                dialect.setWfRepository(wfRepository);
                return dialect;
            }
            if ("MySQL".equalsIgnoreCase(name)) {
                MySqlDialect dialect = new MySqlDialect();
                dialect.setWfRepository(wfRepository);
                return dialect;
            }
            if ("PostgreSQL".equalsIgnoreCase(name)) {
                PostgreSQLDialect dialect = new PostgreSQLDialect();
                dialect.setWfRepository(wfRepository);
                return dialect;
            }
            throw new Error("No dialect available for DBMS " + name);
        } catch (Exception e) {
            throw new CopperRuntimeException("Unable to create dialect", e);
        } finally {
            if (c != null)
                try {
                    c.close();
                } catch (SQLException e) {
                    logger.error("unable to close connection", e);
                }
        }
    }

    public Batcher getBatcher() {
        return batcher.get();
    }

    @Override
    public void destroyEngine() {
        super.destroyEngine();
        batcher.get().shutdown();
    }
}
