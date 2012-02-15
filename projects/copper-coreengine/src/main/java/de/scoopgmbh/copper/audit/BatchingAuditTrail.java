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

import javax.sql.DataSource;

import de.scoopgmbh.copper.audit.BatchInsertIntoAutoTrail.Command;
import de.scoopgmbh.copper.batcher.Batcher;
import de.scoopgmbh.copper.batcher.CommandCallback;
import de.scoopgmbh.copper.management.AuditTrailMXBean;

/**
 * Fast db based audit trail implementation.
 * 
 * @author austermann
 *
 */
public class BatchingAuditTrail implements AuditTrail, AuditTrailMXBean {
	
	private Batcher batcher;
	private DataSource dataSource;
	private int level = 5;
	private MessagePostProcessor messagePostProcessor = new DummyPostProcessor();
	
	public void setMessagePostProcessor(MessagePostProcessor messagePostProcessor) {
		this.messagePostProcessor = messagePostProcessor;
	}
	
	public void setBatcher(Batcher batcher) {
		this.batcher = batcher;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setLevel (int level) {
		this.level = level;
	}

	@Override
	public int getLevel() {
		return level;
	}
	
	@Override
	public boolean isEnabled (int level) {
		return this.level >= level;
	}

	@Override
	public void synchLog(int logLevel, Date occurrence, String conversationId, String context, String workflowInstanceId, String correlationId, String transactionId, String _message) {
		if ( isEnabled(logLevel) ) {
			final String message = messagePostProcessor.serialize(_message);
			final Object mutex = new Object();
			final Exception[] exc = new Exception[1];
			final boolean[] done = { false };
			CommandCallback<BatchInsertIntoAutoTrail.Command> callback = new CommandCallback<BatchInsertIntoAutoTrail.Command>() {
				@Override
				public void commandCompleted(Command cmd) {
					synchronized (mutex) {
						done[0] = true;
						mutex.notify();
					}
				}
				@Override
				public void unhandledException(Exception e) {
					synchronized (mutex) {
						done[0] = true;
						exc[0] = e;
						mutex.notify();
					}
				}
			};
			batcher.submitBatchCommand(new BatchInsertIntoAutoTrail.Command(new AuditTrailEvent(logLevel, occurrence, conversationId, context, workflowInstanceId, correlationId, transactionId, message),dataSource, callback));
			synchronized (mutex) {
				while (!done[0]) {
					try {
						mutex.wait(1000L);
					} 
					catch (InterruptedException e) {
						throw new RuntimeException("wait interrupted",e);
					}
				}
			}
		}
	}

	@Override
	public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String workflowInstanceId, String correlationId, String transactionId, String _message) {
		if ( isEnabled(logLevel) ) {
			final String message = messagePostProcessor.serialize(_message);
			batcher.submitBatchCommand(new BatchInsertIntoAutoTrail.Command(new AuditTrailEvent(logLevel, occurrence, conversationId, context, workflowInstanceId, correlationId, transactionId, message),dataSource));
		}
	}

	@Override
	public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String workflowInstanceId, String correlationId, String transactionId, String _message, final AuditTrailCallback cb) {
		if ( isEnabled(logLevel) ) {
			final String message = messagePostProcessor.serialize(_message);
			CommandCallback<BatchInsertIntoAutoTrail.Command> callback = new CommandCallback<BatchInsertIntoAutoTrail.Command>() {
				@Override
				public void commandCompleted(Command cmd) {
					cb.done();
				}
				@Override
				public void unhandledException(Exception e) {
					cb.error(e);
				}
			};
			batcher.submitBatchCommand(new BatchInsertIntoAutoTrail.Command(new AuditTrailEvent(logLevel, occurrence, conversationId, context, workflowInstanceId, correlationId, transactionId, message),dataSource,callback));
		}
	}

}
