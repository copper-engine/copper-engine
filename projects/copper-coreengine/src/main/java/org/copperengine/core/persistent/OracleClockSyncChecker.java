package org.copperengine.core.persistent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleClockSyncChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(OracleClockSyncChecker.class);
    
    private final DataSource dataSource;
    private int allowedDeltaMSec = 100;
    private int checkIntervalSeconds = 60;
    
    public OracleClockSyncChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setAllowedDeltaMSec(int allowedDeltaMSec) {
        if (allowedDeltaMSec <= 0) 
            throw new IllegalArgumentException();
        this.allowedDeltaMSec = allowedDeltaMSec;
    }
    
    public void setCheckIntervalSeconds(int checkIntervalSeconds) {
        if (checkIntervalSeconds <= 0) 
            throw new IllegalArgumentException();
        this.checkIntervalSeconds = checkIntervalSeconds;
    }
    
    Date readDatabaseClock(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("SELECT SYSTIMESTAMP FROM DUAL")) {
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getTimestamp(1);
        }
    }
    
    void checkClocksAreSynchronized(Connection con) {
        try {
            final Date appServerTS = new Date();
            final long now = System.nanoTime();
            final Date dbServerTS = readDatabaseClock(con);
            final long etMSec = (System.nanoTime() - now) / 1000000L;
            if (Math.abs(appServerTS.getTime() - dbServerTS.getTime()) > (etMSec+allowedDeltaMSec)) {
                logger.warn("ATTENTION! App server and DB server clocks are not in sync: {} {}", appServerTS, dbServerTS);
            }
        }
        catch(Exception e) {
            logger.error("checkClocksAreSynchronized failed", e);
        }
        
    }
}
