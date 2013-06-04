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
package de.scoopgmbh.copper.test.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
			//testWaitAllMultiResponseAndTimeout();
			
			testWaitFirstMultiResponse();

			testWaitFirst();

			for (int i=0; i<5; i++) {
				callFoo();
				assertNotNull(this.getCreationTS());
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
		mockAdapter.fooWithMultiResponse(getData(), cid, 3);
		List<Response<Object>> responseList = new ArrayList<Response<Object>>();
		long waitUntil = System.currentTimeMillis()+20000;
		while (responseList.size() < 3 && System.currentTimeMillis() < waitUntil) {
			wait(WaitMode.ALL, 10000, cid);
			List<Response<Object>> tmpResponses = getAndRemoveResponses(cid); 
			assertNotNull(tmpResponses);
			responseList.addAll(tmpResponses);
		}
		assertEquals(3, responseList.size());
		for (Response<Object> res : responseList) {
			logger.info(res.toString());
			assertNotNull(res);
			assertFalse(res.isTimeout());
			assertEquals(getData(), res.getResponse());
			assertNull(res.getException());
		}
		auditTrail.synchLog(0, new Date(), "unittest", "-", this.getId(), null, null, "foo successfully called", "TEXT");
	}

	private void callFooWithWaitHook() throws InterruptException {
		final String cid = getEngine().createUUID();
		mockAdapter.foo(getData(), cid);

		dataHolder.clear(cid);
		getEngine().addWaitHook(this.getId(), new WaitHook() {
			@Override
			public void onWait(Workflow<?> wf, Connection con) throws Exception {
				assertNotNull(wf);
				assertNotNull(con);
				dataHolder.put(cid,wf.getId());
			}
		});
		wait(WaitMode.ALL, 10000, cid);
		assertEquals(getId(), dataHolder.get(cid));
		dataHolder.clear(cid);

		Response<?> res = getAndRemoveResponse(cid);
		logger.info(res.toString());
		assertNotNull(res);
		assertFalse(res.isTimeout());
		assertEquals(getData(),res.getResponse());
		assertNull(res.getException());
	}

	private void testWaitFirst() throws InterruptException {
		final String cidEarly = getEngine().createUUID();
		final String cidLate = getEngine().createUUID();
		mockAdapter.foo(getData(), cidEarly, 50);

		wait(WaitMode.FIRST, 5000, cidEarly, cidLate);

		Response<?> resEarly = getAndRemoveResponse(cidEarly);
		logger.info(resEarly.toString());
		assertNotNull(resEarly);
		assertFalse(resEarly.isTimeout());
		assertEquals(getData(),resEarly.getResponse());
		assertNull(resEarly.getException());

		Response<?> resLate = getAndRemoveResponse(cidLate);
		assertNull(resLate);

		mockAdapter.foo(getData(), cidLate,  50);

		wait(WaitMode.ALL, 5000, cidLate);

		resEarly = getAndRemoveResponse(cidEarly);
		assertNull(resEarly);

		resLate = getAndRemoveResponse(cidLate);
		assertNotNull(resLate);
		assertFalse(resLate.isTimeout());
	}	
	
	private void testWaitFirstMultiResponse() throws InterruptException {
		final String cidWithResponse = getEngine().createUUID();
		final String cidNoResponse = getEngine().createUUID();
		mockAdapter.fooWithMultiResponse(getData(), cidWithResponse, 2);

		wait(WaitMode.FIRST, 10000, cidWithResponse, cidNoResponse);

		Response<?> response = getAndRemoveResponse(cidWithResponse);
		assertNotNull(response);
		assertFalse(response.isTimeout());
		assertEquals(getData(),response.getResponse());
		assertNull(response.getException());
		
		response = getAndRemoveResponse(cidWithResponse);
		assertNotNull(response);
		assertFalse(response.isTimeout());
		assertEquals(getData(),response.getResponse());
		assertNull(response.getException());
		
		response = getAndRemoveResponse(cidWithResponse);
		assertNull(response);

		Response<?> timedOutResponse = getAndRemoveResponse(cidNoResponse);
		assertNull(timedOutResponse);
	}	

	private void testWaitAllMultiResponseAndTimeout() throws InterruptException {
		final String cidWithResponse = getEngine().createUUID();
		final String cidNoResponse = getEngine().createUUID();
		mockAdapter.fooWithMultiResponse(getData(), cidWithResponse, 2);

		wait(WaitMode.ALL, 200, cidWithResponse, cidNoResponse);

		Response<?> response = getAndRemoveResponse(cidWithResponse);
		assertNotNull(response);
		assertFalse(response.isTimeout());
		assertEquals(getData(),response.getResponse());
		assertNull(response.getException());
		
		response = getAndRemoveResponse(cidWithResponse);
		assertNotNull(response);
		assertFalse(response.isTimeout());
		assertEquals(getData(),response.getResponse());
		assertNull(response.getException());
		
		response = getAndRemoveResponse(cidWithResponse);
		assertNull(response);

		Response<?> timedOutResponse = getAndRemoveResponse(cidNoResponse);
		assertNotNull(timedOutResponse);
		assertTrue(timedOutResponse.isTimeout());
		assertNull(timedOutResponse.getResponse());
		assertNull(timedOutResponse.getException());
	}	
}
