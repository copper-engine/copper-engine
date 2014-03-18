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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.copperengine.core.db.utility.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link PersistentLockManagerDialect} for most supported SQL databases, currently Oracle,
 * DerbyDB, H2 and MySQL.
 * It does not support multiple instances working on the same database. See
 * {@link PersistentLockManagerDialectOracleMultiInstance} for an Oracle implementation supporting multiple distributes
 * instances.
 * It does also not support Postgres. See {@link PersistentLockManagerDialectPostgres} for a Postgres implementation.
 * 
 * @author austermann
 * 
 */
public class PersistentLockManagerDialectSQL implements PersistentLockManagerDialect {

    protected static final String LOCK_PREFIX = "dssdfSSfgsf65kj458934zsfd__5464359gjf";

    private static final Logger logger = LoggerFactory.getLogger(PersistentLockManagerDialectSQL.class);

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    protected ReentrantReadWriteLock getRwl() {
        return rwl;
    }

    @Override
    public String acquireLock(String _lockId, String _workflowInstanceId, String _correlationId, Date _insertTS, Connection con) throws Exception {
        logger.debug("acquireLock({},{},{},{})", _lockId, _workflowInstanceId, _correlationId, _insertTS);
        rwl.readLock().lock();
        try {
            synchronized ((LOCK_PREFIX + _lockId).intern()) {
                insertOrUpdate(_lockId, _workflowInstanceId, _correlationId, _insertTS, con);
                return findNewLockOwner(_lockId, con);
            }
        } finally {
            rwl.readLock().unlock();
        }
    }

    @Override
    public String releaseLock(String _lockId, String _workflowInstanceId, Connection con) throws Exception {
        logger.debug("releaseLock({},{})", _lockId, _workflowInstanceId);
        rwl.readLock().lock();
        try {
            synchronized ((LOCK_PREFIX + _lockId).intern()) {
                PreparedStatement pstmtDeleteLock = null;
                try {
                    pstmtDeleteLock = con.prepareStatement("DELETE FROM COP_LOCK WHERE LOCK_ID=? AND WORKFLOW_INSTANCE_ID=?");
                    pstmtDeleteLock.setString(1, _lockId);
                    pstmtDeleteLock.setString(2, _workflowInstanceId);
                    pstmtDeleteLock.execute();
                    JdbcUtils.closeStatement(pstmtDeleteLock);
                    return findNewLockOwner(_lockId, con);
                } finally {
                    JdbcUtils.closeStatement(pstmtDeleteLock);
                }
            }
        } finally {
            rwl.readLock().unlock();
        }
    }

    @Override
    public boolean supportsMultipleInstances() {
        return false;
    }

    void insertOrUpdate(String _lockId, String _workflowInstanceId, String _correlationId, Date _insertTS, Connection con) throws SQLException, Exception {
        PreparedStatement pstmtInsertLock = null;
        try {
            pstmtInsertLock = con.prepareStatement("INSERT INTO COP_LOCK (LOCK_ID, CORRELATION_ID, WORKFLOW_INSTANCE_ID, INSERT_TS, REPLY_SENT) VALUES (?,?,?,?,?)");
            pstmtInsertLock.setString(1, _lockId);
            pstmtInsertLock.setString(2, _correlationId);
            pstmtInsertLock.setString(3, _workflowInstanceId);
            pstmtInsertLock.setTimestamp(4, new Timestamp(_insertTS.getTime()));
            pstmtInsertLock.setString(5, "N");
            pstmtInsertLock.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            // Oracle, DerbyDB: lock already inserted...
        } catch (SQLException e) {
            if (e.getMessage().contains("PRIMARY_KEY")) {
                // H2: lock already inserted...
            }
            else {
                throw e;
            }
        } finally {
            JdbcUtils.closeStatement(pstmtInsertLock);
        }
    }

    String findNewLockOwner(String _lockId, Connection con) throws SQLException {
        PreparedStatement pstmtSelectLock = null;
        PreparedStatement pstmtUpdateLock = null;
        try {
            pstmtSelectLock = con.prepareStatement("select l.* from cop_lock l where lock_id = ? order by l.REPLY_SENT desc, l.insert_ts, l.workflow_instance_id");
            pstmtSelectLock.setString(1, _lockId);
            ResultSet rs = pstmtSelectLock.executeQuery();
            if (rs.next()) {
                boolean replySent = "Y".equals(rs.getString("REPLY_SENT"));
                if (!replySent) {
                    final String correlationId = rs.getString("CORRELATION_ID");
                    final String wfInstanceId = rs.getString("WORKFLOW_INSTANCE_ID");
                    pstmtUpdateLock = con.prepareStatement("UPDATE COP_LOCK SET REPLY_SENT='Y' WHERE LOCK_ID=? AND WORKFLOW_INSTANCE_ID=?");
                    pstmtUpdateLock.setString(1, _lockId);
                    pstmtUpdateLock.setString(2, wfInstanceId);
                    pstmtUpdateLock.execute();
                    return correlationId;
                }
            }
            return null;
        } finally {
            JdbcUtils.closeStatement(pstmtSelectLock);
            JdbcUtils.closeStatement(pstmtUpdateLock);
        }
    }

}
