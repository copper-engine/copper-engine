package de.scoopgmbh.copper.test.persistent;


public class OraclePersistentWorkflowTest extends PersistentWorkflowTest {
	
	private static final String DS_CONTEXT = "oracle-unittest-context.xml";
	
	public void testAsnychResponse() throws Exception {
		super.testAsnychResponse(DS_CONTEXT);
	}

	public void testAsnychResponseLargeData() throws Exception {
		super.testAsnychResponseLargeData(DS_CONTEXT);
	}

	public void testWithConnection() throws Exception {
		super.testAsnychResponse(DS_CONTEXT);
	}
	
	public void testWithConnectionBulkInsert() throws Exception {
		super.testWithConnectionBulkInsert(DS_CONTEXT);
	}
	
	public void testTimeouts() throws Exception {
		super.testTimeouts(DS_CONTEXT);
	}
	
	public void testMultipleEngines() throws Exception {
		super.testMultipleEngines(DS_CONTEXT);
	}
	
	public void testErrorHandlingInCoreEngine() throws Exception {
		super.testErrorHandlingInCoreEngine(DS_CONTEXT);
	}
	
	public void testParentChildWorkflow() throws Exception {
		super.testParentChildWorkflow(DS_CONTEXT);
	}	

}
