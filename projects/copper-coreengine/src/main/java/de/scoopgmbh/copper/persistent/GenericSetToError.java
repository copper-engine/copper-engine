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
package de.scoopgmbh.copper.persistent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.batcher.AbstractBatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;
import de.scoopgmbh.copper.batcher.NullCallback;
import de.scoopgmbh.copper.db.utility.RetryingTransaction;

class GenericSetToError {

	private static final Logger logger = Logger.getLogger(GenericSetToError.class);

	static final class Command extends AbstractBatchCommand<Executor, Command> {

		private final DataSource dataSource;
		private final PersistentWorkflow<?> wf;
		private final Throwable error;

		@SuppressWarnings("unchecked")
		public Command(PersistentWorkflow<?> wf, DataSource dataSource, Throwable error) {
			super(NullCallback.instance,250);
			this.dataSource = dataSource;
			this.wf = wf;
			this.error = error;
		}

		@Override
		public Executor executor() {
			return Executor.INSTANCE;
		}

	}

	static final class Executor extends BatchExecutor<Executor, Command> {

		private static final Executor INSTANCE = new Executor();

		@Override
		protected void executeCommands(final Collection<Command> commands) {
			if (commands.isEmpty())
				return;
			
			
			
			try {
				new RetryingTransaction(commands.iterator().next().dataSource) {
					@Override
					protected void execute() throws Exception {
						final PreparedStatement stmtUpdateState = getConnection().prepareStatement("UPDATE COP_WORKFLOW_INSTANCE SET STATE=? WHERE ID=?");
						final PreparedStatement stmtInsertError = getConnection().prepareStatement("INSERT INTO COP_WORKFLOW_INSTANCE_ERROR (WORKFLOW_INSTANCE_ID, EXCEPTION, ERROR_TS) VALUES (?,?,SYSTIMESTAMP)");
						for (Command cmd : commands) {
							stmtUpdateState.setInt(1, DBProcessingState.ERROR.ordinal());
							stmtUpdateState.setString(2, cmd.wf.getId());
							stmtUpdateState.addBatch();

							stmtInsertError.setString(1, cmd.wf.getId());
							stmtInsertError.setString(2, convert2String(cmd.error));
							stmtInsertError.addBatch();
						}
						stmtUpdateState.executeBatch();
						stmtInsertError.executeBatch();
					}
				}.run();
			} 
			catch (Exception e) {
				logger.error("",e);
				throw new RuntimeException(e);
				// todo Einzelverarbeitung...
			}
		}

		@Override
		public int maximumBatchSize() {
			return 100;
		}

		@Override
		public int preferredBatchSize() {
			return 50;
		}

	}
	
	private static final String convert2String(Throwable t)  {
		StringWriter sw = new StringWriter(2048);
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}
