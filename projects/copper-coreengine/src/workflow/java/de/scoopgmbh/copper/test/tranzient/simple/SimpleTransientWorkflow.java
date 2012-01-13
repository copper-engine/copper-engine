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
package de.scoopgmbh.copper.test.tranzient.simple;

import junit.framework.Assert;
import de.scoopgmbh.copper.AutoWire;
import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.test.MockAdapter;
import de.scoopgmbh.copper.test.TestResponseReceiver;

public class SimpleTransientWorkflow extends Workflow<String> {

	private static final long serialVersionUID = 7325419989364229211L;
	
	private int counter = 0;

	private MockAdapter mockAdapter;
	private TestResponseReceiver<String, Integer> rr;

	@AutoWire
	public void setMockAdapter(MockAdapter mockAdapter) {
		this.mockAdapter = mockAdapter;
	}

	@AutoWire(beanId="OutputChannel4711")
	public void setResponseReceiver(TestResponseReceiver<String, Integer> rr) {
		this.rr = rr;
	}

	private void reply() {
		if (rr != null) rr.setResponse(this, counter);
	}
	
	public class Innerclass {
		
	}

	/* (non-Javadoc)
	 * @see de.scoopgmbh.copper.Workflow#main()
	 */
	@Override
	public void main() throws InterruptException {
		new Innerclass();
		
		try {
			execute();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
		try {
			for (int i=0; i<5; i++) {

				try {
					String cid = getEngine().createUUID();
					mockAdapter.incrementAsync(counter, cid);
					waitForAll(cid);
					counter = ((Integer)getAndRemoveResponse(cid).getResponse()).intValue();

					resubmit(); // just for fun...

					cid = getEngine().createUUID();
					mockAdapter.incrementSync(counter, cid);
					waitForAll(cid);
					counter = ((Integer)getAndRemoveResponse(cid).getResponse()).intValue();
				}
				catch(Exception e) {
					e.printStackTrace();
					throw e;
				}
			}

			// simulate timeout
			final long startTS = System.currentTimeMillis();
			wait(WaitMode.FIRST, 500, getEngine().createUUID(), getEngine().createUUID());
			if (System.currentTimeMillis() < startTS+490L) throw new AssertionError();

			// test double call
			final String cid1 = getEngine().createUUID();
			final String cid2 = getEngine().createUUID();
			mockAdapter.foo("foo", cid1);
			mockAdapter.foo("foo", cid2);
			wait4all(cid1, cid2);
			Response<String> x1 = getAndRemoveResponse(cid1);
			Response<String> x2 = getAndRemoveResponse(cid2);
			if (x1 == null) throw new AssertionError();
			if (x2 == null) throw new AssertionError();
			if (!x1.getCorrelationId().equals(cid1)) throw new AssertionError();
			if (!x2.getCorrelationId().equals(cid2)) throw new AssertionError();
			if (getAndRemoveResponse(cid1) != null) throw new AssertionError();
			if (getAndRemoveResponse(cid2) != null) throw new AssertionError();
			if (!x1.getResponse().equals("foo")) throw new AssertionError();
			if (!x2.getResponse().equals("foo")) throw new AssertionError();
			

			reply();
		}
		catch(Exception e) {
			e.printStackTrace();
			Assert.fail("should never come here");
		}
		finally {
			System.out.println("finally");
		}
	}

	private void wait4all(final String cid1, final String cid2) throws InterruptException {
		try {
			wait(WaitMode.ALL, 5000, cid1, cid2);
		}
		catch(Exception e) {
			e.printStackTrace();
			Assert.fail("should never come here");
		}
		finally {
			System.out.println("finally");
		}
	}

	
	private void execute() throws InterruptException {
		System.out.println("start of execute()");
		final String cid = getEngine().createUUID();
		mockAdapter.foo("foo", cid);
		wait(WaitMode.ALL, 1000, cid);
		Response<String> response = getAndRemoveResponse(cid);
		if (response == null) throw new AssertionError();
		if (!response.getCorrelationId().equals(cid)) throw new AssertionError();
		if (getAndRemoveResponse(cid) != null) throw new AssertionError();
		if (!response.getResponse().equals("foo")) throw new AssertionError();
		System.out.println("end of execute()");
	}

}
