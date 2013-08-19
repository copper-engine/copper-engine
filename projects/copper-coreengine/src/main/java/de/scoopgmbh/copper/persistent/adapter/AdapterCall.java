package de.scoopgmbh.copper.persistent.adapter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

import de.scoopgmbh.copper.persistent.PersistentEntity;
import de.scoopgmbh.copper.persistent.alpha.generator.PersisterFactoryGenerator;
import de.scoopgmbh.copper.persistent.alpha.generator.PersisterFactoryGenerator.GenerationDescription;
import de.scoopgmbh.copper.persistent.alpha.generator.PersisterFactoryGenerator.PersistentMember;

public class AdapterCall extends PersistentEntity {

	private static final long serialVersionUID = 1L;

	final String   workflowId;
	final String   entityId;
	final String   adapterId;
	final int      priority;
	final Method   method;
	final Object[] args;
		
	public AdapterCall(String workflowId, String adapterId, String entityId, Method method, Object[] args, int priority) {
		this.workflowId = workflowId;
		this.entityId = entityId;
		this.adapterId = adapterId;
		this.method = method;
		this.args = args;
		this.priority = priority;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public String getEntityId() {
		return entityId;
	}

	public int getPriority() {
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

	public static void main(String[] args) throws SecurityException, NoSuchMethodException, UnsupportedEncodingException, IOException {
		PersisterFactoryGenerator gen = new PersisterFactoryGenerator();
		GenerationDescription desc = new GenerationDescription(AdapterCall.class.getSimpleName(), AdapterCall.class.getPackage().getName(), "scribble");
		desc.getPersistentMembers().add(PersistentMember.fromProperty(AdapterCall.class, "adapterId"));
		desc.getPersistentMembers().add(PersistentMember.fromProperty(AdapterCall.class, "priority"));
		desc.getPersistentMembers().add(PersistentMember.fromProperty(AdapterCall.class, "methodDeclaringClass"));
		desc.getPersistentMembers().add(PersistentMember.fromProperty(AdapterCall.class, "methodName"));
		desc.getPersistentMembers().add(PersistentMember.fromProperty(AdapterCall.class, "methodSignature"));
		desc.getPersistentMembers().add(PersistentMember.fromProperty(AdapterCall.class, "args"));
		gen.generatePersisterFactory(desc, new OutputStreamWriter(System.out));
	}
	
		 
}
