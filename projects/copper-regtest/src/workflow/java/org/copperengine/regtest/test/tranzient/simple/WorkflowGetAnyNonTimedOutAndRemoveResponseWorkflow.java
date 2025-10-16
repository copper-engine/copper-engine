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
package org.copperengine.regtest.test.tranzient.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.regtest.test.MockAdapter;
import org.copperengine.core.util.AsyncResponseReceiver;

public class WorkflowGetAnyNonTimedOutAndRemoveResponseWorkflow extends Workflow<AsyncResponseReceiver<Integer>> {

    private static final long serialVersionUID = 1L;

    private MockAdapter mockAdapter;

    @AutoWire
    public void setMockAdapter(MockAdapter mockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    @Override
    public void main() throws Interrupt {

        // mockAdapter.foo() will notify the engine with a Response<String>
        // mockAdapter.incrementAsync will notify the engine with a Response<Integer>

        //////
        // Test getAnyNonTimedOutAndRemoveResponse for  a simple wait call with different CIDs.
        {
            final String cidString = mockAdapter.foo("testParam");
            final String cidInt = getEngine().createUUID();
            mockAdapter.incrementAsync(5, cidInt);
            wait(WaitMode.ALL, 300, TimeUnit.MILLISECONDS, cidInt, cidString);
            Response<Integer> respInt = getAnyNonTimedOutAndRemoveResponse(Integer.class);
            assertNotEquals(null, respInt);
            assertEquals(6, respInt.getResponse().intValue());
            assertEquals(true, !respInt.isTimeout());
            Response<String> respString = getAnyNonTimedOutAndRemoveResponse(String.class);
            assertNotEquals(null, respString);
            assertEquals(false, respString.isTimeout());
            assertEquals(true, getAndRemoveResponses(cidInt).isEmpty()); // The responses were properly removed. An empty list is returned.
            assertEquals(true, getAndRemoveResponses(cidString).isEmpty());
        }


        //////
        // Test send multiple responses to the same correlation id and pick any out of it.
        {
            final int numResponses = 5;
            final String cidMulti = "multiCID";
            mockAdapter.fooWithMultiResponse("testMulti", cidMulti, numResponses);
            wait(WaitMode.ALL, 300, TimeUnit.MILLISECONDS, cidMulti, "emptyCID"); //Make sure to run in timeout and thus get all responses to the same cid.
            assertEquals(true, getAndRemoveResponse("emptyCID").isTimeout());
            Response<String> respMulti = getAnyNonTimedOutAndRemoveResponse(String.class);
            assertNotEquals(null, respMulti);
            assertEquals(numResponses - 1, getAndRemoveResponses(cidMulti).size());
        }

        //////
        // Test getAnyNonTimedOutAndRemoveResponse for only long running tasks, so timed out responses
        {
            final int numTests = 3;
            String[] cids = new String[numTests];
            for (int i=0; i<numTests; i++) {
                cids[i] = getEngine().createUUID();
                mockAdapter.foo("long", cids[i], 1000);
            }
            wait(WaitMode.ALL, 300, TimeUnit.MILLISECONDS, cids);
            Response<String> resp = getAnyNonTimedOutAndRemoveResponse(String.class);
            assertEquals(null, resp); // All are timed out.
            int numRestResponses = 0;
            for (int i=0; i<numTests; i++) {
                Response<String> nextResp = getAndRemoveResponse(cids[i]);
                if(nextResp != null) {
                    numRestResponses++;
                }
            }
            assertEquals(numTests, numRestResponses); // They all came but are timed out.
        }


        //////
        // Test getAnyNonTimedOutAndRemoveResponse with string responses. one should be timed out, so the other shall be returned.
        {
            final String cidFast = getEngine().createUUID();
            final String cidSlow = getEngine().createUUID();
            final String fastParam = "fast";
            mockAdapter.foo(fastParam, cidFast, 1);
            mockAdapter.foo("slow", cidSlow, 1000);
            wait(WaitMode.FIRST, 300, TimeUnit.MILLISECONDS, cidFast, cidSlow);
            Response<String> resp = getAnyNonTimedOutAndRemoveResponse(String.class);
            assertEquals(false, resp.isTimeout());
            assertEquals(fastParam, resp.getResponse());
            // And try to grep any other response
            Response<Object> resp2 = getAnyNonTimedOutAndRemoveResponse(Object.class);
            assertEquals(null, resp2); // Should be timed out.
        }

        getData().setResponse(Integer.valueOf(1));

    }
}
