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
package org.copperengine.regtest.test.tranzient.classhierarchy;

import java.util.concurrent.TimeUnit;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.regtest.test.MockAdapter;
import org.copperengine.core.util.BlockingResponseReceiver;

public abstract class Base extends Workflow<BlockingResponseReceiver<Integer>> {

    private static final long serialVersionUID = 1L;

    protected int counter = 0;
    protected int i;
    private String cid;
    private String cid1;
    private String cid2;
    private long startTS;

    protected MockAdapter mockAdapter;

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    protected void doubleWait() throws Interrupt {
        // test double call
        cid1 = getEngine().createUUID();
        cid2 = getEngine().createUUID();
        mockAdapter.foo("foo", cid1);
        mockAdapter.foo("foo", cid2);
        wait(WaitMode.ALL, 5000, TimeUnit.MILLISECONDS, cid1, cid2);
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
    }

    protected void simulateTimeout() throws Interrupt {
        // simulate timeout
        startTS = System.currentTimeMillis();
        wait(WaitMode.FIRST, 500, TimeUnit.MILLISECONDS, getEngine().createUUID(), getEngine().createUUID());
        assert (System.currentTimeMillis() > startTS + 490L);
    }

    protected void mockAsync() throws Interrupt {
        cid = getEngine().createUUID();
        mockAdapter.incrementSync(counter, cid);
        waitForAll(cid);
        counter = ((Integer) getAndRemoveResponse(cid).getResponse()).intValue();
    }

    protected void mockSync() throws Interrupt {
        cid = getEngine().createUUID();
        mockAdapter.incrementAsync(counter, cid);
        waitForAll(cid);
        counter = ((Integer) getAndRemoveResponse(cid).getResponse()).intValue();
    }

    protected String waitAndReturnString(String cid) throws Interrupt {
        wait(WaitMode.ALL, 50, cid);
        Response<?> r = getAndRemoveResponse(cid);
        if (!r.isTimeout())
            throw new AssertionError();
        return cid;
    }
}
