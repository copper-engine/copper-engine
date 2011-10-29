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
package de.scoopgmbh.copper.batcher;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.db.utility.RetryingTransaction;

/**
 * Abstract base implementation of the {@link BatchExecutorBase} interface.
 * 
 * @author austermann
 *
 * @param <E>
 * @param <T>
 */
public abstract class BatchExecutor<E extends BatchExecutor<E,T>, T extends BatchCommand<E,T>> implements BatchExecutorBase<E,T> {

	private static final Logger logger = Logger.getLogger(BatchExecutor.class);

	private final String id = this.getClass().getName();

	@Override
	public final void execute(final Collection<BatchCommand<E,T>> commands) {
		if (commands.isEmpty())
			return;

		try {
			final DataSource ds = commands.iterator().next().dataSource();
			if (ds == null) {
				doExec(commands, null);
			}
			else {
				new RetryingTransaction(ds) {
					@Override
					protected void execute() throws Exception {
						doExec(commands, getConnection());
					}
				}.run();
			}
			for (BatchCommand<?,?> cmd : commands) {
				cmd.callback().commandCompleted(null);
			}
		} 
		catch (Exception e) {
			if (commands.size() == 1) {
				BatchCommand<?,?> cmd = commands.iterator().next();
				cmd.callback().unhandledException(e);
			}
			else {
				logger.warn("batch execution failed - trying execution of separate commands ",e);
				for (BatchCommand<E,T> cmd : commands) {
					List<BatchCommand<E,T>> l = new ArrayList<BatchCommand<E,T>>();
					l.add(cmd);
					execute(l);
				}
			}
		}
	}	

	protected abstract void doExec(Collection<BatchCommand<E,T>> commands, Connection connection) throws Exception;

	public boolean prioritize() {
		return false;
	}

	@Override
	public String id() {
		return id;
	}

}
