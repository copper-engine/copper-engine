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

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.batcher.AbstractBatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;
import de.scoopgmbh.copper.batcher.CommandCallback;
import de.scoopgmbh.copper.batcher.NullCallback;
import de.scoopgmbh.copper.db.utility.RetryingTransaction;

public class BatchInsertIntoAutoTrail {
	
	static final class Command extends AbstractBatchCommand<Executor, Command>{
		
		final AuditTrailEvent data;
		final DataSource dataSource;

		@SuppressWarnings("unchecked")
		public Command(AuditTrailEvent data, DataSource dataSource) {
			super(NullCallback.instance,250);
			this.data = data;
			this.dataSource = dataSource;
		}

		public Command(AuditTrailEvent data, DataSource dataSource, CommandCallback<Command> callback) {
			super(callback,250);
			this.data = data;
			this.dataSource = dataSource;
		}

		@Override
		public Executor executor() {
			return Executor.INSTANCE;
		}

	}
	
	static final class Executor extends BatchExecutor<Executor, Command>{

		private static final Executor INSTANCE = new Executor();
		private static final Logger logger = Logger.getLogger(Executor.class);

		@Override
		public int maximumBatchSize() {
			return 100;
		}

		@Override
		public int preferredBatchSize() {
			return 50;
		}

		@Override
		protected void executeCommands(final Collection<Command> commands) {
			if (commands.isEmpty())
				return;
			
			try {
				new RetryingTransaction(commands.iterator().next().dataSource) {
					@Override
					protected void execute() throws Exception {
						final PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO COP_AUDIT_TRAIL_EVENT (SEQ_ID,OCCURRENCE,CONVERSATION_ID,LOGLEVEL,CONTEXT,WORKFLOW_INSTANCE_ID,CORRELATION_ID,MESSAGE,LONG_MESSAGE) VALUES (COP_SEQ_AUDIT_TRAIL.NEXTVAL,?,?,?,?,?,?,?,NULL)");
						for (Command cmd : commands) {
							AuditTrailEvent data = cmd.data;
							stmt.setTimestamp(1, new Timestamp(data.occurrence.getTime()));
							stmt.setString(2, data.conversationId);
							stmt.setInt(3, data.logLevel);
							stmt.setString(4, data.context);
							stmt.setString(5, data.workflowInstanceId);
							stmt.setString(6, data.correlationId);
							if ( data.message != null && data.message.length()>4000 ) { 
								stmt.setString(7, data.message.substring(0,3999));
								stmt.setString(8, data.message);
							} else {
								stmt.setString(7, data.message);
							}
							stmt.addBatch();
						}
						stmt.executeBatch();
					}
				}.run();
				for (Command cmd : commands) {
					cmd.callback().commandCompleted(cmd);
				}
			} 
			catch (Exception e) {
				logger.error("",e);
				throw new RuntimeException(e);
				// todo Einzelverarbeitung...
			}
			
		}

	}

}
