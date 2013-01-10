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

import javax.sql.DataSource;

import de.scoopgmbh.copper.persistent.DerbyDbDialect;


public class DerbyDbSpringTxnPersistentWorkflowTest extends BaseSpringTxnPersistentWorkflowTest {

	private static final String DS_CONTEXT = "/datasources/datasource-derbydb.xml";

	private static boolean dbmsAvailable = true;

	@Override
	void cleanDB(DataSource ds) throws Exception {
		DerbyDbDialect.checkAndCreateSchema(ds);
		super.cleanDB(ds);
	}

	@Override
	protected void tearDown() throws Exception {
		DerbyDbDialect.shutdownDerby();
		super.tearDown();
	}

	public void testAsnychResponse() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testAsnychResponse(DS_CONTEXT);
	}

	public void testAsnychResponseLargeData() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testAsnychResponseLargeData(DS_CONTEXT,10000);
	}

	public void testWithConnection() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testWithConnection(DS_CONTEXT);
	}

	public void testWithConnectionBulkInsert() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testWithConnectionBulkInsert(DS_CONTEXT);
	}

	public void testTimeouts() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testTimeouts(DS_CONTEXT);
	}

	public void testErrorHandlingInCoreEngine() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testErrorHandlingInCoreEngine(DS_CONTEXT);
	}

	public void testParentChildWorkflow() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testParentChildWorkflow(DS_CONTEXT);
	}	

	public void testErrorKeepWorkflowInstanceInDB() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testErrorKeepWorkflowInstanceInDB(DS_CONTEXT);
	}

	public void testErrorHandlingInCoreEngine_restartAll() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		try {
			super.testErrorHandlingInCoreEngine_restartAll(DS_CONTEXT);
			fail();
		}
		catch(UnsupportedOperationException e) {
			// ok
		}
	}

	//	public void testCompressedAuditTrail() throws Exception {
	//		if (!dbmsAvailable) fail("DBMS not available");
	//		super.testCompressedAuditTrail(DS_CONTEXT);
	//	}

	public void testAutoCommit() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testAutoCommit(DS_CONTEXT);
	}

	public void testAuditTrailUncompressed() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testAuditTrailUncompressed(DS_CONTEXT);
	}

	public void testErrorHandlingWithWaitHook() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testErrorHandlingWithWaitHook(DS_CONTEXT);
	}

	public void testAuditTrailCustomSeqNr() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		try {
			super.testAuditTrailCustomSeqNr(DS_CONTEXT);
			fail();		}
		catch(UnsupportedOperationException e) {
			// ok
		}
	}

	public void testSpringTxnUnitTestWorkflow() throws Exception {
		if (!dbmsAvailable) fail("DBMS not available");
		super.testSpringTxnUnitTestWorkflow(DS_CONTEXT);
	}	
}
