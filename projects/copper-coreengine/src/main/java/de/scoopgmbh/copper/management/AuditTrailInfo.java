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
package de.scoopgmbh.copper.management;


public class AuditTrailInfo {

	long id;

	long occurrence;
	String conversationId;
	int loglevel;
	String context;
	String workflowInstanceId;
	String correlationId;
	String transactionId;
	String messageType;
	
	
	public AuditTrailInfo(
			long id,
			String transactionId,
			String conversationId,
			String correlationId, 
			long occurrence, 
			int loglevel,
			String context, 
			String workflowInstanceId, 
			String messageType) {
		super();
		this.id = id;
		this.occurrence = occurrence;
		this.conversationId = conversationId;
		this.loglevel = loglevel;
		this.context = context;
		this.workflowInstanceId = workflowInstanceId;
		this.correlationId = correlationId;
		this.transactionId = transactionId;
		this.messageType = messageType;
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getOccurrence() {
		return occurrence;
	}
	public String getConversationId() {
		return conversationId;
	}
	public int getLoglevel() {
		return loglevel;
	}
	public String getContext() {
		return context;
	}
	public String getWorkflowInstanceId() {
		return workflowInstanceId;
	}
	public String getCorrelationId() {
		return correlationId;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setOccurrence(long occurrence) {
		this.occurrence = occurrence;
	}
	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}
	public void setLoglevel(int loglevel) {
		this.loglevel = loglevel;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public void setWorkflowInstanceId(String workflowInstanceId) {
		this.workflowInstanceId = workflowInstanceId;
	}
	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
}
