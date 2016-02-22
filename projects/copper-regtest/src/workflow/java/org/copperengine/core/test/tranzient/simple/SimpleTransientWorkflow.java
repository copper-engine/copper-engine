/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.core.test.tranzient.simple;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.test.MockAdapter;
import org.copperengine.core.test.TestResponseReceiver;

public class SimpleTransientWorkflow extends Workflow<String> {

    private static final long serialVersionUID = 7325419989364229211L;

    private int counter = 0;

    private MockAdapter mockAdapter;
    private TestResponseReceiver<String, Integer> rr;

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @AutoWire(beanId = "OutputChannel4711")
    public void setResponseReceiver(TestResponseReceiver<String, Integer> rr) {
        this.rr = rr;
    }

    private void reply() {
        if (rr != null)
            rr.setResponse(this, counter);
    }

    public class Innerclass {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.copperengine.core.Workflow#main()
     */
    @Override
    public void main() throws Interrupt {
        new Innerclass();

        try {
            if (counter != 0) {

            } else {
                final String rv = execute(getEngine().createUUID(), 1000);
                System.out.println(rv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < 5; i++) {

                try {
                    String cid = getEngine().createUUID();
                    mockAdapter.incrementAsync(counter, cid);
                    waitForAll(cid);
                    counter = ((Integer) getAndRemoveResponse(cid).getResponse()).intValue();

                    resubmit(); // just for fun...
                    savepoint();

                    cid = getEngine().createUUID();
                    mockAdapter.incrementSync(counter, cid);
                    waitForAll(cid);
                    counter = ((Integer) getAndRemoveResponse(cid).getResponse()).intValue();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }

            // simulate timeout
            final long startTS = System.currentTimeMillis();
            wait(WaitMode.FIRST, 500, getEngine().createUUID(), getEngine().createUUID());
            if (System.currentTimeMillis() < startTS + 490L)
                throw new AssertionError();

            // test double call
            final String cid1 = getEngine().createUUID();
            final String cid2 = getEngine().createUUID();
            mockAdapter.foo("foo", cid1);
            mockAdapter.foo("foo", cid2);
            wait4all(cid1, cid2);
            Response<String> x1 = getAndRemoveResponse(cid1);
            Response<String> x2 = getAndRemoveResponse(cid2);
            if (x1 == null)
                throw new AssertionError();
            if (x2 == null)
                throw new AssertionError();
            if (!x1.getCorrelationId().equals(cid1))
                throw new AssertionError();
            if (!x2.getCorrelationId().equals(cid2))
                throw new AssertionError();
            if (getAndRemoveResponse(cid1) != null)
                throw new AssertionError();
            if (getAndRemoveResponse(cid2) != null)
                throw new AssertionError();
            if (!x1.getResponse().equals("foo"))
                throw new AssertionError();
            if (!x2.getResponse().equals("foo"))
                throw new AssertionError();

            testMultiResponse();

            reply();
        } catch (Exception e) {
            e.printStackTrace();
            fail("should never come here");
        } finally {
            System.out.println("finally");
        }
    }

    private void wait4all(final String cid1, final String cid2) throws Interrupt {
        try {
            wait(WaitMode.ALL, 5000, cid1, cid2);
        } catch (Exception e) {
            e.printStackTrace();
            fail("should never come here");
        } finally {
            System.out.println("finally");
        }
    }

    private String execute(String cid, int timeout) throws Interrupt {
        mockAdapter.foo("foo", cid);
        wait(WaitMode.ALL, timeout, cid);
        Response<String> response = getAndRemoveResponse(cid);
        if (response == null)
            throw new AssertionError();
        if (!response.getCorrelationId().equals(cid))
            throw new AssertionError();
        if (getAndRemoveResponse(cid) != null)
            throw new AssertionError();
        if (!response.getResponse().equals("foo"))
            throw new AssertionError();
        return cid;
    }

    private void testMultiResponse() throws Interrupt {
        final int SIZE = 5;
        final String cid1 = getEngine().createUUID();
        mockAdapter.fooWithMultiResponse("foo", cid1, SIZE);
        List<Response<Object>> list1 = new ArrayList<Response<Object>>();
        for (int i = 0; i < SIZE; i++) {
            wait(WaitMode.ALL, 500, cid1);
            List<Response<Object>> r1 = getAndRemoveResponses(cid1);
            System.out.println(r1.size());
            for (Response<Object> r : r1) {
                if (r.isTimeout()) {
                    throw new AssertionError("Unexpected timeout");
                }
            }
            list1.addAll(r1);
            if (list1.size() == SIZE) {
                break;
            }
        }
        if (list1.size() != SIZE) {
            throw new AssertionError("Expected size 3 but is " + list1.size());
        }
    }

}
