package org.copperengine.core.test.tranzient.simple;

import static org.junit.Assert.assertEquals;
import org.copperengine.core.EngineState;
import org.copperengine.core.test.tranzient.TransientEngineTestContext;
import org.copperengine.core.util.BlockingResponseReceiver;
import org.junit.Test;

public class WorkflowGetAnyNonTimedOutAndRemoveResponseTest {

    @Test
    public void testWorkflow() throws Exception {
        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            BlockingResponseReceiver<Integer> brr = new BlockingResponseReceiver<Integer>();
            ctx.getEngine().run("org.copperengine.core.test.tranzient.simple.WorkflowGetAnyNonTimedOutAndRemoveResponseWorkflow", brr);
            brr.wait4response(5000L);
            assertEquals(1, brr.getResponse().intValue());
        }
    }
}
