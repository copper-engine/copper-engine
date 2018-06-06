/**
 * Copyright 2002-2017 SCOOP Software GmbH
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
package org.copperengine.regtest.test.persistent;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import javax.sql.DataSource;

import org.copperengine.core.DependencyInjector;
import org.copperengine.core.ProcessingEngine;
import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.batcher.impl.BatcherImpl;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.db.utility.RetryingTransaction;
import org.copperengine.core.persistent.AbstractSqlDialect;
import org.copperengine.core.persistent.DatabaseDialect;
import org.copperengine.core.persistent.OracleDialect;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.lock.PersistentLockManager;
import org.copperengine.core.persistent.lock.PersistentLockManagerDialectPostgres;
import org.copperengine.core.persistent.lock.PersistentLockManagerDialectSQL;
import org.copperengine.core.persistent.lock.PersistentLockManagerImpl;
import org.copperengine.core.persistent.txn.CopperTransactionController;
import org.copperengine.core.persistent.txn.TransactionController;
import org.copperengine.core.util.Backchannel;
import org.copperengine.core.util.BackchannelDefaultImpl;
import org.copperengine.core.wfrepo.FileBasedWorkflowRepository;
import org.copperengine.ext.persistent.RdbmsEngineFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.copperengine.regtest.persistent.DataSourceFactory;
import org.copperengine.regtest.test.DBMockAdapter;
import org.copperengine.regtest.test.DataHolder;
import org.copperengine.regtest.test.MockAdapter;
import org.copperengine.regtest.test.TestContext;
import org.copperengine.regtest.test.backchannel.BackChannelQueue;
import org.copperengine.regtest.test.persistent.jmx.JmxTestAdapter;

public class PersistentEngineTestContext extends TestContext {

    protected final Supplier<RdbmsEngineFactory<DependencyInjector>> engineFactoryRed;
    protected final Supplier<ComboPooledDataSource> dataSource;
    protected final Supplier<DataHolder> dataHolder;
    protected final Supplier<BatchingAuditTrail> auditTrail;
    protected final Supplier<DBMockAdapter> dbMockAdapter;
    protected final Supplier<PersistentLockManager> lockManager;
    protected final Supplier<Backchannel> backchannel;
    protected final Supplier<JmxTestAdapter> jmxTestAdapter;

    public PersistentEngineTestContext(final DataSourceType dataSourceType, final boolean cleanDB) {
        this(dataSourceType, cleanDB, "default", false);
    }

    public PersistentEngineTestContext(final DataSourceType dataSourceType, final boolean cleanDB, final String engineId, final boolean multiEngineMode) {
        jmxTestAdapter = Suppliers.memoize(new Supplier<JmxTestAdapter>() {
            @Override
            public JmxTestAdapter get() {
                return createJmxTestAdapter();
            }
        });
        suppliers.put("jmxTestAdapter", jmxTestAdapter);
        
        backchannel = Suppliers.memoize(new Supplier<Backchannel>() {
            @Override
            public Backchannel get() {
                return createBackchannel();
            }
        });
        suppliers.put("backchannel", backchannel);

        lockManager = Suppliers.memoize(new Supplier<PersistentLockManager>() {
            @Override
            public PersistentLockManager get() {
                return createPersistentLockManager(dataSourceType);
            }
        });
        suppliers.put("persistentLockManager", lockManager);

        dbMockAdapter = Suppliers.memoize(new Supplier<DBMockAdapter>() {
            @Override
            public DBMockAdapter get() {
                return createDBMockAdapter();
            }
        });
        suppliers.put("dbMockAdapter", dbMockAdapter);

        auditTrail = Suppliers.memoize(new Supplier<BatchingAuditTrail>() {
            @Override
            public BatchingAuditTrail get() {
                return createBatchingAuditTrail();
            }
        });
        suppliers.put("auditTrail", auditTrail);

        dataHolder = Suppliers.memoize(new Supplier<DataHolder>() {
            @Override
            public DataHolder get() {
                return createDataHolder();
            }
        });
        suppliers.put("dataHolder", dataHolder);

        dataSource = Suppliers.memoize(new Supplier<ComboPooledDataSource>() {
            @Override
            public ComboPooledDataSource get() {
                ComboPooledDataSource ds = createDataSource(dataSourceType);
                if (cleanDB && ds != null) {
                    cleanDB(ds);
                }
                return ds;
            }
        });
        suppliers.put("dataSource", dataSource);

        engineFactoryRed = Suppliers.memoize(new Supplier<RdbmsEngineFactory<DependencyInjector>>() {
            @Override
            public RdbmsEngineFactory<DependencyInjector> get() {
                return createRdbmsEngineFactory(engineId, multiEngineMode);
            }
        });
        suppliers.put("engineFactoryRed", engineFactoryRed);
    }

    protected JmxTestAdapter createJmxTestAdapter() {
        return new JmxTestAdapter(engineFactoryRed.get().getEngine());
    }

    protected Backchannel createBackchannel() {
        return new BackchannelDefaultImpl();
    }

    protected PersistentLockManager createPersistentLockManager(final DataSourceType dataSourceType) {
        if (dataSourceType == DataSourceType.Postgres) {
            return new PersistentLockManagerImpl(engineFactoryRed.get().getEngine(), new PersistentLockManagerDialectPostgres(), new CopperTransactionController(dataSource.get()));
        } else {
            return new PersistentLockManagerImpl(engineFactoryRed.get().getEngine(), new PersistentLockManagerDialectSQL(), new CopperTransactionController(dataSource.get()));
        }
    }

    protected DBMockAdapter createDBMockAdapter() {
        DBMockAdapter dbMockAdapter = new DBMockAdapter();
        dbMockAdapter.setDataSource(dataSource.get());
        dbMockAdapter.setEngine(getEngine());
        return dbMockAdapter;
    }

    protected BatchingAuditTrail createBatchingAuditTrail() {
        BatchingAuditTrail batchingAuditTrail = new BatchingAuditTrail();
        batchingAuditTrail.setDataSource(dataSource.get());
        batchingAuditTrail.setBatcher(engineFactoryRed.get().getBatcher());
        return batchingAuditTrail;
    }

    protected void cleanDB(DataSource ds) {
        try {
            new RetryingTransaction<Void>(ds) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_AUDIT_TRAIL_EVENT");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_WAIT");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_RESPONSE");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_QUEUE");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_WORKFLOW_INSTANCE_ERROR");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_WORKFLOW_INSTANCE");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_LOCK");
                    stmt.close();
                    return null;
                }
            }.run();
        } catch (Exception e) {
            throw new RuntimeException("cleanDB failed", e);
        }
    }

    private Statement createStatement(Connection con) throws SQLException {
        return con.createStatement();
    }

    protected ComboPooledDataSource createDataSource(final DataSourceType dataSourceType) {
        ComboPooledDataSource ds;
        if (dataSourceType == DataSourceType.H2) {
            ds = DataSourceFactory.createH2Datasource();
        }
        else if (dataSourceType == DataSourceType.DerbyDB) {
            ds = DataSourceFactory.createDerbyDbDatasource();
        }
        else if (dataSourceType == DataSourceType.Oracle) {
            ds = DataSourceFactory.createOracleDatasource();
        }
        else if (dataSourceType == DataSourceType.MySQL) {
            ds = DataSourceFactory.createMySqlDatasource();
        }
        else if (dataSourceType == DataSourceType.Postgres) {
            ds = DataSourceFactory.createPostgresDatasource();
        }
        else if (dataSourceType == DataSourceType.Oracle_simple) {
            ds = DataSourceFactory.createOracleSimpleDatasource();
        }
        else {
            throw new IllegalArgumentException(dataSourceType.name());
        }
        return ds;
    }

    protected DataHolder createDataHolder() {
        return new DataHolder();
    }

    protected RdbmsEngineFactory<DependencyInjector> createRdbmsEngineFactory(final String engineId, final boolean multiEngineMode) {
        RdbmsEngineFactory<DependencyInjector> x = new RdbmsEngineFactory<DependencyInjector>(Collections.<String>emptyList()) {

            @Override
            protected DataSource createDataSource() {
                return PersistentEngineTestContext.this.dataSource.get();
            }

            @Override
            protected DependencyInjector createDependencyInjector() {
                return PersistentEngineTestContext.this.dependencyInjector.get();
            }

            @Override
            protected WorkflowRepository createWorkflowRepository() {
                FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
                repo.setSourceDirs("src/workflow/java");
                repo.setTargetDir("build/compiled_workflow_" + engineId);
                repo.setLoadNonWorkflowClasses(true);
                return repo;
            }

            @Override
            protected DatabaseDialect createDatabaseDialect() {
                DatabaseDialect x = super.createDatabaseDialect();
                if (multiEngineMode) {
                    if (x instanceof OracleDialect) {
                        ((OracleDialect) x).setMultiEngineMode(true);
                    }
                    else if (x instanceof AbstractSqlDialect) {
                        ((AbstractSqlDialect) x).setMultiEngineMode(true);
                    }
                    else {
                        throw new RuntimeException("Unexpected DatabaseDialect: " + x.getClass().getName());
                    }
                }
                return x;
            }

            @Override
            protected TransactionController createTransactionController() {
                CopperTransactionController txnController = new CopperTransactionController();
                txnController.setDataSource(dataSource.get());
                txnController.setMaxConnectRetries(1);
                return txnController;
            }

        };
        x.setEngineId(engineId);
        return x;
    }

    @Override
    public void startup() {
        super.startup();
        try {
            mockAdapter.get().startup();
            dbMockAdapter.get().startup();
            engineFactoryRed.get().getEngine().startup();
            auditTrail.get().startup();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("startup failed", e);
        }
    }

    @Override
    public void shutdown() {
        if (dataSource.get() != null) {
            engineFactoryRed.get().destroyEngine();
            mockAdapter.get().shutdown();
            dbMockAdapter.get().shutdown();
            dataSource.get().close();
            super.shutdown();
        }
    }

    public MockAdapter getMockAdapter() {
        return mockAdapter.get();
    }

    public DataSource getDataSource() {
        return dataSource.get();
    }

    public PersistentScottyEngine getEngine() {
        return engineFactoryRed.get().getEngine();
    }

    public BackChannelQueue getBackChannelQueue() {
        return backChannelQueue.get();
    }

    public BatchingAuditTrail getAuditTrail() {
        return auditTrail.get();
    }

    public boolean isDbmsAvailable() {
        try {
            DataSource ds = getDataSource();
            ds.setLoginTimeout(10);
            ds.getConnection();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public DataHolder getDataHolder() {
        return dataHolder.get();
    }

    @Override
    protected ProcessingEngine getProcessingEngine() {
        return getEngine();
    }

    public Backchannel getBackchannel() {
        return backchannel.get();
    }

    public BatcherImpl getBatcher() {
        return (BatcherImpl) engineFactoryRed.get().getBatcher();
    }

}
