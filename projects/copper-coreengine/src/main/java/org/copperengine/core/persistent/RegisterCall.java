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
package org.copperengine.core.persistent;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.copperengine.core.WaitHook;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;

/**
 * Simple class that bundles the input parameters of a call to {@link ScottyDBStorageInterface#registerCallback}
 *
 * @author austermann
 */
public class RegisterCall {

    public Workflow<?> workflow;
    public WaitMode waitMode;
    public Long timeout;
    public String[] correlationIds;
    public Timestamp timeoutTS;
    public List<WaitHook> waitHooks;

    public RegisterCall(Workflow<?> workflow, WaitMode waitMode, Long timeout, String[] correlationIds, List<WaitHook> waitHooks) {
        super();
        this.waitMode = waitMode;
        this.timeout = timeout;
        this.correlationIds = correlationIds;
        this.workflow = workflow;
        this.timeoutTS = timeout != null ? new Timestamp(System.currentTimeMillis() + timeout) : null;
        this.waitHooks = waitHooks;
    }

    @Override
    public String toString() {
        return "RegisterCall [correlationIds="
                + Arrays.toString(correlationIds) + ", timeout=" + timeout
                + ", timeoutTS=" + timeoutTS + ", waitMode=" + waitMode + ", waitHooks.size=" + waitHooks.size()
                + ", workflow=" + workflow + "]";
    }

}
