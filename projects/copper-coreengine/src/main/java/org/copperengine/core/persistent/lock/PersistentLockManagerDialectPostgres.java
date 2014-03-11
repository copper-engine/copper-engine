package org.copperengine.core.persistent.lock;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link PersistentLockManagerDialect} for Postgres.
 * 
 * @author austermann
 * 
 */
public class PersistentLockManagerDialectPostgres extends PersistentLockManagerDialectSQL {

    private static final Logger logger = LoggerFactory.getLogger(PersistentLockManagerDialectPostgres.class);

    @Override
    public String acquireLock(String _lockId, String _workflowInstanceId, String _correlationId, Date _insertTS, Connection con) throws Exception {
        logger.debug("acquireLock({},{},{},{})", _lockId, _workflowInstanceId, _correlationId, _insertTS);
        getRwl().readLock().lock();
        try {
            synchronized ((LOCK_PREFIX + _lockId).intern()) {
                Savepoint sp = con.setSavepoint();
                try {
                    insertOrUpdate(_lockId, _workflowInstanceId, _correlationId, _insertTS, con);
                } catch (SQLException e) {
                    if (e.getMessage().toUpperCase().contains("COP_LOCK_PKEY")) {
                        con.rollback(sp);
                    }
                    else {
                        throw e;
                    }
                }
                return findNewLockOwner(_lockId, con);
            }
        } finally {
            getRwl().readLock().unlock();
        }
    }
}
