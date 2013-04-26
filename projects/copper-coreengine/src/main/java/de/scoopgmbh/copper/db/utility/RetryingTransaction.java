/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.db.utility;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple transaction implementation for COPPER applications. Retrying means that the user's implementation of
 * execute() will automatically be retried in case of technical failures. 
 * 
 * @author austermann
 *
 */
public abstract class RetryingTransaction<R> implements Transaction<R>  {

    private static final Logger logger = LoggerFactory.getLogger(RetryingTransaction.class);
    private static final ThreadLocal<RetryingTransaction<?>> currentTransaction = new ThreadLocal<RetryingTransaction<?>>();
    private static SQLExceptionProcessor defaultSQLExceptionProcessor = new MockSQLExceptionProcessor();
    

    public static RetryingTransaction<?> getCurrent() {
        return currentTransaction.get();
    }

    public static void setDefaultSQLExceptionProcessor(SQLExceptionProcessor defaultSQLExceptionProcessor) {
        if (defaultSQLExceptionProcessor == null) throw new NullPointerException();
        RetryingTransaction.defaultSQLExceptionProcessor = defaultSQLExceptionProcessor;
    }
    
    private final String name;
    private final boolean modificatory;
    private final DataSource ds;
    private Connection connection;
    private int maxConnectRetries = Integer.MAX_VALUE;
    private SQLExceptionProcessor sqlExceptionProcessor;
    
    public RetryingTransaction(String name, DataSource ds) {
        this(name, ds ,true);
    }

    public RetryingTransaction(DataSource ds) {
        this("anonym", ds ,true);
    }

    public RetryingTransaction(String name, DataSource ds, boolean modificatory) {
        if (name == null) throw new NullPointerException();
        if (ds == null) throw new NullPointerException();
        this.name = name;
        this.modificatory = modificatory;
        this.ds = ds;
        this.sqlExceptionProcessor = defaultSQLExceptionProcessor;
    }

    public void setSqlExceptionProcessor(SQLExceptionProcessor sqlExceptionProcessor) {
        if (sqlExceptionProcessor == null) throw new NullPointerException();
        this.sqlExceptionProcessor = sqlExceptionProcessor;
    }
    
    /**
     * This function is to be implemented by anonymous inner classes. Usage
     * should look like this: <code>...
     * new RetryingTransaction<ReturnType>("TestTransaction") {
     *       protected ReturnType execute() {
     *             doSomething();
     *             doAnotherThing();
     *             return ...;
     *       }
     *    }.run();
     *    </code>
     */
    protected abstract R execute() throws Exception;

    public Connection getConnection() {
        return connection;
    }

    /**
     * This function implements error handling (i.e. transaction rollbacks in
     * case of exceptions etc.), and automatic retries in case of deadlocks or
     * timeouts.
     */
    public final R run() throws Exception {
        if (getCurrent() != null) {
            if (logger.isDebugEnabled()) logger.debug("Starting new inner transaction "+name);
            connection = getCurrent().connection;
            try {
                return execute();
            }
            finally {
                connection = null;
                if (logger.isDebugEnabled()) logger.debug("Finished inner transaction "+name);
            }
        }
        else {
            try {
                if (logger.isDebugEnabled()) logger.debug("Starting new transaction "+name);
                R result = null;
                currentTransaction.set(this);
                connection = aquireConnection(false);
                for(int seqNr=1;;seqNr++) {
                    try {
                        result = execute();
                        if (modificatory) { 
                            logger.debug("Trying to commit");
                            connection.commit();
                            logger.debug("Transaction {} commited", name);
                        }
                        else {
                        	logger.debug("Txn is read only - rolling back");
                        	connection.rollback();
                            logger.debug("Transaction {} rolled back", name);
                        }
                        break;
                    }
                    catch(SQLException e) {
                        if (logger.isDebugEnabled()) logger.debug("Transaction "+name+" will be rolled back due to SQLException: "+e.toString(),e);
                        try {
                        	connection.rollback();
                        }
                        catch(SQLException e2) {
                        	logger.warn("Rollback failed:"+e2.toString(),e2);
                        }
                        final RetryAction ra = check4retry(e, seqNr);
                        if (logger.isDebugEnabled()) logger.debug("RetryAction="+ra);
                        if (ra == RetryAction.noRetry) {
                            throw e;
                        }
                        else if (ra == RetryAction.retryWithNewConnection) {
                            logger.warn("Transaction "+name+" will be retried with new connection due to SQLException "+e.toString());
                            try {
                            	connection.close();
                            }
                            catch(SQLException e2) {
                            	logger.warn("close failed:"+e2.toString(),e2);
                            }
                            connection = aquireConnection(true);
                        }
                        else {
                            logger.error("Unexpected RetryAction "+ra);
                            assert false : "Unexpected RetryAction "+ra;
                        }
                    }
                    catch(Exception e) {
                        if (logger.isDebugEnabled()) logger.debug("Transaction "+name+" will be rolled back due to Exception: "+e.toString(),e);
                        try {
                        	connection.rollback();
                        }
                        catch(SQLException e2) {
                        	logger.warn("Rollback failed:"+e2.toString(),e2);
                        }
                        throw e;
                    }
                }
                return result;
            }
            finally {
                currentTransaction.remove();
                if (connection != null) { 
                    connection.close();
                }
                if (logger.isDebugEnabled()) logger.debug("Finished transaction "+name);
            }
        }
    }
    
    protected RetryAction check4retry(SQLException e, int seqNr) {
        if (!sqlExceptionProcessor.retryPossible(e)) {
            return RetryAction.noRetry;
        }
        
        if (seqNr == 1) {
            return RetryAction.retryWithNewConnection;
        }
        if (seqNr == 2) {
            return RetryAction.retryWithNewConnection;
        }
        return RetryAction.noRetry;
    }

    protected Connection aquireConnection(boolean validateNewConnection) throws SQLException {
        int counter = 0;
        for (;;) {
            try {
                Connection c = ds.getConnection();
                c.setAutoCommit(false);
                if (validateNewConnection) {
                	c.rollback();
                }
                c.setAutoCommit(false);
                return c;
            }
            catch(SQLException e) {
                if (counter == maxConnectRetries) {
                    throw e;
                }
                logger.error("Unable to get connection: "+e.toString());
                logger.error("Retrying...");
            }
            ++counter;
        }
    }
    
    public int getMaxConnectRetries() {
        return maxConnectRetries;
    }
    
    public Transaction<R> setMaxConnectRetries(int maxConnectRetries) {
        if (maxConnectRetries < 0) {
            throw new IllegalArgumentException();
        }
        this.maxConnectRetries = maxConnectRetries;
        return this;
    }
}
