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

import java.util.Date;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.AutoWire;
import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.audit.AuditTrail;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;
import de.scoopgmbh.copper.test.DBMockAdapter;
import de.scoopgmbh.copper.test.backchannel.BackChannelQueue;
import de.scoopgmbh.copper.test.backchannel.WorkflowResult;

public class DBMockAdapterUsingPersistentUnitTestWorkflow extends PersistentWorkflow<String> {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(PersistentUnitTestWorkflow.class);

	private transient BackChannelQueue backChannelQueue;
	private transient DBMockAdapter dbMockAdapter;
	private transient AuditTrail auditTrail;

	@AutoWire
	public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
		this.backChannelQueue = backChannelQueue;
	}

	@AutoWire
	public void setDbMockAdapter(DBMockAdapter dbMockAdapter) {
		this.dbMockAdapter = dbMockAdapter;
	}
	
	@AutoWire
	public void setAuditTrail(AuditTrail auditTrail) {
		this.auditTrail = auditTrail;
	}

	@Override
	public void main() throws InterruptException {
		try {
			for (int i=0; i<10; i++) {
				callFoo();
				Assert.assertNotNull(this.getCreationTS());
			}
			auditTrail.asynchLog(0, new Date(), "unittest", "-", this.getId(), null, null, "finished");
			backChannelQueue.enqueue(new WorkflowResult(getData(), null));
		}
		catch(Exception e) {
			logger.error("execution failed",e);
			backChannelQueue.enqueue(new WorkflowResult(null, e));
		}
//		finally {
//			System.out.println("xxx");
//		}
	}

	private void callFoo() throws InterruptException {
		String cid = getEngine().createUUID();
		dbMockAdapter.foo(getData(), cid);
		wait(WaitMode.ALL, 10000, cid);
		Response<?> res = getAndRemoveResponse(cid);
		logger.info(res.toString());
		Assert.assertNotNull(res);
		Assert.assertFalse(res.isTimeout());
		Assert.assertEquals(getData(),res.getResponse());
		Assert.assertNull(res.getException());
		auditTrail.synchLog(0, new Date(), "unittest", "-", this.getId(), null, null, "foo successfully called");
	}

}
