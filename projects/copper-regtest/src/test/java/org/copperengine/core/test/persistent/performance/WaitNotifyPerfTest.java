package org.copperengine.core.test.persistent.performance;

import java.util.concurrent.TimeUnit;

import org.copperengine.core.test.persistent.DataSourceType;
import org.copperengine.core.test.persistent.PersistentEngineTestContext;

public class WaitNotifyPerfTest {

    public void run() throws Exception {
        try (PersistentEngineTestContext ctx = new PersistentEngineTestContext(DataSourceType.Oracle, true)) {
            ctx.startup();
            final String id = ctx.getEngine().run("org.copperengine.core.test.persistent.WaitNotifyPerfTestWorkflow", null);
            ctx.getBackchannel().wait(id, 5, TimeUnit.MINUTES);
        }
    }

    public static void main(String[] args) {
        try {
            new WaitNotifyPerfTest().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
