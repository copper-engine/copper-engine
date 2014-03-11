package org.copperengine.core.persistent.lock;

import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.db.utility.JdbcUtils;
import org.copperengine.core.persistent.DataSourceFactory;
import org.junit.Assert;
import org.junit.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class PersistentLockManagerDialectOracleTest extends AbstractPersistentLockManagerDialectTest {

    @Override
    protected ComboPooledDataSource createDatasource() {
        return DataSourceFactory.createOracleDatasource();
    }

    @Override
    protected PersistentLockManagerDialect createImplementation() {
        return new PersistentLockManagerDialectOracleMultiInstance();
    }

    @Test
    public void testSupportsMultipleInstances() {
        Assert.assertTrue(createImplementation().supportsMultipleInstances());
    }

    @Test
    public void testAcquireLock_MultiConcurrent() throws Exception {
        final Connection con = getConnection();
        final Connection con2 = getConnection();
        final Connection con3 = getConnection();
        final ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            final PersistentLockManagerDialectOracleMultiInstance x = new PersistentLockManagerDialectOracleMultiInstance();
            final String _lockId = "4711";

            final String correlationId1 = UUID.randomUUID().toString();
            final String workflowInstanceId1 = UUID.randomUUID().toString();
            final String correlationId2 = UUID.randomUUID().toString();
            final String workflowInstanceId2 = UUID.randomUUID().toString();
            final String correlationId3 = UUID.randomUUID().toString();
            final String workflowInstanceId3 = UUID.randomUUID().toString();

            String rv = x.acquireLock(_lockId, workflowInstanceId1, correlationId1, null, con);
            org.junit.Assert.assertEquals(correlationId1, rv);

            rv = x.acquireLock(_lockId, workflowInstanceId1, correlationId1, null, con);
            org.junit.Assert.assertNull(rv);

            final CountDownLatch latch = new CountDownLatch(1);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String rv = x.acquireLock(_lockId, workflowInstanceId2, correlationId2, null, con2);
                        org.junit.Assert.assertNull(rv);
                        con2.commit();
                        latch.countDown();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            org.junit.Assert.assertEquals(1, latch.getCount());
            con.commit();
            latch.await(10, TimeUnit.SECONDS);
            org.junit.Assert.assertEquals(0, latch.getCount());

            rv = x.acquireLock(_lockId, workflowInstanceId3, correlationId3, null, con);
            org.junit.Assert.assertNull(rv);
            con.commit();

            rv = x.releaseLock(_lockId, workflowInstanceId1, con);
            con.commit();
            org.junit.Assert.assertEquals(correlationId2, rv);

            rv = x.releaseLock(_lockId, workflowInstanceId2, con);
            org.junit.Assert.assertEquals(correlationId3, rv);
            con.commit();

            rv = x.releaseLock(_lockId, workflowInstanceId3, con);
            org.junit.Assert.assertNull(rv);
            con.commit();

        } finally {
            con.rollback();
            con2.rollback();
            con3.rollback();
            JdbcUtils.closeConnection(con);
            JdbcUtils.closeConnection(con2);
            JdbcUtils.closeConnection(con3);
            executor.shutdown();
        }
    }

}
