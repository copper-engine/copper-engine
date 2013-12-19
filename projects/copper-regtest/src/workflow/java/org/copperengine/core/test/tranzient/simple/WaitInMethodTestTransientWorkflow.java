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
package org.copperengine.core.test.tranzient.simple;

import org.copperengine.core.AutoWire;
import org.copperengine.core.InterruptException;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.test.MockAdapter;
import org.copperengine.core.util.AsyncResponseReceiver;

public class WaitInMethodTestTransientWorkflow extends Workflow<AsyncResponseReceiver<Integer>> {

    private static final long serialVersionUID = -9191480374225984819L;

    private MockAdapter mockAdapter;
    private int response = 1;

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @Override
    public void main() throws InterruptException {
        getData();

        Exception exception = null;
        try {
            execute();
        } catch (Exception e) {
            e.printStackTrace();
            response = 0;
            exception = e;
        }
        System.out.println("finished");
        getData().setResponse(Integer.valueOf(response));

        Response<Object> response = new Response<Object>("4711", null, exception);
        notify(response);

    }

    private void execute() throws InterruptException {
        System.out.println("start of execute()");
        final String cid = getEngine().createUUID();
        mockAdapter.foo("foo", cid);
        wait(WaitMode.ALL, 1000, cid);
        Response<String> response = getAndRemoveResponse(cid);
        if (response == null)
            throw new AssertionError();
        if (!response.getCorrelationId().equals(cid))
            throw new AssertionError();
        if (getAndRemoveResponse(cid) != null)
            throw new AssertionError();
        if (!response.getResponse().equals("foo"))
            throw new AssertionError();
        System.out.println("end of execute()");
    }
}
