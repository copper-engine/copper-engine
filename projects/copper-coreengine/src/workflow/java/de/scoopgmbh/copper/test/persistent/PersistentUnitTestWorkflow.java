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
package de.scoopgmbh.copper.test.persistent;

import java.sql.Connection;
import java.util.Date;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.AutoWire;
import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitHook;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.audit.AuditTrail;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;
import de.scoopgmbh.copper.test.DataHolder;
import de.scoopgmbh.copper.test.MockAdapter;
import de.scoopgmbh.copper.test.backchannel.BackChannelQueue;
import de.scoopgmbh.copper.test.backchannel.WorkflowResult;

public class PersistentUnitTestWorkflow extends PersistentWorkflow<String> {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(PersistentUnitTestWorkflow.class);

	private transient BackChannelQueue backChannelQueue;
	private transient MockAdapter mockAdapter;
	private transient AuditTrail auditTrail;
	private transient DataHolder dataHolder;

	@AutoWire
	public void setDataHolder(DataHolder dataHolder) {
		this.dataHolder = dataHolder;
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
			testWaitFirst();
			
			for (int i=0; i<5; i++) {
				callFoo();
				Assert.assertNotNull(this.getCreationTS());
			}
			
			callFooWithWaitHook();

			auditTrail.asynchLog(0, new Date(), "unittest", "-", this.getId(), null, null, "finished", "TEXT");
			backChannelQueue.enqueue(new WorkflowResult(getData(), null));
		}
		catch(Exception e) {
			logger.error("execution failed",e);
			backChannelQueue.enqueue(new WorkflowResult(null, e));
		}
	}

	private void callFoo() throws InterruptException {
		String cid = getEngine().createUUID();
		mockAdapter.foo(getData(), cid);
		wait(WaitMode.ALL, 10000, cid);
		Response<?> res = getAndRemoveResponse(cid);
		logger.info(res.toString());
		Assert.assertNotNull(res);
		Assert.assertFalse(res.isTimeout());
		Assert.assertEquals(getData(),res.getResponse());
		Assert.assertNull(res.getException());
		auditTrail.synchLog(0, new Date(), "unittest", "-", this.getId(), null, null, "foo successfully called", "TEXT");
	}
	
	private void callFooWithWaitHook() throws InterruptException {
		final String cid = getEngine().createUUID();
		mockAdapter.foo(getData(), cid);
		
		dataHolder.clear(cid);
		getEngine().addWaitHook(this.getId(), new WaitHook() {
			@Override
			public void onWait(Workflow<?> wf, Connection con) throws Exception {
				Assert.assertNotNull(wf);
				Assert.assertNotNull(con);
				dataHolder.put(cid,wf.getId());
			}
		});
		wait(WaitMode.ALL, 10000, cid);
		Assert.assertEquals(getId(), dataHolder.get(cid));
		dataHolder.clear(cid);

		Response<?> res = getAndRemoveResponse(cid);
		logger.info(res.toString());
		Assert.assertNotNull(res);
		Assert.assertFalse(res.isTimeout());
		Assert.assertEquals(getData(),res.getResponse());
		Assert.assertNull(res.getException());
	}
	
	private void testWaitFirst() throws InterruptException {
		final String cidEarly = getEngine().createUUID();
		final String cidLate = getEngine().createUUID();
		mockAdapter.foo(getData(), cidEarly, 50);
		
		wait(WaitMode.FIRST, 5000, cidEarly, cidLate);

		Response<?> resEarly = getAndRemoveResponse(cidEarly);
		logger.info(resEarly.toString());
		Assert.assertNotNull(resEarly);
		Assert.assertFalse(resEarly.isTimeout());
		Assert.assertEquals(getData(),resEarly.getResponse());
		Assert.assertNull(resEarly.getException());
		
		Response<?> resLate = getAndRemoveResponse(cidLate);
		Assert.assertNotNull(resLate);
		Assert.assertTrue(resLate.isTimeout());

		mockAdapter.foo(getData(), cidLate,  50);
		
		wait(WaitMode.ALL, 5000, cidLate);

		resEarly = getAndRemoveResponse(cidEarly);
		Assert.assertNull(resEarly);
		
		resLate = getAndRemoveResponse(cidLate);
		Assert.assertNotNull(resLate);
		Assert.assertFalse(resLate.isTimeout());
	}	
}
