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
package org.copperengine.core.persistent.lock;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.db.utility.JdbcUtils;
import org.copperengine.core.persistent.DataSourceFactory;
import org.copperengine.core.persistent.txn.CopperTransactionController;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class PersistentLockManagerImplTest {

    private static ComboPooledDataSource dataSource;

    @AfterClass
    public static void afterClass() {
        dataSource.close();
        dataSource = null;
    }

    static Connection getConnection() throws SQLException {
        Connection c = dataSource.getConnection();
        c.setAutoCommit(false);
        return c;
    }

    @Before
    public void beforeTest() throws Exception {
        if (dataSource == null) {
            dataSource = DataSourceFactory.createOracleDatasource();
        }

        Connection con = getConnection();
        try {
            con.createStatement().execute("DELETE FROM COP_LOCK");
            con.commit();
        } finally {
            JdbcUtils.closeConnection(con);
        }
    }

    @Test
    public void testAcquireLock() throws Exception {
        final String LOCK_ID = "LOCK";
        final PersistentProcessingEngine engine = Mockito.mock(PersistentProcessingEngine.class);
        final PersistentLockManager impl = new PersistentLockManagerImpl(engine, new PersistentLockManagerDialectOracleMultiInstance(), new CopperTransactionController(dataSource));
        // final PersistentLockManager impl = new PersistentLockManagerImpl(engine, new
        // PersistentLockManagerDialectSQL(), new CopperTransactionController(dataSource));
        final List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 4; i++) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " started");
                    try {
                        final String workflowInstanceId = UUID.randomUUID().toString();
                        final Random random = new Random();
                        for (int i = 0; i < 1000; i++) {
                            final String correlationId = UUID.randomUUID().toString();
                            impl.acquireLock(LOCK_ID, correlationId, workflowInstanceId);
                            Thread.sleep(random.nextInt(10));
                            impl.releaseLock(LOCK_ID, workflowInstanceId);
                            if (i % 100 == 0) {
                                System.out.println(Thread.currentThread().getName() + ": " + i);
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + " finished");
                }
            };
            t.start();
            threads.add(t);
        }
        for (Thread t : threads) {
            t.join();
        }
    }
}
