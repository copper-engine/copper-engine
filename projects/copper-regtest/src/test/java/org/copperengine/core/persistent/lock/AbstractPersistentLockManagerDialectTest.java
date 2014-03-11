package org.copperengine.core.persistent.lock;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import org.copperengine.core.db.utility.JdbcUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public abstract class AbstractPersistentLockManagerDialectTest {

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
            dataSource = createDatasource();
        }

        Connection con = getConnection();
        try {
            con.createStatement().execute("DELETE FROM COP_LOCK");
            con.commit();
        } finally {
            JdbcUtils.closeConnection(con);
        }
    }

    protected abstract ComboPooledDataSource createDatasource();

    protected abstract PersistentLockManagerDialect createImplementation();

    @Test
    public void testAcquireLock_Simple() throws Exception {
        Connection con = getConnection();
        try {
            PersistentLockManagerDialect x = createImplementation();
            Date _insertTS = new Date();
            String _correlationId = UUID.randomUUID().toString();
            String _lockId = "4711";
            String _workflowInstanceId = "123456";
            String rv = x.acquireLock(_lockId, _workflowInstanceId, _correlationId, _insertTS, con);
            org.junit.Assert.assertEquals(_correlationId, rv);

            rv = x.releaseLock(_lockId, _workflowInstanceId, con);
            org.junit.Assert.assertNull(rv);

        } finally {
            con.rollback();
            JdbcUtils.closeConnection(con);
        }
    }

    @Test
    public void testAcquireLock_Dubbled() throws Exception {
        Connection con = getConnection();
        try {
            PersistentLockManagerDialect x = createImplementation();
            Date _insertTS = new Date();
            String _correlationId = UUID.randomUUID().toString();
            String _lockId = "4711";
            String _workflowInstanceId = "123456";
            String rv = x.acquireLock(_lockId, _workflowInstanceId, _correlationId, _insertTS, con);
            org.junit.Assert.assertEquals(_correlationId, rv);

            rv = x.acquireLock(_lockId, _workflowInstanceId, _correlationId, _insertTS, con);
            org.junit.Assert.assertNull(rv);

            rv = x.acquireLock(_lockId, _workflowInstanceId, _correlationId, _insertTS, con);
            org.junit.Assert.assertNull(rv);

            rv = x.acquireLock(_lockId, _workflowInstanceId, _correlationId, _insertTS, con);
            org.junit.Assert.assertNull(rv);

            rv = x.releaseLock(_lockId, _workflowInstanceId, con);
            org.junit.Assert.assertNull(rv);

        } finally {
            con.rollback();
            JdbcUtils.closeConnection(con);
        }
    }

    @Test
    public void testAcquireLock_Multi() throws Exception {
        Connection con = getConnection();
        try {
            PersistentLockManagerDialect x = createImplementation();
            String _lockId = "4711";

            Date _insertTS1 = new Date(System.currentTimeMillis() - 5000);
            Date _insertTS2 = new Date(System.currentTimeMillis() - 4000);
            Date _insertTS3 = new Date(System.currentTimeMillis() - 3000);
            String correlationId1 = UUID.randomUUID().toString();
            String workflowInstanceId1 = UUID.randomUUID().toString();
            String correlationId2 = UUID.randomUUID().toString();
            String workflowInstanceId2 = UUID.randomUUID().toString();
            String correlationId3 = UUID.randomUUID().toString();
            String workflowInstanceId3 = UUID.randomUUID().toString();

            String rv = x.acquireLock(_lockId, workflowInstanceId1, correlationId1, _insertTS1, con);
            org.junit.Assert.assertEquals(correlationId1, rv);
            Thread.sleep(5);

            rv = x.acquireLock(_lockId, workflowInstanceId1, correlationId1, _insertTS1, con);
            org.junit.Assert.assertNull(rv);
            Thread.sleep(5);

            rv = x.acquireLock(_lockId, workflowInstanceId2, correlationId2, _insertTS2, con);
            org.junit.Assert.assertNull(rv);
            Thread.sleep(5);

            rv = x.acquireLock(_lockId, workflowInstanceId3, correlationId3, _insertTS3, con);
            org.junit.Assert.assertNull(rv);
            Thread.sleep(5);

            rv = x.releaseLock(_lockId, workflowInstanceId1, con);
            org.junit.Assert.assertEquals(correlationId2, rv);
            Thread.sleep(5);

            rv = x.releaseLock(_lockId, workflowInstanceId2, con);
            org.junit.Assert.assertEquals(correlationId3, rv);
            Thread.sleep(5);

            rv = x.releaseLock(_lockId, workflowInstanceId3, con);
            org.junit.Assert.assertNull(rv);

        } finally {
            con.rollback();
            JdbcUtils.closeConnection(con);
        }
    }

}
