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
import java.util.ListIterator;
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
     * @return the processing state
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
     * @return the workflow instance priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority for this workflow instance. A smaller value is a higher priority, e.g. a priority of 1 is
     * higher than 2.
     * @param priority
     *        new priority (Takes effect after next checkpoint (wait/resubmit/savepoint) for prioritizing waiting..
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * For internal use only
     *
     * @param engine engine which (currently) owns this workflow. [For a distributed setup it might happen that
     *               another engine takes over the workflow for computation. But the workflow can always call
     *               {@link #getEngine} and receives the current owning engine.]
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
     * @return the processing engine currently executing this workflow instance
     */
    public ProcessingEngine getEngine() {
        return engine;
    }

    /**
     * @return the id of this workflow instance. The id of a workflow instance is at least unique within one processing
     * engine.
     */
    public String getId() {
        return id;
    }

    /**
     * Creates a {@link Callback} object used for the asynchronous wait for some reponse.
     * @param <E>
     *         type of response for the new callback
     * @return the created callback
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
    protected final void waitForAll(String... correlationIds) {
        this.wait(WaitMode.ALL, 0, correlationIds);
    }

    /**
     * waits/sleeps until a response for every callback occurs
     *
     * @param callbacks
     *        one or more callback objects
     */
    protected final void waitForAll(Callback<?>... callbacks) {
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
    protected final void wait(WaitMode mode, int timeoutMsec, String... correlationIds) {
        if (correlationIds.length == 0)
            throw new IllegalArgumentException();
        updateLastWaitStackTrace();
        for (int i = 0; i < correlationIds.length; i++) {
            if (correlationIds[i] == null)
                throw new NullPointerException();
        }
        engine.registerCallbacks(this, mode, timeoutMsec, correlationIds);
    }

    protected final void wait(final WaitMode mode, final int timeoutMsec, final Callback<?>... callbacks) {
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
    protected final void wait(final WaitMode mode, final long timeout, final TimeUnit timeUnit, final String... correlationIds) {
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
    protected final void wait(final WaitMode mode, final long timeout, final TimeUnit timeUnit, final Callback<?>... callbacks) {
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
     *        response to be put into the response map.
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
     *        correlation id for which response shall be get
     * @param <T>
     *         type of data holt in the response
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
     *        correlation id for which responses shall be get
     * @param <T>
     *         type of data holt in the response
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
     * This method is similar to getAndRemoveResponse but a convenient extension for some usage scenarios.
     * Sometimes, we don't bother which correlation ID is notificated upon but just want one of the
     * notifications. (e.g. we send the same request to multiple services and are happy if one of those answers.)
     * We could also run wait in a timed out loop and handle always those responses only which already got answered (for the next
     * loop iteration we could remove the to be waited correlation ids from the wait list by querying the correlation id via response.getCorrelationID).)
     * In the method, we look up the list of responses and return any of those which didn't run into a timeout (and with the per parameter given type).
     * If multiple correlations didn't run into a timeout, a random one of those is returned and removed.
     * If all given correlation IDs with the given type ran into the timeout, null is returned! This is due to the
     * fact, that we can't find the type of an empty response on runtime and thus not filter accordingly.
     * So if null is returned, you know that the workflow was waken up by timeout. (If all correlation IDs were of the given type)
     * Responses with different types than the specified one are just ignored.
     * Call it like
     * <pre>
     *     Response&lt;MyResponseType&gt; resp = getAnyNonTimedOutAndRemoveResponse(MyResponseType.class);
     * </pre>
     * To get any non timed out response, you can just call the method with Object.class as parameter.
     * @param type
     *         type class for which responses shall be get. (e.g. for getting only Response&lt;String&gt;). Might as well
     *         be {@code Object.class} to get valid Responses with all types of data.
     * @return the response or null, if no fitting response is found
     * @param <T>
     *         type of data holt in the response
     */
    @SuppressWarnings({ "unchecked" })
    protected <T> Response<T> getAnyNonTimedOutAndRemoveResponse(Class<? extends T> type) {
        synchronized (responseMap) {
            for(Map.Entry<String, List<Response<?> > > correlationResponse : responseMap.entrySet()) {
                assert(correlationResponse.getValue() != null);
                ListIterator<Response<?> > li = correlationResponse.getValue().listIterator(correlationResponse.getValue().size());
                while(li.hasPrevious()) {
                    Response<?> prev = li.previous();
                    if (prev.getResponse() == null)
                        continue;
                    // Runtime check type
                    try {
                        type.cast(prev.getResponse());
                        li.remove();
                        if (correlationResponse.getValue().isEmpty()) {
                            responseMap.remove(correlationResponse.getKey());
                        }
                        return (Response<T>) prev;
                    } catch (Exception e) {
                        //Do nothing, just wrong type so we skip.
                    }
                }
            }
            // No valid response found, only timed out ones (or none at all).
            return null;
        }
    }



    /**
     * Entry point for this workflow
     * @throws Interrupt
     *        for internal use of COPPER. When main is called, an Interrupt may be thrown, caught by COPPER itself for
     *        taking back control over the executed workflow.
     *        An application developer MUST NOT throw an Interrupt at any time in Workflow class code for
     *        COPPER to work properly.
     */
    public abstract void main() throws Interrupt;

    /**
     * Causes the engine to stop processing of this workflow instance and to enqueue it again.
     * May be used in case of processor pool change or to create a 'savepoint'.
     *
     */
    protected final void resubmit() {
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
     */
    protected final void savepoint() {
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
     * @return the processor pool id
     */
    public String getProcessorPoolId() {
        return processorPoolId;
    }

    /**
     * For internal usage only
     * @return the internally used stack representing the call stack altogether with used local variables.
     */
    public List<StackEntry> get__stack() {
        return __stack;
    }

    /**
     * @return the data attached to this workflow instance
     */
    public D getData() {
        return this.Data;
    }

    /**
     * Sets the data for this workflow instance. Typically invoked at construction time of a workflow instance
     *
     * @param data the new data object for this workflow
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
     * @return the creation timestamp for this workflow instance.
     */
    public Date getCreationTS() {
        return creationTS;
    }

    void setCreationTS(Date creationTS) {
        this.creationTS = creationTS;
    }

    /**
     * We create a quasi empty acknowledge here. It doesn't block at all and has empty callbacks for success and errors.
     * @return a BestEffortAcknowledge
     */
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
