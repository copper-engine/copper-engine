/*
 * Copyright 2002-2011 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.instrument.Transformed;

/**
 * Abstract COPPER workflow base class.  
 * Workflows must inherit from this class. If your workflow requires (or maybe requires) persistence, then it has to
 * inherit from {@link de.scoopgmbh.copper.persistent.PersistentWorkflow}.
 * 
 * @author austermann
 *
 * @param <D> workflow's <code>data</code> class
 */
public abstract class Workflow<D> implements Serializable {
	
	private static final long serialVersionUID = -6351894157077862055L;

	private static final Logger logger = Logger.getLogger(Workflow.class);
	
	private transient ProcessingEngine engine;
	private transient String id = null;
	private transient Map<String, Response<?>> responseMap = new HashMap<String, Response<?>>();
	protected Stack<StackEntry> __stack = new Stack<StackEntry>();
	protected transient int __stackPosition = 0; 
	private transient String processorPoolId = null;
	private transient int priority = 5;
	private D Data;
	
	protected Workflow() {
		if (logger.isDebugEnabled()) logger.debug("Creating new "+getClass().getName());
		if (this.getClass().getAnnotation(Transformed.class) == null) {
			throw new CopperRuntimeException(this.getClass().getName()+" has not been transformed");
		}
	}
	
	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		responseMap = new HashMap<String, Response<?>>();
	}	
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public void setEngine(ProcessingEngine engine) {
		this.engine = engine;
	}
	
	public void setId(String id) {
		if (this.id != null) throw new IllegalStateException("id for this object has already been set");
		this.id = id;
	}
	
	public ProcessingEngine getEngine() {
		return engine;
	}
	
	public String getId() {
		return id;
	}
	
	protected <E> Callback<E> createCallback() {
		return new DefaultCallback<E>(engine);
	}
	
	protected final void waitForAll(String... correlationIds) throws InterruptException {
		this.wait(WaitMode.ALL,0,correlationIds);
	}
	
	protected final void waitForAll(Callback<?>... callbacks) throws InterruptException {
		this.wait(WaitMode.ALL,0,callbacks);
	}
	
	protected final void wait(WaitMode mode, int timeoutMsec, String... correlationIds) throws InterruptException {
		engine.registerCallbacks(this, mode, timeoutMsec, correlationIds);
	}
	
	protected final void wait(final WaitMode mode, final int timeoutMsec, final Callback<?>... callbacks) throws InterruptException {
		String[] correlationIds = new String[callbacks.length];
		for (int i=0; i<correlationIds.length; i++) {
			correlationIds[i] = callbacks[i].getCorrelationId();
		}
		engine.registerCallbacks(this, mode, timeoutMsec, correlationIds);
	}

	public void putResponse(Response<?> r) {
		synchronized (responseMap) {
			responseMap.put(r.getCorrelationId(), r);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> Response<T> getAndRemoveResponse(String correlationId) {
		synchronized (responseMap) {
			return (Response<T>) responseMap.remove(correlationId);
		}
	}
	
	public abstract void main() throws InterruptException;
	
	protected final void resubmit() throws InterruptException {
		final String cid = engine.createUUID();
		engine.registerCallbacks(this, WaitMode.ALL, 0, cid);
		engine.notify(new Response<Object>(cid,null,null));
	}
	
	public void setProcessorPoolId(String processorPoolId) {
		if (logger.isTraceEnabled()) logger.trace("Setting processorPoolId to "+processorPoolId);
		this.processorPoolId = processorPoolId;
	}
	
	public String getProcessorPoolId() {
		return processorPoolId;
	}
	
	public List<StackEntry> get__stack() {
		return __stack;
	}
	
	public D getData() {
		return this.Data;
	}
	
	public void setData(D data) {
		this.Data = data;
	}

	@Override
	public String toString() {
		return "Workflow [id=" + id + ", priority=" + priority + ", processorPoolId=" + processorPoolId + "]";
	}
	
	public void __beforeProcess() {
		__stackPosition = 0;
	}
	
	
}
