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

/**
 * Interface for logging audit trail events to the underlying database.
 * 
 * @author austermann
 *
 */
public interface AuditTrail {

	boolean isEnabled(int level);

	/**
	 * writes an event to the audit trail log and returns after the log message is written to the underlying storage.
	 * @param logLevel the level on that the audit trail event is recorded (might be used for filtering)
	 * @param occurrence timestamp of the audit trail event 
	 * @param conversationId conversation id embraces all audit trail events for one business process (might be the same for a whole business transaction over a range of involved systems)
	 * @param context the context of the audit trail event (e.g. a camel route, a workflow task, ...)
	 * @param workflowInstanceId workflow id for a single workflow
	 * @param correlationId correlates a request response pair (e.g. workflow calls another workflow, workflow calls a camel route, ...)
	 * @param transactionId Same ID vor several conversations, that belongs to the same transaction. Example: ExecuteOrder (conversation 1), ChangeOrder (conversation 2) and CancelOrder (conversation 3) that all belongs to transaction 77. When transaction 77 can be deleted, all conversations for this transaction can be deleted.
	 * @param message a message describing the audit trail event
	 */
	public void synchLog(int logLevel, Date occurrence, String conversationId, String context, String workflowInstanceId, String correlationId, String transactionId, String message);
	
	/**
	 * returns immediately after queueing the log message
	 * @param logLevel the level on that the audit trail event is recorded (might be used for filtering)
	 * @param occurrence timestamp of the audit trail event 
	 * @param conversationId conversation id embraces all audit trail events for one business process (might be the same for a whole business transaction over a range of involved systems)
	 * @param context the context of the audit trail event (e.g. a camel route, a workflow task, ...)
	 * @param workflowInstanceId workflow id for a single workflow
	 * @param correlationId correlates a request response pair (e.g. workflow calls another workflow, workflow calls a camel route, ...)
	 * @param transactionId Same ID vor several conversations, that belongs to the same transaction. Example: ExecuteOrder (conversation 1), ChangeOrder (conversation 2) and CancelOrder (conversation 3) that all belongs to transaction 77. When transaction 77 can be deleted, all conversations for this transaction can be deleted.
	 * @param message a message describing the audit trail event
	 */
	public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String workflowInstanceId, String correlationId, String transactionId, String message);
	
	/**
	 * returns immediately after queueing the log message
	 * @param logLevel the level on that the audit trail event is recorded (might be used for filtering)
	 * @param occurrence timestamp of the audit trail event 
	 * @param conversationId conversation id embraces all audit trail events for one business process (might be the same for a whole business transaction over a range of involved systems)
	 * @param context the context of the audit trail event (e.g. a camel route, a workflow task, ...)
	 * @param workflowInstanceId workflow id for a single workflow
	 * @param correlationId correlates a request response pair (e.g. workflow calls another workflow, workflow calls a camel route, ...)
	 * @param transactionId Same ID vor several conversations, that belongs to the same transaction. Example: ExecuteOrder (conversation 1), ChangeOrder (conversation 2) and CancelOrder (conversation 3) that all belongs to transaction 77. When transaction 77 can be deleted, all conversations for this transaction can be deleted.
	 * @param message a message describing the audit trail event
	 * @param cb
	 */
	public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String workflowInstanceId, String correlationId, String transactionId, String message, AuditTrailCallback cb);

}
