package de.scoopgmbh.copper.wfrepo;

import junit.framework.TestCase;

public class URLClassloaderClasspathProviderTest extends TestCase {

	public void testGetOptions() {
		FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
		try {
			repo.addSourceDir("src/workflow/java");
			repo.setTargetDir("target/compiled_workflow");
			repo.addCompilerOptionsProvider(new URLClassloaderClasspathProvider());
			repo.start();
		}
		finally {
			try {
				repo.shutdown();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
