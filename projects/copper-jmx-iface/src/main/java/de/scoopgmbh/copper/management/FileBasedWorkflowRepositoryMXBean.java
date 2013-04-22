package de.scoopgmbh.copper.management;

import java.util.List;

public interface FileBasedWorkflowRepositoryMXBean extends WorkflowRepositoryMXBean {
	
	/**
	 * Returns the configured local directory/directories
	 */	
	public List<String> getSourceDirs();
}
