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
package org.copperengine.core.test.tranzient.lockManager;


import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.copperengine.core.CopperException;
import org.copperengine.core.EngineState;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.lockmgr.tranzient.TransientLockManager;
import org.copperengine.core.test.tranzient.TransientEngineTestContext;
import org.copperengine.core.tranzient.TransientPriorityProcessorPool;
import org.copperengine.core.tranzient.TransientProcessorPool;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class TransientLockManagerTest {

    @Test
    public void testWorkflow() throws Exception {
        try (TransientEngineTestContext ctx = new TransientEngineTestContext()) {
            ctx.startup();
            assertEquals(EngineState.STARTED, ctx.getEngine().getEngineState());

            // Extend the dependency injector for this test
            Supplier<TransientLockManager> lockMgr = Suppliers.memoize(new Supplier<TransientLockManager>() {
                @Override
                public TransientLockManager get() {
                    return new TransientLockManager(ctx.getEngine());
                }
            });
            ctx.getSuppliers().put("lockManager", lockMgr);

            // And add a result map to evaluate the locking mechanism
            Supplier<ConcurrentHashMap<Integer, TransientLockEvaluationData>> evalData = Suppliers.memoize(new Supplier<ConcurrentHashMap<Integer, TransientLockEvaluationData>>() {
               @Override
                public ConcurrentHashMap<Integer, TransientLockEvaluationData> get() {
                   return new ConcurrentHashMap<>(10);
               }
            });
            ctx.getSuppliers().put("evalData", evalData);


            final int NUM_WORKFLOWS_TO_LAUNCH = 8;
            assertTrue(NUM_WORKFLOWS_TO_LAUNCH >= 6);
            List<WorkflowInstanceDescr<?>> wfsToBeLaunched = new ArrayList<>();
            for(int i=0; i<NUM_WORKFLOWS_TO_LAUNCH; i++) {
                int data = i;
                if (data <3) {
                    data = 0;
                } else if (data < 6) {
                    data = 3;
                }
                wfsToBeLaunched.add(
                        new WorkflowInstanceDescr<Integer>("org.copperengine.core.test.tranzient.TransientLockManagerTestWorkflow",
                                data,
                                "Workflow-"+i,
                                5,
                                null));
            }

            // Start the list of workflows. The first 4 of them shall go for the same lockID, the next 4 on another but for them same lock id
            // and the rest with unique locks each, so no locking after all.
            try {
                ctx.getEngine().runBatch(wfsToBeLaunched);
            } catch (CopperException e) {
                e.printStackTrace();
            }

            while (ctx.getEngine().queryWorkflowInstances().size() > 0)
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Don't care.
                }
            }

            ConcurrentHashMap<Integer, TransientLockEvaluationData> evaluation = evalData.get();
            for (int i : new int[]{0, 3} ) {
                assertNotNull(evaluation.get(i));
            }
            for (int i=6;i<NUM_WORKFLOWS_TO_LAUNCH;i++) {
                assertNotNull(evaluation.get(i));
                assertEquals(0, evaluation.get(i).getCountOfAlreadyAcquired());
            }

            // Now let's run two more workflows. Transient Lock Manager should recreate locks
            assertTrue(lockMgr.get().curNumberOfLocks() == 0);
            evaluation.clear();
            for (int i=0; i<2; i++) {
                try {
                    ctx.getEngine().run(new WorkflowInstanceDescr<Integer>("org.copperengine.core.test.tranzient.TransientLockManagerTestWorkflow",
                            0,
                            "WorkflowRunTwo-" + i,
                            5,
                            null));
                } catch (Exception e) {
                    throw new AssertionError(e.getMessage());
                }
            }
            while (ctx.getEngine().queryWorkflowInstances().size() > 0)
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Don't care.
                }
            }

            // Now test if wait worked fine as well.
            if (((TransientPriorityProcessorPool)ctx.getPpoolManager().getProcessorPool(TransientProcessorPool.DEFAULT_POOL_ID)).getNumberOfThreads()>1) {
                assertEquals(2, evaluation.get(0).getCountOfAlreadyAcquired()); //First runs without wait. Second calls wait comes to timeout and calls wait again (so 2 times)
            } else {
                assertEquals(0, evaluation.get(0).getCountOfAlreadyAcquired()); // Single threaded, lock can always be acquired.
            }
            // And lock map is cleared (All locks removed after not to be used anymore)
            assertTrue(lockMgr.get().curNumberOfLocks() == 0);
        }
    }

}
