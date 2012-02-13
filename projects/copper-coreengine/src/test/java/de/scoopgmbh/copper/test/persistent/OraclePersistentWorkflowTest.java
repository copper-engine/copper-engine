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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class OraclePersistentWorkflowTest extends PersistentWorkflowTest {
	
	private static final String DS_CONTEXT = "oracle-unittest-context.xml";
	
	private static boolean dbmsAvailable = false;
	
	static {
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {DS_CONTEXT, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		try {
			DataSource ds = context.getBean(DataSource.class);
			ds.setLoginTimeout(10);
			ds.getConnection();
			dbmsAvailable = true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			context.close();
		}
	}	
	
	public void testAsnychResponse() throws Exception {
		if (dbmsAvailable) super.testAsnychResponse(DS_CONTEXT);
	}

	public void testAsnychResponseLargeData() throws Exception {
		if (dbmsAvailable) super.testAsnychResponseLargeData(DS_CONTEXT,65536);
	}

	public void testWithConnection() throws Exception {
		if (dbmsAvailable) super.testAsnychResponse(DS_CONTEXT);
	}
	
	public void testWithConnectionBulkInsert() throws Exception {
		if (dbmsAvailable) super.testWithConnectionBulkInsert(DS_CONTEXT);
	}
	
	public void testTimeouts() throws Exception {
		if (dbmsAvailable) super.testTimeouts(DS_CONTEXT);
	}
	
	public void testMultipleEngines() throws Exception {
		if (dbmsAvailable) super.testMultipleEngines(DS_CONTEXT);
	}
	
	public void testErrorHandlingInCoreEngine() throws Exception {
		if (dbmsAvailable) super.testErrorHandlingInCoreEngine(DS_CONTEXT);
	}
	
	public void testParentChildWorkflow() throws Exception {
		if (dbmsAvailable) super.testParentChildWorkflow(DS_CONTEXT);
	}	

	public void testErrorKeepWorkflowInstanceInDB() throws Exception {
		if (dbmsAvailable) super.testErrorKeepWorkflowInstanceInDB(DS_CONTEXT);
	}
	
	public void testErrorHandlingInCoreEngine_restartAll() throws Exception {
		if (dbmsAvailable) super.testErrorHandlingInCoreEngine_restartAll(DS_CONTEXT);
	}
	
	public void testCompressedAuditTrail() throws Exception {
		if (dbmsAvailable) super.testCompressedAuditTrail(DS_CONTEXT);
	}
	
}
