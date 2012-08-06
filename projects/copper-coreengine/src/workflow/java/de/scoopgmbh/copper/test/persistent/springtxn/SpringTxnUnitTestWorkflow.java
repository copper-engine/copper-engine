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
package de.scoopgmbh.copper.test.persistent.springtxn;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import de.scoopgmbh.copper.AutoWire;
import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.audit.AuditTrail;
import de.scoopgmbh.copper.audit.AuditTrailEvent;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;
import de.scoopgmbh.copper.test.MockAdapter;
import de.scoopgmbh.copper.test.backchannel.BackChannelQueue;
import de.scoopgmbh.copper.test.backchannel.WorkflowResult;

public class SpringTxnUnitTestWorkflow extends PersistentWorkflow<String> {

	private static final String SQL_STMT = "INSERT INTO COP_AUDIT_TRAIL_EVENT (SEQ_ID, OCCURRENCE, CONVERSATION_ID, LOGLEVEL, CONTEXT, LONG_MESSAGE) VALUES (?, SYSTIMESTAMP, ?, 1, 'CTX', ?)";
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(SpringTxnUnitTestWorkflow.class);
	
	private static AtomicLong idFactory = new AtomicLong(-1L * System.currentTimeMillis());

	private transient BackChannelQueue backChannelQueue;
	private transient MockAdapter mockAdapter;
	private transient AuditTrail auditTrail;
	private transient JdbcTemplate jdbcTemplate;
	
	@AutoWire(beanId="jdbcTemplate4wf")
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@AutoWire
	public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
		this.backChannelQueue = backChannelQueue;
	}

	@AutoWire
	public void setMockAdapter(MockAdapter mockAdapter) {
		this.mockAdapter = mockAdapter;
	}
	
	@AutoWire
	public void setAuditTrail(AuditTrail auditTrail) {
		this.auditTrail = auditTrail;
	}

	@Override
	public void main() throws InterruptException {
		try {
			for (int i=0; i<3; i++) {
				callFoo();
				Assert.assertNotNull(this.getCreationTS());
			}
			backChannelQueue.enqueue(new WorkflowResult(getData(), null));
			backChannelQueue = null;
			throw new RuntimeException("test exception to abort execution!!!");
		}
		catch(RuntimeException e) {
			logger.error("execution failed",e);
			if (backChannelQueue != null) backChannelQueue.enqueue(new WorkflowResult(null, e));
			throw e;
		}
	}

	private void callFoo() throws InterruptException {
		String cid = getEngine().createUUID();
		// This is running within the current DB transaction
		jdbcTemplate.update(SQL_STMT, idFactory.incrementAndGet(), cid, "beforeFoo");
		
		mockAdapter.foo(getData(), cid);
		
		// current Txn end here
		wait(WaitMode.ALL, 10000, cid);
		// new Txn starts here
		
		Response<?> res = getAndRemoveResponse(cid);
		logger.info(res.toString());

		jdbcTemplate.update(SQL_STMT, idFactory.incrementAndGet(), cid, "afterFoo - result = "+res.toString());
		
		Assert.assertNotNull(res);
		Assert.assertFalse(res.isTimeout());
		Assert.assertEquals(getData(),res.getResponse());
		Assert.assertNull(res.getException());
		
		// This is also running within the current DB transaction
		auditTrail.synchLog(new AuditTrailEvent(1, new Date(), cid, "Assertions checked", getId(), cid, cid, "Assertions checked", "String", null));
	}
	
}
