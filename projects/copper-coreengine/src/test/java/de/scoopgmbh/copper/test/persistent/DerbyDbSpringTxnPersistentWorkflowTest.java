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
package de.scoopgmbh.copper.test.persistent;

import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

import de.scoopgmbh.copper.persistent.DerbyDbDialect;


public class DerbyDbSpringTxnPersistentWorkflowTest extends BaseSpringTxnPersistentWorkflowTest {

	private static final String DS_CONTEXT = "/datasources/datasource-derbydb.xml";

	private static boolean dbmsAvailable = true;

	@Override
	void cleanDB(DataSource ds) throws Exception {
		DerbyDbDialect.checkAndCreateSchema(ds);
		super.cleanDB(ds);
	}

	@After
	public void tearDown() throws Exception {
		DerbyDbDialect.shutdownDerby();
	}

	@Test
	public void testAsnychResponse() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testAsnychResponse(DS_CONTEXT);
	}

	@Test
	public void testAsnychResponseLargeData() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testAsnychResponseLargeData(DS_CONTEXT,10000);
	}

	@Test
	public void testWithConnection() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testWithConnection(DS_CONTEXT);
	}

	@Test
	public void testWithConnectionBulkInsert() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testWithConnectionBulkInsert(DS_CONTEXT);
	}

	@Test
	public void testTimeouts() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testTimeouts(DS_CONTEXT);
	}

	@Test
	public void testErrorHandlingInCoreEngine() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testErrorHandlingInCoreEngine(DS_CONTEXT);
	}

	@Test
	public void testParentChildWorkflow() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testParentChildWorkflow(DS_CONTEXT);
	}

	@Test
	public void testErrorKeepWorkflowInstanceInDB() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testErrorKeepWorkflowInstanceInDB(DS_CONTEXT);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testErrorHandlingInCoreEngine_restartAll() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testErrorHandlingInCoreEngine_restartAll(DS_CONTEXT);
	}

	//	public void testCompressedAuditTrail() throws Exception {
	//		assertTrue("DBMS not available",dbmsAvailable);
	//		super.testCompressedAuditTrail(DS_CONTEXT);
	//	}

	@Test
	public void testAutoCommit() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testAutoCommit(DS_CONTEXT);
	}

	@Test
	public void testAuditTrailUncompressed() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testAuditTrailUncompressed(DS_CONTEXT);
	}

	@Test
	public void testErrorHandlingWithWaitHook() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testErrorHandlingWithWaitHook(DS_CONTEXT);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testAuditTrailCustomSeqNr() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testAuditTrailCustomSeqNr(DS_CONTEXT);
	}

	@Test
	public void testSpringTxnUnitTestWorkflow() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testSpringTxnUnitTestWorkflow(DS_CONTEXT);
	}	
	
	@Override
	protected void closeContext(final ConfigurableApplicationContext context) {
		try {
			Thread.sleep(100);
		} 
		catch (InterruptedException e) {
			// ignore
		}
		context.close();
	}	
	
	@Test
	public void testFailOnDuplicateInsert() throws Exception {
		assertTrue("DBMS not available",dbmsAvailable);
		super.testFailOnDuplicateInsert(DS_CONTEXT);
	}	
	
}
