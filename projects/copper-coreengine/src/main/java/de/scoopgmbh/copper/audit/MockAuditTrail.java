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
package de.scoopgmbh.copper.audit;

import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Mock implementation of an audit trail.
 * 
 * @author austermann
 *
 */
public class MockAuditTrail implements AuditTrail {

	private static final Logger logger = Logger.getLogger(MockAuditTrail.class);

	private int level = 5;
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	@Override
	public boolean isEnabled (int level) {
		return  this.level >= level;
	}
	
	@Override
	public void synchLog(int logLevel, Date occurrence, String conversationId,String context, String workflowInstanceId, String correlationId,String message) {
		if ( isEnabled(logLevel) ) logger.info(createMessage(logLevel, occurrence, conversationId, context, workflowInstanceId, correlationId, message));
	}

	@Override
	public void asynchLog(int logLevel, Date occurrence, String conversationId,String context, String workflowInstanceId, String correlationId,String message) {
		if ( isEnabled(logLevel) ) logger.info(createMessage(logLevel, occurrence, conversationId, context, workflowInstanceId, correlationId, message));
	}

	@Override
	public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String workflowInstanceId, String correlationId, String message, AuditTrailCallback cb) {
		if ( isEnabled(logLevel) ) logger.info(createMessage(logLevel, occurrence, conversationId, context, workflowInstanceId, correlationId, message));
		cb.done();
	}

	private String createMessage(int logLevel, Date occurrence, String conversationId, String context, String workflowInstanceId, String correlationId, String message) {
		return new StringBuilder()
			.append(logLevel).append('|')
			.append(occurrence).append('|')
			.append(conversationId).append('|')
			.append(context).append('|')
			.append(workflowInstanceId).append('|')
			.append(correlationId).append('|')
			.append(message)
			.toString();
	}

}
