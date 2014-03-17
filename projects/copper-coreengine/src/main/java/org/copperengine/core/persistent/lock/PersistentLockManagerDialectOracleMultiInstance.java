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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import org.copperengine.core.db.utility.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link PersistentLockManagerDialect} that supports multiple (distributed) instances, working
 * on one single Oracle database.
 * 
 * @author austermann
 * 
 */
public class PersistentLockManagerDialectOracleMultiInstance implements PersistentLockManagerDialect {

    private static final Logger logger = LoggerFactory.getLogger(PersistentLockManagerDialectOracleMultiInstance.class);

    @Override
    public String acquireLock(String _lockId, String _workflowInstanceId, String _correlationId, Date _insertTS, Connection con) throws Exception {
        logger.debug("acquireLock({},{},{},{},{},{},{})", _lockId, _workflowInstanceId, _correlationId, _insertTS);
        CallableStatement stmt = con.prepareCall("BEGIN COP_COREENGINE.acquire_lock(?,?,?,?,?,?); END;");
        try {
            stmt.setString(1, _lockId);
            stmt.setString(2, _correlationId);
            stmt.setString(3, _workflowInstanceId);
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.registerOutParameter(5, Types.NUMERIC);
            stmt.registerOutParameter(6, Types.VARCHAR);
            stmt.execute();
            final String oCorrelationId = stmt.getString(4);
            final int oResultCode = stmt.getInt(5);
            final String oResultMsg = stmt.getString(6);
            if (oResultCode != 0) {
                throw new SQLException("acquireLock failed: " + oResultMsg);
            }
            return oCorrelationId;
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    @Override
    public String releaseLock(String _lockId, String _workflowInstanceId, Connection con) throws Exception {
        logger.debug("releaseLock({},{})", _lockId, _workflowInstanceId);
        CallableStatement stmt = con.prepareCall("BEGIN COP_COREENGINE.release_lock(?,?,?,?,?); END;");
        try {
            stmt.setString(1, _lockId);
            stmt.setString(2, _workflowInstanceId);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.registerOutParameter(4, Types.NUMERIC);
            stmt.registerOutParameter(5, Types.VARCHAR);
            stmt.execute();
            final String oCorrelationId = stmt.getString(3);
            final int oResultCode = stmt.getInt(4);
            final String oResultMsg = stmt.getString(5);
            if (oResultCode != 0) {
                throw new SQLException("releaseLock failed: " + oResultMsg);
            }
            return oCorrelationId;
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
    }

    @Override
    public boolean supportsMultipleInstances() {
        return true;
    }

}
