/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.core;

import java.lang.reflect.InvocationTargetException;

/**
 * Utiltiy class to signal detached execution states.
 * In most cases {@link DefaultAcknowledge#waitForAcknowledge} can be employed to obtain a safe result on the caller
 * side.
 *
 * @author rscheel
 */
public interface Acknowledge {

    void onSuccess();

    void onException(Throwable t);

    public static class DefaultAcknowledge implements Acknowledge {

        Object[] responseHolder = new Object[1];

        @Override
        public final void onSuccess() {
            synchronized (responseHolder) {
                responseHolder[0] = Void.class;
                responseHolder.notify();
            }
        }

        @Override
        public final void onException(Throwable t) {
            synchronized (responseHolder) {
                responseHolder[0] = t instanceof InvocationTargetException ? (InvocationTargetException) t : new InvocationTargetException(t);
                responseHolder.notify();
            }
        }

        public void waitForAcknowledge() throws CopperRuntimeException {
            synchronized (responseHolder) {
                while (responseHolder[0] == null) {
                    try {
                        responseHolder.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CopperRuntimeException(e);
                    }
                }
                if (responseHolder[0] == Void.class)
                    return;
                throw new CopperRuntimeException(((InvocationTargetException) responseHolder[0]).getCause());
            }
        }

    }

    public static class BestEffortAcknowledge implements Acknowledge {

        @Override
        public void onSuccess() {
        }

        @Override
        public void onException(Throwable t) {
        }

    }

}
