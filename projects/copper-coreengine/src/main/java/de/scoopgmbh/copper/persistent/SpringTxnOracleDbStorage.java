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
package de.scoopgmbh.copper.persistent;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.spring.SpringTransaction;

public class SpringTxnOracleDbStorage extends OracleScottyDBStorage {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringTxnOracleDbStorage.class);

	private PlatformTransactionManager transactionManager;
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	private void runTransactional(final BatchCommand<?, ?> cmd) {
		new SpringTransaction() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			protected void execute(Connection con) throws Exception {
				cmd.executor().doExec((Collection)Collections.singletonList(cmd), con);
			}
		}.run(transactionManager, getDataSource(), new DefaultTransactionDefinition());
	}
	
	@Override
	public void finish(Workflow<?> w) {
		if (logger.isTraceEnabled()) logger.trace("finish("+w.getId()+")");
		final PersistentWorkflow<?> pwf = (PersistentWorkflow<?>) w;
		runTransactional(new GenericRemove.Command(pwf,isRemoveWhenFinished()));
	}

	@Override
	public void error(Workflow<?> w, Throwable t) {
		if (logger.isTraceEnabled()) logger.trace("error("+w.getId()+")");
		final PersistentWorkflow<?> pwf = (PersistentWorkflow<?>) w;
		runTransactional(new GenericSetToError.Command(pwf,t));
	}
	
	@Override
	public void registerCallback(RegisterCall rc) throws Exception {
		if (logger.isTraceEnabled()) logger.trace("registerCallback("+rc+")");
		if (rc == null) throw new NullPointerException();
		runTransactional(new GenericRegisterCallback.Command(rc, getDataSource(), getSerializer() , null));
	}
	
	@Override
	public void insert(final List<Workflow<?>> wfs) throws Exception {
		logger.trace("insert(wfs.size=?)", wfs.size());
		new SpringTransaction() {
			@Override
			protected void execute(Connection con) throws Exception {
				doInsert(wfs, con);
			}
		}.run(transactionManager, getDataSource(), new DefaultTransactionDefinition());
	}
	
	@Override
	public void insert(final Workflow<?> wf) throws Exception {
		logger.trace("insert(?)", wf);
		new SpringTransaction() {
			@Override
			protected void execute(Connection con) throws Exception {
				doInsert(wf, con);
			}
		}.run(transactionManager, getDataSource(), new DefaultTransactionDefinition());
	}
	
	@Override
	public void notify(final Response<?> response, Object callback) throws Exception {
		logger.trace("notify(?)", response);
		if (response == null) throw new NullPointerException();
		runTransactional(new GenericNotify.Command(response, getSerializer(), getDefaultStaleResponseRemovalTimeout()));
	}
	
	@Override
	public void notify(List<Response<?>> response) throws Exception {
		logger.trace("notify(response.size=?)", response.size());

		if (response.size() == 0) 
			return;
		
		final List<GenericNotify.Command> cmds = new ArrayList<GenericNotify.Command>(response.size());
		for (Response<?> r : response) {
			GenericNotify.Command cmd = new GenericNotify.Command(r, getSerializer(), getDefaultStaleResponseRemovalTimeout());
			cmds.add(cmd);
		}
		
		new SpringTransaction() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			protected void execute(Connection con) throws Exception {
				cmds.get(0).executor().doExec((Collection)cmds, con);
			}
		}.run(transactionManager, getDataSource(), new DefaultTransactionDefinition());
	}
	
	
}
