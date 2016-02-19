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
package org.copperengine.core.test.persistent;

import java.io.Serializable;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitHook;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.persistent.PersistentWorkflow;

public class ErrorWaitHookUnitTestWorkflow extends PersistentWorkflow<Serializable> {

    private static final long serialVersionUID = 1L;

    @Override
    public void main() throws Interrupt {
        wait(WaitMode.ALL, 50, TimeUnit.MILLISECONDS, getEngine().createUUID());
        getEngine().addWaitHook(this.getId(), new WaitHook() {
            @Override
            public void onWait(Workflow<?> wf, Connection con) throws Exception {
                throw new RuntimeException("TEST Exception");
            }
        });
        wait(WaitMode.ALL, 50, TimeUnit.MILLISECONDS, getEngine().createUUID());
    }

}
