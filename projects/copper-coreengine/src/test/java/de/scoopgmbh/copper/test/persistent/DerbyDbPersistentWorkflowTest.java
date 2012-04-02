/*
 * Copyright 2002-2012 SCOOP Software GmbH
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

import de.scoopgmbh.copper.persistent.DerbyDbScottyDbStorage;


public class DerbyDbPersistentWorkflowTest extends PersistentWorkflowTest {
	
	private static final String DS_CONTEXT = "derbydb-unittest-context.xml";
	
	@Override
	void cleanDB(DataSource ds) throws Exception {
		DerbyDbScottyDbStorage.checkAndCreateSchema(ds);
		super.cleanDB(ds);
	}

	@Override
	protected void tearDown() throws Exception {
		DerbyDbScottyDbStorage.shutdownDerby();
		super.tearDown();
	}
	
	public void testAsnychResponse() throws Exception {
		super.testAsnychResponse(DS_CONTEXT);
	}

	public void testAsnychResponseLargeData() throws Exception {
		super.testAsnychResponseLargeData(DS_CONTEXT, 10000);
	}

	public void testWithConnection() throws Exception {
		super.testWithConnection(DS_CONTEXT);
	}
	
	public void testWithConnectionBulkInsert() throws Exception {
		super.testWithConnectionBulkInsert(DS_CONTEXT);
	}
	
	public void testTimeouts() throws Exception {
		super.testTimeouts(DS_CONTEXT);
	}
	
	public void testErrorHandlingInCoreEngine() throws Exception {
		super.testErrorHandlingInCoreEngine(DS_CONTEXT);
	}
	
	public void testParentChildWorkflow() throws Exception {
		super.testParentChildWorkflow(DS_CONTEXT);
	}	

	public void testErrorKeepWorkflowInstanceInDB() throws Exception {
		super.testErrorKeepWorkflowInstanceInDB(DS_CONTEXT);
	}
	
//	public void testCompressedAuditTrail() throws Exception {
//		super.testCompressedAuditTrail(DS_CONTEXT);
//	}
	
	public void testAutoCommit() throws Exception {
		super.testAutoCommit(DS_CONTEXT);
	}	
	
}
