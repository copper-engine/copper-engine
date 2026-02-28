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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.Auditor;
import org.copperengine.core.Callback;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.jspecify.annotations.NonNull;
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
        LoggerFactory.getLogger(this.getClass()).trace("interrupt {}", jumpNos);
        this.lastInterruptCalled = lastCall(jumpNos);

    }

    @Override
    public void resume(final List<Integer> jumpNos) {
        LoggerFactory.getLogger(this.getClass()).trace("resume {}", jumpNos);
        this.lastResumeCalled = lastCall(jumpNos);

    }

    private void interruptableMethod() throws Interrupt {
        System.out.println("No interrupt, just declaration.");
    }

    private static boolean lastCall(List<Integer> jumpNos) {
        System.out.println("lastCall " + jumpNos);
        return jumpNos.size() == 2
                && jumpNos.get(0) == 7
                && jumpNos.get(1) == 2;
    }

    @Override
    public void main() throws Interrupt {
        System.out.println("main start");
        try {
            if (!startCalled) {
                throw new RuntimeException("Missing start()");
            }
            wait(WaitMode.FIRST, 1, "xx");
            System.out.println("jumpNo=0");

            wait(WaitMode.FIRST, 1, createCallback());
            System.out.println("jumpNo=1");

            wait(WaitMode.FIRST, 1L, TimeUnit.MILLISECONDS, "xx");
            System.out.println("jumpNo=2");

            wait(WaitMode.ALL, 1L, TimeUnit.MILLISECONDS, createCallback());
            System.out.println("jumpNo=3");

            // notify early response
            getEngine().notify(new Response<>("xx3"), new Acknowledge.DefaultAcknowledge());
            waitForAll("xx3");
            System.out.println("jumpNo=4");

            getEngine().notify(new Response<>("xx4"), new Acknowledge.DefaultAcknowledge());
            waitForAll(getCallback("xx4"));
            System.out.println("jumpNo=5");

            subWorkflow(1, 666L);
            System.out.println("jumpNo=6");

            subWorkflow(2, 777L);
            System.out.println("jumpNo=7");

            interruptableMethod();
            System.out.println("jumpNo=8");

            if (!lastInterruptCalled) {
                throw new RuntimeException("Missing last interrupt()");
            }
            if (!lastResumeCalled) {
                throw new RuntimeException("Missing last resume()");
            }
            System.out.println("main end");
        } catch (Exception e) {
            System.err.println("Wait 6 secs to make test fail. " + e.getMessage());
            e.printStackTrace();
            LockSupport.parkNanos(Duration.ofSeconds(6).toNanos());
            throw e;
        }
    }

    public void subWorkflow(int i1, long l) throws Interrupt {
        System.out.println("subWorkflow start");

        resubmit();
        System.out.println("jumpNo=0");

        savepoint();
        System.out.println("jumpNo=1");

        super.savepoint();
        System.out.println("jumpNo=2");

        System.out.println("subWorkflow end");
    }

    private @NonNull Callback<Object> getCallback(final String correlationId) {
        return new Callback<>() {

            @Override
            public String getCorrelationId() {
                return correlationId;
            }

            @Override
            public void notify(Object response, Acknowledge ack) {
                getEngine().notify(new Response<>(correlationId, response, null), ack);
            }

            @Override
            public void notify(Exception exception, Acknowledge ack) {
                getEngine().notify(new Response<>(correlationId, null, exception), ack);
            }

            @Override
            public Response<Object> getResponse(Workflow<?> wf) {
                return null;
            }
        };
    }
}
