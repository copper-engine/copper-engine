package de.scoopgmbh.copper.persistent.adapter;

import java.lang.reflect.Method;

import de.scoopgmbh.copper.persistent.PersistentEntity;

public class AdapterCall extends PersistentEntity {

	private static final long serialVersionUID = 1L;

	String   workflowId;
	long     priority;
	
	final String   entityId;
	final String   adapterId;
	final Method   method;
	final Object[] args;
		
	public AdapterCall(String adapterId, String entityId, Method method, Object[] args) {
		this.entityId = entityId;
		this.adapterId = adapterId;
		this.method = method;
		this.args = args;
	}
	
	public void setWorkflowData(String workflowId, long priority) {
		this.workflowId = workflowId;
		this.priority = priority;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public String getEntityId() {
		return entityId;
	}

	public long getPriority() {
		return priority;
	}

	public Method getMethod() {
		return method;
	}

	public String getAdapterId() {
		return adapterId;
	}

	public Object[] getArgs() {
		return args;
	}

	@Override
	public String toString() {
		return adapterId+": "+method;
	}
		 
}
