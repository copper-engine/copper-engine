package de.scoopgmbh.copper.wfrepo;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.WorkflowFactory;
import junit.framework.TestCase;

public class FileBasedWorkflowRepositoryTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(FileBasedWorkflowRepositoryTest.class);

	public void testCreateWorkflowFactory() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
		repo.setSourceDir("src/workflow/java");
		repo.setTargetDir("target/compiled_workflow");
		repo.start();
		try {
			WorkflowFactory<Object> factory = repo.createWorkflowFactory("foo");
			factory.newInstance();
			fail("expected ClassNotFoundException");
		}
		catch(ClassNotFoundException e) {
			// OK
		}
		catch(Throwable e) {
			logger.error("",e);
			fail("expected ClassNotFoundException");
		}
		finally {
			repo.shutdown();
		}
		
	}

}
