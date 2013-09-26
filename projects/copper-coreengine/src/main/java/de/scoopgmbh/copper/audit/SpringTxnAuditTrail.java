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
package de.scoopgmbh.copper.audit;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.spi.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.scoopgmbh.copper.audit.BatchInsertIntoAutoTrail.Command;
import de.scoopgmbh.copper.audit.BatchInsertIntoAutoTrail.Executor;
import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.batcher.NullCallback;
import de.scoopgmbh.copper.spring.SpringTransaction;


public class SpringTxnAuditTrail extends BatchingAuditTrail {
	
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SpringTxnAuditTrail.class);
	
	private PlatformTransactionManager transactionManager;
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Override
	public void synchLog(final AuditTrailEvent e) {
		if ( isEnabled(e.logLevel) ) {
			logger.debug("doLog({})",e);
			e.setMessage(messagePostProcessor.serialize(e.message));
			new SpringTransaction() {
				@Override
				protected void execute(Connection con) throws Exception {
					@SuppressWarnings("unchecked")
					BatchCommand<Executor, Command> cmd = createBatchCommand(e, true, NullCallback.instance);
					@SuppressWarnings("unchecked")
					Collection<BatchCommand<Executor, Command>> cmdList = Arrays.<BatchCommand<Executor, Command>>asList(cmd);
					cmd.executor().doExec(cmdList, con);
				}
			}.run(transactionManager, getDataSource(), createTransactionDefinition());
		}
	}

	protected TransactionDefinition createTransactionDefinition() {
		return new DefaultTransactionDefinition();
	}
	

}
