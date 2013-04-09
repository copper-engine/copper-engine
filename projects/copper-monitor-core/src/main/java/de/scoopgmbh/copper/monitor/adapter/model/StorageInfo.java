package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;

public class StorageInfo implements Serializable{
	private static final long serialVersionUID = 8298167440433882270L;
	
	String name;
	BatcherInfo batcher;

	public StorageInfo(String name,BatcherInfo batcher) {
		super();
		this.name = name;
		this.batcher = batcher;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BatcherInfo getBatcher() {
		return batcher;
	}

	public void setBatcher(BatcherInfo batcher) {
		this.batcher = batcher;
	}

	public StorageInfo() {
		super();
		batcher= new BatcherInfo();
	}
	
	
	
	

}
