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

import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;

public class OrderWorkflow extends OrderBaseWorkflow {

    private static final long serialVersionUID = 1L;

    @Override
    public void main() throws Interrupt {
        final var correlationId = "correlationId";
        final var name = "Wolf";
        final var state = "main";
        capture(correlationId);
    }

    private void capture(final String correlationId) throws Interrupt {
        final var state = "capture";
        checkCustomer(correlationId);
        checkCredit(correlationId);
    }

    private void checkCustomer(final String correlationId) throws Interrupt {
        final var state = "checkCustomer";
        wait(WaitMode.FIRST, 100, correlationId);
        callWithRetry(correlationId);
    }

    private void checkCredit(final String correlationId) throws Interrupt {
        final var state = "checkCredit";
        wait(WaitMode.FIRST, 100, correlationId);
        callWithRetry(correlationId);
    }
}
