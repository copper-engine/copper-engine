package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;

public class CopperStatusInfo implements Serializable{
	private static final long serialVersionUID = -7724489455243850566L;
	
	int processorPoolThreadPriority;
	int processorPoolMemoryQueueSize;
	int processorPoolNumberOfThreads;
}
