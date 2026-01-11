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
package test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import org.copperengine.core.Auditor;
import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.slf4j.LoggerFactory;

public class AuditorWorkflow extends Workflow<Void> implements Auditor {

    private static final long serialVersionUID = 1L;
    private boolean startCalled = false;
    private boolean lastInterruptCalled = false;
    private boolean lastResumeCalled = false;

    @Override
    public void start() {
        LoggerFactory.getLogger(this.getClass()).trace("start");
        this.startCalled = true;
    }

    @Override
    public void interrupt(final List<Integer> jumpNos) {
        LoggerFactory.getLogger(this.getClass()).trace("interrupt {}", jumpNos  );
        this.lastInterruptCalled = lastCall(jumpNos);

    }

    @Override
    public void resume(final List<Integer> jumpNos) {
        LoggerFactory.getLogger(this.getClass()).trace("resume {}", jumpNos  );
        this.lastResumeCalled = lastCall(jumpNos);

    }

    private static boolean lastCall(List<Integer> jumpNos) {
        return jumpNos.size() == 2
                && jumpNos.get(0) == 3
                && jumpNos.get(1) == 0;
    }

    @Override
    public void main() throws Interrupt {
        System.out.println("main start");
        try {
            if (!startCalled) {
                throw new RuntimeException("Missing start()");
            }
            savepoint();
            System.out.println("jumpNo=0");
            wait(WaitMode.FIRST, 1, "xx");
            System.out.println("jumpNo=1");
            subWorkflow(1, 666L);
            System.out.println("jumpNo=2");
            subWorkflow(2, 777L);
            System.out.println("jumpNo=3");
            if (!lastInterruptCalled) {
                throw new RuntimeException("Missing last interrupt()");
            }
            if (!lastResumeCalled) {
                throw new RuntimeException("Missing last resume()");
            }
            System.out.println("main end");
        } catch (Exception e) {
            System.err.println("Wait 6 secs to make test fail.");
            LockSupport.parkNanos(Duration.ofSeconds(6).toNanos());
            throw e;
        }
    }

    public void subWorkflow(int i1, long l) throws Interrupt {
        System.out.println("subWorkflow start");
        savepoint();
        System.out.println("jumpNo=0");
        System.out.println("subWorkflow end");
    }
}
