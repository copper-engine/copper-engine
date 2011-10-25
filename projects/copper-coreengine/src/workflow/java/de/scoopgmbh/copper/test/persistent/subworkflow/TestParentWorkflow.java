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
package de.scoopgmbh.copper.test.persistent.subworkflow;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import de.scoopgmbh.copper.AutoWire;
import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowFactory;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;
import de.scoopgmbh.copper.test.backchannel.BackChannelQueue;
import de.scoopgmbh.copper.test.backchannel.WorkflowResult;

public class TestParentWorkflow extends PersistentWorkflow<String> {

	private static final long serialVersionUID = 1L;
	
	private transient BackChannelQueue backChannelQueue;
	
	@AutoWire
	public void setBackChannelQueue(BackChannelQueue backChannelQueue) {
		this.backChannelQueue = backChannelQueue;
	}

	@Override
	public void main() throws InterruptException {
		try {
			// create and launch the children
			List<String> cids = new ArrayList<String>();
			WorkflowFactory<String> wfFactory = getEngine().createWorkflowFactory(TestChildWorkflow.class.getName());
			for (int i=0; i<10; i++) {
				String id = getEngine().createUUID();
				Workflow<String> wf = wfFactory.newInstance();
				wf.setId(id);
				wf.setData("12345");
				getEngine().run(wf);
				cids.add(id);
			}
			wfFactory = null; // set to null, otherwise copper will try to serialize it...
			
			// wait for the children to finish
			wait(WaitMode.ALL, 10000, cids.toArray(new String[cids.size()])); 
			
			// collect the responses
			for (int i=0; i<10; i++) {
				Response<String> r = getAndRemoveResponse(cids.get(i));
				Assert.assertNotNull(r);
				Assert.assertNotNull(r.getResponse());
				Assert.assertNull(r.getException());
				Assert.assertFalse(r.isTimeout());
				Assert.assertEquals("54321", r.getResponse());
			}
			
			backChannelQueue.enqueue(new WorkflowResult(null, null));
		}
		catch(Exception e) {
			e.printStackTrace();
			backChannelQueue.enqueue(new WorkflowResult(null, e));
		}
	}

}
