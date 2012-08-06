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

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.sql.DataSource;

import de.scoopgmbh.copper.batcher.Batcher;
import de.scoopgmbh.copper.batcher.CommandCallback;
import de.scoopgmbh.copper.db.utility.RetryingTransaction;
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
	public void synchLog(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String _message, String messageType) {
		this.synchLog(new AuditTrailEvent(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, _message, messageType, null));

	}

	@Override
	public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String _message, String messageType) {
		this.asynchLog(new AuditTrailEvent(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, _message, messageType, null));
	}

	@Override
	public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String _message, String messageType, final AuditTrailCallback cb) {
		this.asynchLog(new AuditTrailEvent(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, _message, messageType, null), cb);
	}

	@Override
	public void asynchLog(AuditTrailEvent e) {
		if ( isEnabled(e.logLevel) ) {
			e.setMessage(messagePostProcessor.serialize(e.message));
			CommandCallback<BatchInsertIntoAutoTrail.Command> callback = new CommandCallback<BatchInsertIntoAutoTrail.Command>() {
				@Override
				public void commandCompleted() {
				}
				@Override
				public void unhandledException(Exception e) {
				}
			};
			batcher.submitBatchCommand(new BatchInsertIntoAutoTrail.Command(e,callback));
		}		
	}

	@Override
	public void asynchLog(final AuditTrailEvent e, final AuditTrailCallback cb) {
		if ( isEnabled(e.logLevel) ) {
			e.setMessage(messagePostProcessor.serialize(e.message));
			CommandCallback<BatchInsertIntoAutoTrail.Command> callback = new CommandCallback<BatchInsertIntoAutoTrail.Command>() {
				@Override
				public void commandCompleted() {
					cb.done();
				}
				@Override
				public void unhandledException(Exception e) {
					cb.error(e);
				}
			};
			batcher.submitBatchCommand(new BatchInsertIntoAutoTrail.Command(e,callback));
		}		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void doSyncLog(AuditTrailEvent e, Connection con) throws Exception {
		e.setMessage(messagePostProcessor.serialize(e.message));
		BatchInsertIntoAutoTrail.Command cmd = new BatchInsertIntoAutoTrail.Command(e);
		cmd.executor().doExec((Collection)Collections.singletonList(cmd), con);
	}

	@Override
	public void synchLog(final AuditTrailEvent event) {
		if (isEnabled(event.logLevel) ) {
			try {
				new RetryingTransaction(dataSource) {
					@Override
					protected void execute() throws Exception {
						doSyncLog(event, getConnection());
					}
				}.run();
			}
			catch(RuntimeException e) {
				throw e;
			}
			catch(Exception e) {
				throw new RuntimeException("synchLog failed",e);
			}
		}
	}
	
	protected DataSource getDataSource() {
		return dataSource;
	}

}
