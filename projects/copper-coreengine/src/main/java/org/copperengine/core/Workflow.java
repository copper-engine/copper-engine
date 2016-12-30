/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.instrument.Transformed;
import org.copperengine.core.persistent.SavepointAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract COPPER workflow base class.
 * Workflows must inherit from this class. If your workflow requires (or maybe requires) persistence, then it has to
 * inherit from {@link org.copperengine.core.persistent.PersistentWorkflow}.
 *
 * @param <D>
 *        workflow's <code>data</code> class
 * @author austermann
 */
public abstract class Workflow<D> implements Serializable {

    private static final long serialVersionUID = -6351894157077862055L;
    private static final Logger logger = LoggerFactory.getLogger(Workflow.class);

    /**
     * Constant value for {@link Workflow#wait(WaitMode, int, Callback...)} and
     * {@link Workflow#wait(WaitMode, int, String...)} indicating
     * that there is no timeout
     */
    public static final int NO_TIMEOUT = -1;

    private transient ProcessingEngine engine;
    private transient String id = null;
    private transient Map<String, List<Response<?>>> responseMap = new HashMap<String, List<Response<?>>>();
    /**
     * for internal use only
     */
    protected Stack<StackEntry> __stack = new Stack<StackEntry>();
    /**
     * for internal use only
     */
    protected transient int __stackPosition = 0;
    private transient String processorPoolId = null;
    private transient int priority = 5;
    private transient ProcessingState processingState = ProcessingState.RAW;
    private transient D Data;
    private transient Date creationTS = new Date();
    private transient Date lastActivityTS = new Date();
    private transient Date timeoutTS;
    
    private String lastWaitStackTrace;

    /**
     * Creates a new instance
     */
    protected Workflow() {
        if (logger.isDebugEnabled())
            logger.debug("Creating new " + getClass().getName());
        if (this.getClass().getAnnotation(Transformed.class) == null) {
            throw new CopperRuntimeException(this.getClass().getName() + " has not been transformed");
        }
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        responseMap = new HashMap<String, List<Response<?>>>();
    }

    /**
     * returns the processing state
     */
    public ProcessingState getProcessingState() {
        return processingState;
    }

    void setProcessingState(ProcessingState processingState) {
        this.processingState = processingState;
        if (processingState != ProcessingState.WAITING) {
            this.timeoutTS = null;
        }
    }

    /**
     * Returns the workflow instance priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority for this workflow instance. A smaller value is a higher priority, e.g. a priority of 1 is
     * higher than 2.
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * For internal use only
     *
     * @param engine
     */
    public void setEngine(ProcessingEngine engine) {
        this.engine = engine;
    }

    public void setId(String id) {
        if (this.id != null)
            throw new IllegalStateException("id for this object has already been set");
        this.id = id;
    }

    /**
     * Returns the processing engine currently executing this workflow instance
     */
    public ProcessingEngine getEngine() {
        return engine;
    }

    /**
     * returns the id of this workflow instance. The id of a workflow instance is at least unique within one processing
     * engine.
     */
    public String getId() {
        return id;
    }

    /**
     * Creates a {@link Callback} object used for the asynchronous wait for some reponse.
     */
    protected <E> Callback<E> createCallback() {
        return new DefaultCallback<E>(engine);
    }

    /**
     * waits/sleeps until a response for every correlation id occurs
     *
     * @param correlationIds
     *        one or more correlation ids
     */
    protected final void waitForAll(String... correlationIds) throws Interrupt {
        this.wait(WaitMode.ALL, 0, correlationIds);
    }

    /**
     * waits/sleeps until a response for every callback occurs
     *
     * @param callbacks
     *        one or more callback objects
     */
    protected final void waitForAll(Callback<?>... callbacks) throws Interrupt {
        this.wait(WaitMode.ALL, 0, callbacks);
    }

    /**
     * Generic wait/sleep. In case of WaitMode FIRST, it waits until at least one response for the specified correlation
     * ids occurs.
     * In case of WaitMode ALL, it waits until a response for every specified correlation id occurs.
     *
     * @param mode
     *        WaitMode
     * @param timeoutMsec
     *        timeout in milliseconds or {@link Workflow#NO_TIMEOUT} (or any value &le; 0) to wait for ever
     * @param correlationIds
     *        one ore more correlation ids
     */
    protected final void wait(WaitMode mode, int timeoutMsec, String... correlationIds) throws Interrupt {
        if (correlationIds.length == 0)
            throw new IllegalArgumentException();
        updateLastWaitStackTrace();
        for (int i = 0; i < correlationIds.length; i++) {
            if (correlationIds[i] == null)
                throw new NullPointerException();
        }
        engine.registerCallbacks(this, mode, timeoutMsec, correlationIds);
    }

    protected final void wait(final WaitMode mode, final int timeoutMsec, final Callback<?>... callbacks) throws Interrupt {
        updateLastWaitStackTrace();
        String[] correlationIds = new String[callbacks.length];
        for (int i = 0; i < correlationIds.length; i++) {
            correlationIds[i] = callbacks[i].getCorrelationId();
        }
        engine.registerCallbacks(this, mode, timeoutMsec, correlationIds);
    }

    /**
     * Generic wait/sleep. In case of WaitMode FIRST, it waits until at least one response for the specified correlation
     * ids occurs.
     * In case of WaitMode ALL, it waits until a response for every specified correlation id occurs.
     *
     * @param mode
     *        WaitMode
     * @param timeout
     *        timeout or {@link Workflow#NO_TIMEOUT} (or any value &le; 0) to wait for ever
     * @param timeUnit
     *        unit of the timeout; ignored, if a negative timeout is specified
     * @param correlationIds
     *        one ore more correlation ids
     */
    protected final void wait(final WaitMode mode, final long timeout, final TimeUnit timeUnit, final String... correlationIds) throws Interrupt {
        if (correlationIds.length == 0)
            throw new IllegalArgumentException();
        updateLastWaitStackTrace();
        for (int i = 0; i < correlationIds.length; i++) {
            if (correlationIds[i] == null)
                throw new NullPointerException();
        }
        engine.registerCallbacks(this, mode, timeout > 0 ? timeUnit.toMillis(timeout) : 0, correlationIds);
    }

    /**
     * Generic wait/sleep. In case of WaitMode FIRST, it waits until at least one response for the specified correlation
     * ids occurs.
     * In case of WaitMode ALL, it waits until a response for every specified correlation id occurs.
     *
     * @param mode
     *        WaitMode
     * @param timeout
     *        timeout or {@link Workflow#NO_TIMEOUT} (or any value &le; 0) to wait for ever
     * @param timeUnit
     *        unit of the timeout; ignored, if a negative timeout is specified
     * @param callbacks
     *        one ore more callbacks
     */
    protected final void wait(final WaitMode mode, final long timeout, final TimeUnit timeUnit, final Callback<?>... callbacks) throws Interrupt {
        updateLastWaitStackTrace();
        String[] correlationIds = new String[callbacks.length];
        for (int i = 0; i < correlationIds.length; i++) {
            correlationIds[i] = callbacks[i].getCorrelationId();
        }
        engine.registerCallbacks(this, mode, timeout > 0 ? timeUnit.toMillis(timeout) : 0, correlationIds);
    }

    /**
     * Internal use only - called by the processing engine
     *
     * @param r
     */
    public void putResponse(Response<?> r) {
        synchronized (responseMap) {
            List<Response<?>> l = responseMap.get(r.getCorrelationId());
            if (l == null) {
                l = new SortedResponseList();
                responseMap.put(r.getCorrelationId(), l);
            }
            l.add(r);
        }
    }

    /**
     * Gets and removes a response for the specified correlation id.
     * <p>
     * <b>Attention</b>: When a workflow instance is resumed after a <code>Workflow.wait</code> call, all corresponding
     * responses for this workflow instance in the storage are read. Each of these responses that you do NOT retrieve
     * using {@link Workflow#getAndRemoveResponse(String)} or {@link Workflow#getAndRemoveResponses(String)} are
     * discarded when calling the next <code>Workflow.wait</code>
     *
     * @param correlationId
     * @return the response or null, if no response for the specified correlation id is found
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected <T> Response<T> getAndRemoveResponse(final String correlationId) {
        synchronized (responseMap) {
            final List<Response<T>> responseList = (List) responseMap.get(correlationId);
            if (responseList == null)
                return null;
            final Response<T> response = responseList.remove(responseList.size() - 1);
            if (responseList.isEmpty()) {
                responseMap.remove(correlationId);
            }
            return response;
        }
    }

    /**
     * Gets and removes all responses for the specified correlation id
     * <p>
     * <b>Attention</b>: When a workflow instance is resumed after a <code>Workflow.wait</code> call, all corresponding
     * responses for this workflow instance in the storage are read. Each of these responses that you do NOT retrieve
     * using {@link Workflow#getAndRemoveResponse(String)} or {@link Workflow#getAndRemoveResponses(String)} are
     * discarded when calling the next <code>Workflow.wait</code>
     *
     * @param correlationId
     * @return the list of responses or an empty list, if no response for the specified correlation id is found
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected <T> List<Response<T>> getAndRemoveResponses(final String correlationId) {
        synchronized (responseMap) {
            final List rv = responseMap.remove(correlationId);
            return rv == null ? Collections.emptyList() : new ArrayList(rv);
        }
    }

    /**
     * Entry point for this workflow
     */
    public abstract void main() throws Interrupt;

    /**
     * Causes the engine to stop processing of this workflow instance and to enqueue it again.
     * May be used in case of processor pool change or the create a 'savepoint'.
     *
     * @throws Interrupt
     */
    protected final void resubmit() throws Interrupt {
        final String cid = engine.createUUID();
        engine.registerCallbacks(this, WaitMode.ALL, 0, cid);
        Acknowledge ack = createCheckpointAcknowledge();
        engine.notify(new Response<Object>(cid, null, null), ack);
        registerCheckpointAcknowledge(ack);
        updateLastWaitStackTrace();
        
    }
    
    private void updateLastWaitStackTrace() {
        lastWaitStackTrace = StackTraceCreator.createStackTrace();
    }

    /**
     * Causes the engine to stop processing of this workflow instance and to enqueue it again.
     * May be used in case of processor pool change or to create a 'savepoint' in a persistent engine.
     * <p>
     * Same as {@link Workflow#resubmit()}
     *
     * @throws Interrupt
     */
    protected final void savepoint() throws Interrupt {
        resubmit();
    }

    protected final <T> void notify(Response<T> response) {
        Acknowledge ack = createCheckpointAcknowledge();
        engine.notify(response, ack);
        registerCheckpointAcknowledge(ack);
    }

    /**
     * Sets the processor pool id for this workflow instance. Changes get active at the next enqueue of this workflow
     * instance.
     * You may initiate an enqueue using {@link Workflow#resubmit()}
     *
     * @param processorPoolId
     *        id of the proccesor pool as specified in the configuration
     */
    public void setProcessorPoolId(String processorPoolId) {
        if (logger.isTraceEnabled())
            logger.trace("Setting processorPoolId to " + processorPoolId);
        this.processorPoolId = processorPoolId;
    }

    /**
     * Returns the processor pool id
     */
    public String getProcessorPoolId() {
        return processorPoolId;
    }

    /**
     * For internal usage only
     */
    public List<StackEntry> get__stack() {
        return __stack;
    }

    /**
     * Returns the data attached to this workflow instance
     */
    public D getData() {
        return this.Data;
    }

    /**
     * Sets the data for this workflow instance. Typically invoked at construction time of a workflow instance
     *
     * @param data
     */
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

    /**
     * Returns the creation timestamp for this workflow instance.
     */
    public Date getCreationTS() {
        return creationTS;
    }

    void setCreationTS(Date creationTS) {
        this.creationTS = creationTS;
    }

    protected Acknowledge createCheckpointAcknowledge() {
        return new Acknowledge.BestEffortAcknowledge();
    }

    protected void registerCheckpointAcknowledge(Acknowledge ack) {
    }

    protected void registerSavepointAware(SavepointAware sa) {
    }

    public String prettyPrintData() {
        return getData().toString();
    }
    
    public String getLastWaitStackTrace() {
        return lastWaitStackTrace;
    }
    
    public Date getLastActivityTS() {
        return lastActivityTS;
    }
    
    void setLastActivityTS(Date lastModTS) {
        this.lastActivityTS = lastModTS;
    }
    
    public Date getTimeoutTS() {
        return timeoutTS;
    }
    
    void setTimeoutTS(Date timeoutTS) {
        this.timeoutTS = timeoutTS;
    }
}
