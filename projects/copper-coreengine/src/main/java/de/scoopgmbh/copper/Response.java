/*
 * Copyright 2002-2013 SCOOP Software GmbH
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

import java.io.Serializable;

/**
 * Container for asynchronous responses.
 * 
 * @author austermann
 *
 * @param <E>
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
	
	/**
	 * Constructor.
     *
	 * @param internalProcessingTimeout timeout in msec
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
	 */
	public Response(String correlationId, E response, Exception exception) {
		this(correlationId, response, exception, false, null, null, null);
	}

	/**
	 * Creates a new instance with timeout set to true. 
	 */
	public Response(String correlationId) {
		this(correlationId, null, null, true, null, null, null);
	}
	
	/**
	 * Return the correlation id of this response.
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
	 * 
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
	 * returns the meta data of this response. Not used by the copper core itself. Applications may use this data
	 * for monitoring or some custom response handling. 
	 */
	public String getMetaData() {
		return metaData;
	}
	
	public void setEarlyResponseHandling(boolean earlyResponseHandling) {
		this.earlyResponseHandling = earlyResponseHandling;
	}
	
	/**
	 * If true, a response is queued temporarily in the 'early response container' if currently no workflow instance is (yet) waiting 
	 * for the responses' correlationId.
	 */
	public boolean isEarlyResponseHandling() {
		return earlyResponseHandling;
	}
	
	public String getResponseId() {
		return responseId;
	}
	
	public void setResponseId(String responseId) {
		if (this.responseId != null) {
			throw new IllegalStateException("responseId already set to value "+this.responseId);
		}
		this.responseId = responseId;
	}

	@Override
	public String toString() {
		return "Response [correlationId=" + correlationId + ", response="
				+ response + ", exception=" + exception + ", timeout="
				+ timeout + ", metaData=" + metaData
				+ ", internalProcessingTimeout=" + internalProcessingTimeout
				+ ", earlyResponseHandling=" + earlyResponseHandling
				+ ", responseId=" + responseId + "]";
	}
	
}
