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
package org.copperengine.core.test.persistent;

import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

public class MySqlSpringTxnPersistentWorkflowTest extends BaseSpringTxnPersistentWorkflowTest {

    private static final String DS_CONTEXT = "/datasources/datasource-mysql.xml";
    private static final Logger logger = LoggerFactory.getLogger(MySqlPersistentWorkflowTest.class);

    private static boolean dbmsAvailable = false;

    static {
        if (Boolean.getBoolean(Constants.SKIP_EXTERNAL_DB_TESTS_KEY)) {
            dbmsAvailable = true;
        } else {
            final ConfigurableApplicationContext context = new MySqlSpringTxnPersistentWorkflowTest().createContext(DS_CONTEXT);
            try {
                DataSource ds = context.getBean(DataSource.class);
                ds.setLoginTimeout(10);
                ds.getConnection();
                dbmsAvailable = true;
            } catch (Exception e) {
                logger.error("Oracle MySql not available! Skipping MySql unit tests.", e);
                e.printStackTrace();
            } finally {
                context.close();
            }
        }
    }

    @Override
    protected boolean skipTests() {
        return Boolean.getBoolean(Constants.SKIP_EXTERNAL_DB_TESTS_KEY);
    }

    @Test
    public void testAsynchResponse() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testAsynchResponse(DS_CONTEXT);
    }

    @Test
    public void testAsynchResponseLargeData() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testAsynchResponseLargeData(DS_CONTEXT, 65536);
    }

    @Test
    public void testWithConnection() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testWithConnection(DS_CONTEXT);
    }

    @Test
    public void testWithConnectionBulkInsert() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testWithConnectionBulkInsert(DS_CONTEXT);
    }

    @Test
    public void testTimeouts() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testTimeouts(DS_CONTEXT);
    }

    @Test
    public void testErrorHandlingInCoreEngine() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testErrorHandlingInCoreEngine(DS_CONTEXT);
    }

    @Test
    public void testParentChildWorkflow() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testParentChildWorkflow(DS_CONTEXT);
    }

    @Test
    public void testErrorKeepWorkflowInstanceInDB() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testErrorKeepWorkflowInstanceInDB(DS_CONTEXT);
    }

    @Test
    public void testAuditTrailUncompressed() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testAuditTrailUncompressed(DS_CONTEXT);
    }

    @Test
    public void testErrorHandlingWithWaitHook() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testErrorHandlingWithWaitHook(DS_CONTEXT);
    }

    @Test
    public void testSpringTxnUnitTestWorkflow() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testSpringTxnUnitTestWorkflow(DS_CONTEXT);
    }

    @Test
    public void testAutoCommit() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testAutoCommit(DS_CONTEXT);
    }

    @Test
    public void testFailOnDuplicateInsert() throws Exception {
        assertTrue("DBMS not available", dbmsAvailable);
        super.testFailOnDuplicateInsert(DS_CONTEXT);
    }
}
