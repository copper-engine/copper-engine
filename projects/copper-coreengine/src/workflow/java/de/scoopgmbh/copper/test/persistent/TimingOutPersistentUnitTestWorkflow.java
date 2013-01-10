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

import java.io.Serializable;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.AutoWire;
import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;
import de.scoopgmbh.copper.test.backchannel.BackChannelQueue;
import de.scoopgmbh.copper.test.backchannel.WorkflowResult;

public class TimingOutPersistentUnitTestWorkflow extends PersistentWorkflow<Serializable> {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(PersistentUnitTestWorkflow.class);

	private transient BackChannelQueue backChannelQueue;


	@AutoWire
	public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
		this.backChannelQueue = backChannelQueue;
	}

	@Override
	public void main() throws InterruptException {
		try {
			String cid = getEngine().createUUID();
			wait(WaitMode.ALL, 500, cid);
			try {
				Response<?> res = getAndRemoveResponse(cid);
				logger.info(res.toString());
				Assert.assertNotNull(res);
				Assert.assertTrue(res.isTimeout());
				Assert.assertNull(res.getResponse());
				Assert.assertNull(res.getException());
			}
			catch(RuntimeException e) {
				logger.error("just for testing - runtime exception caught",e);
				throw e;
			}
			backChannelQueue.enqueue(new WorkflowResult(null, null));
		}
		catch(Exception e) {
			logger.error("execution failed",e);
			backChannelQueue.enqueue(new WorkflowResult(null, e));
		}
	}

}
