package org.copperengine.core.test.persistent;

import java.util.Collections;
import java.util.Comparator;
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
        String cid = getData();
        logger.info("WF started, id=" + getId() + ", cid=" + cid);
        boolean done = false;
        while (!done)
        {
            wait(WaitMode.ALL, NO_TIMEOUT, cid);
            List<Response<String>> responses = getAndRemoveResponses(cid);
            Collections.sort(responses, new Comparator<Response<String>>() {
                @Override
                public int compare(Response<String> o1, Response<String> o2) {
                    return o1.getResponse().compareTo(o2.getResponse());
                }
            });
            for (Response<String> response : responses) {
                String res = response.getResponse();
                logger.info("received: " + res);
                if (res.equals("GG"))
                {
                    done = true;
                }
            }
        }
        logger.info("WF end, GG...");
    }
}