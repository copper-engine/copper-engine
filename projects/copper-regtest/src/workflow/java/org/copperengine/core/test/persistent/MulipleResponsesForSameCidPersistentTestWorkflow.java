/**
 * Copyright 2002-2017 SCOOP Software GmbH
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
package org.copperengine.core.test.persistent;

import java.util.List;

import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.WorkflowDescription;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WorkflowDescription(alias = "MulipleResponsesForSameCidPersistentTestWorkflow", majorVersion = 1, minorVersion = 0, patchLevelVersion = 0)
public class MulipleResponsesForSameCidPersistentTestWorkflow extends PersistentWorkflow<String>
{

    private static final long serialVersionUID = -5414921076132557210L;
    private static final Logger logger = LoggerFactory.getLogger(MulipleResponsesForSameCidPersistentTestWorkflow.class);

    @Override
    public final void main() throws Interrupt
    {
        String lastResponse = "Response#0";
        String cid = getData();
        logger.info("WF started, id=" + getId() + ", cid=" + cid);
        boolean done = false;
        while (!done)
        {
            wait(WaitMode.ALL, NO_TIMEOUT, cid);
            List<Response<String>> responses = getAndRemoveResponses(cid);
            logger.info("responses.size={}", responses.size());
            for (Response<String> response : responses) {
                String res = response.getResponse();
                logger.info("received: " + res);
                if (res.equals("GG"))
                {
                    done = true;
                }
                else {
                    if (lastResponse.compareTo(res) > 0) {
                        throw new AssertionError("Responses are not in correct order!");
                    }
                }
                lastResponse = res;
            }
        }
        logger.info("WF end, GG...");
    }
}