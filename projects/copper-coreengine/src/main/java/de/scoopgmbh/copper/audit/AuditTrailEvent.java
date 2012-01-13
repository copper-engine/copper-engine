/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.audit;

import java.util.Date;

class AuditTrailEvent {

	int logLevel;
	Date occurrence;
	String conversationId;
	String context;
	String workflowInstanceId;
	String correlationId;
	String message;
	String transactionId;
	
	public AuditTrailEvent(int logLevel, Date occurrence, String conversationId, String context, String workflowInstanceId, String correlationId, String transactionId, String message) {
		super();
		this.logLevel = logLevel;
		this.occurrence = occurrence;
		this.conversationId = conversationId;
		this.context = context;
		this.workflowInstanceId = workflowInstanceId;
		this.correlationId = correlationId;
		this.message = message;
		this.transactionId = transactionId;
	}
	
}
