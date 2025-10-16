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
package org.copperengine.regtest.test;

import java.io.Serializable;

import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;

public class PersistentTimeoutTestWF extends PersistentWorkflow<Serializable> {

    private static final long serialVersionUID = 1L;

    @Override
    public void main() throws Interrupt {
        final String cid = getEngine().createUUID();
        wait(WaitMode.ALL, 100, cid);
        Response<Object> r = getAndRemoveResponse(cid);
        if (r == null)
            throw new RuntimeException("response is null");
        if (!r.isTimeout())
            throw new RuntimeException("timeout flag not set in response");
        if (r.getResponse() != null)
            throw new RuntimeException("unexpected payload in response");
        if (r.getException() != null)
            throw new RuntimeException("unexpected exception in response");
    }

}
