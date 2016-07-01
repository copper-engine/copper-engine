package org.copperengine.core.test.persistent;

import java.util.concurrent.TimeUnit;

import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.test.backchannel.WorkflowResult;

public class CrashTest {

    private final DataSourceType dsType;
    private final int COUNT = 50;

    public CrashTest(final DataSourceType dsType) {
        this.dsType = dsType;
    }

    public void phaseOne() throws Exception {
        final PersistentEngineTestContext context = new PersistentEngineTestContext(dsType, true);
        context.startup();
        try {
            final PersistentScottyEngine engine = context.getEngine();
            for (int i = 0; i < COUNT; i++)
                engine.run("org.copperengine.core.test.persistent.CrashTestWorkflow", "wf#" + i);

            System.out.println("Workflow instances created!");
            Thread.sleep(777);
            System.out.println("Simulating crash...!");
            System.exit(0);
        } finally {
            context.close();
        }
    }

    public void phaseTwo() throws Exception {
        final PersistentEngineTestContext context = new PersistentEngineTestContext(dsType, false);
        context.startup();
        System.out.println("Engine resumed");
        try {
            for (int i = 0; i < COUNT;) {
                WorkflowResult x = context.getBackChannelQueue().dequeue(60, TimeUnit.SECONDS);
                if (x == null) {
                    System.out.println("Timeout!");
                }
                else {
                    System.out.println(x.getResult());
                    i++;
                }
            }
            System.out.println("All workflow instances finished");

        } finally {
            context.close();
        }

    }

    public static void main(String[] args) {
        try {
            DataSourceType dsType = DataSourceType.valueOf(args[0]);
            String phase = args[1];
            if ("1".equals(phase)) {
                new CrashTest(dsType).phaseOne();
            }
            else {
                new CrashTest(dsType).phaseTwo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
