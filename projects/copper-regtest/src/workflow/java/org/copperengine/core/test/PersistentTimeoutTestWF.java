/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package org.copperengine.core.test;

import java.io.Serializable;

import org.copperengine.core.InterruptException;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;

import com.mchange.util.AssertException;

public class PersistentTimeoutTestWF extends PersistentWorkflow<Serializable> {

    private static final long serialVersionUID = 1L;

    @Override
    public void main() throws InterruptException {
        final String cid = getEngine().createUUID();
        wait(WaitMode.ALL, 100, cid);
        Response<Object> r = getAndRemoveResponse(cid);
        if (r == null)
            throw new AssertException("response is null");
        if (!r.isTimeout())
            throw new AssertException("timeout flag not set in response");
        if (r.getResponse() != null)
            throw new AssertException();
        if (r.getException() != null)
            throw new AssertException();
    }

}
