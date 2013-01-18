package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;

public class EngineDiscriptor implements Serializable {
	private static final long serialVersionUID = -4083220310307902745L;
	
	private EngineTyp typ;
	private String processorPoolId;
	private String processingEngineId;
	
	public static enum EngineTyp{
		TRANSIENT, PERSISTENT
	}

	public EngineDiscriptor() {
	}

	public EngineDiscriptor(EngineTyp typ, String processorPoolId, String processingEngineId) {
		super();
		this.typ = typ;
		this.processorPoolId = processorPoolId;
		this.processingEngineId = processingEngineId;
	}

	public EngineTyp getTyp() {
		return typ;
	}

	public void setTyp(EngineTyp typ) {
		this.typ = typ;
	}

	public String getProcessorPoolId() {
		return processorPoolId;
	}

	public void setProcessorPoolId(String processorPoolId) {
		this.processorPoolId = processorPoolId;
	}

	public String getProcessingEngineId() {
		return processingEngineId;
	}

	public void setProcessingEngineId(String processingEngineId) {
		this.processingEngineId = processingEngineId;
	}
	
	
}
