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

import java.io.Serializable;

/**
 * Container for asynchronous responses.
 * 
 * @author austermann
 * @param <E>
 *        type of data held by the response for the user/workflow.
 */
public class Response<E> implements Serializable {

    private static final long serialVersionUID = 7903392981566779966L;
    private final String correlationId;
    private final E response;
    private final Exception exception;
    private final boolean timeout;
    private final String metaData;
    private final Long internalProcessingTimeout;
    private boolean earlyResponseHandling = true;
    private String responseId;
    private Long sequenceId;

    /**
     * Constructor.
     * 
     * @param correlationId
     *        the correlation ID on which a workflow is or will hopefully be waiting. This is kind of the mapping between
     *        notification from outside and the workflow inside COPPER.
     * @param response
     *        data which shall be passed to the workflow with the response when the workflow continues execution.
     * @param exception
     *        Might be set if an exception occurred. So the workflow can query the Response and will know if an error
     *        somewhere happened.
     * @param isTimeout
     *        holds information whether this response was automatically generated from COPPER as a dummy holder to
     *        notify the workflow that there was no "real" response but the specified timeout was reached
     * @param metaData
     *        Might hold some more arbitrary meta data. Usually is just null.
     * @param internalProcessingTimeout
     *        timeout in msec
     * @param responseId
     *        unique id of the response. Each response must be unique of course.
     */
    public Response(String correlationId, E response, Exception exception, boolean isTimeout, String metaData, Long internalProcessingTimeout, final String responseId) {
        super();
        if (internalProcessingTimeout != null && internalProcessingTimeout <= 0) {
            throw new IllegalArgumentException("internalProcessingTimeout must be null or > 0");
        }
        this.correlationId = correlationId;
        this.response = response;
        this.exception = exception;
        this.timeout = isTimeout;
        this.metaData = metaData;
        this.internalProcessingTimeout = internalProcessingTimeout;
        this.responseId = responseId;
    }

    /**
     * Creates a new instance.
     * @param correlationId
     *        {@link Response#Response(String, Object, Exception, boolean, String, Long, String)}
     * @param response
     *        {@link Response#Response(String, Object, Exception, boolean, String, Long, String)}
     * @param exception
     *        {@link Response#Response(String, Object, Exception, boolean, String, Long, String)}
     */
    public Response(String correlationId, E response, Exception exception) {
        this(correlationId, response, exception, false, null, null, null);
    }

    /**
     * Creates a new instance with timeout set to true.
     * @param correlationId
     *        {@link Response#Response(String, Object, Exception, boolean, String, Long, String)}
     */
    public Response(String correlationId) {
        this(correlationId, null, null, true, null, null, null);
    }

    /**
     * Return the correlation id of this response.
     * 
     * @return the correlation id of this response
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * @return the response object
     */
    public E getResponse() {
        return response;
    }

    /**
     * @return the exception or <code>null</code> if no exception occured
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @return true, if a timeout occured while waiting for the response.
     */
    public boolean isTimeout() {
        return timeout;
    }

    /**
     * @return Timeout in milliseconds
     */
    public Long getInternalProcessingTimeout() {
        return internalProcessingTimeout;
    }

    /**
     * @return the meta data of this response. Not used by the copper core itself. Applications may use this data
     * for monitoring or some custom response handling.
     */
    public String getMetaData() {
        return metaData;
    }

    public void setEarlyResponseHandling(boolean earlyResponseHandling) {
        this.earlyResponseHandling = earlyResponseHandling;
    }

    /**
     * @return
     *         if response is held in early response handling.
     *         If true, a response is queued temporarily in the 'early response container' if currently no workflow
     *         instance is (yet) waiting for the responses' correlationId.
     */
    public boolean isEarlyResponseHandling() {
        return earlyResponseHandling;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        if (this.responseId != null) {
            throw new IllegalStateException("responseId already set to value " + this.responseId);
        }
        this.responseId = responseId;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    @Override
    public String toString() {
        return "Response [correlationId=" + correlationId + ", response=" + response + ", exception=" + exception + ", timeout=" + timeout + ", metaData=" + metaData + ", internalProcessingTimeout=" + internalProcessingTimeout + ", earlyResponseHandling=" + earlyResponseHandling + ", responseId=" + responseId + ", sequenceId=" + sequenceId + "]";
    }

}
