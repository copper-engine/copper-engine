package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;

public class BatcherInfo implements Serializable{
	private static final long serialVersionUID = 7907179275571625622L;
	
	String name;
	int numThreads;
	
	public BatcherInfo(String name, int numThreads) {
		super();
		this.name = name;
		this.numThreads = numThreads;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumThreads() {
		return numThreads;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	public BatcherInfo() {
		super();
	}

	
	

}
