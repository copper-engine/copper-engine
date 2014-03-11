package org.copperengine.core.persistent.lock;

import java.sql.Connection;
import java.util.Date;

public interface PersistentLockManagerDialect {

    public String acquireLock(String lockId, String workflowInstanceId, String correlationId, Date insertTS, Connection con) throws Exception;

    public String releaseLock(String lockId, String workflowInstanceId, Connection con) throws Exception;

    public boolean supportsMultipleInstances();

}
