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
package org.copperengine.core.test.versioning.compatibility;

import java.io.Serializable;

import org.copperengine.core.InterruptException;
import org.copperengine.core.WaitMode;
import org.copperengine.core.WorkflowDescription;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compatible change example 0006
 * This class is a compatible version of {@link CompatibilityCheckWorkflow_Base}. The following change(s) are applied:
 * Adding a new waiting method and calling it AFTER existing wait calls (directly or indirectly).
 * Adding a new wait call AFTER existing wait calls (directly or indirectly).
 *
 * @author austermann
 */
@WorkflowDescription(alias = CompatibilityCheckWorkflowDef.NAME, majorVersion = 1, minorVersion = 0, patchLevelVersion = 0006)
public class CompatibilityCheckWorkflow_0006 extends PersistentWorkflow<Serializable> {

    private static final Logger logger = LoggerFactory.getLogger(CompatibilityCheckWorkflow_0006.class);

    private static final long serialVersionUID = 1L;

    private String aString;
    private String bString;

    @Override
    public void main() throws InterruptException {
        aString = "A";
        int localIntValue = 1;
        directlyWaitingMethod(aString, localIntValue);
        bString = "B";
        localIntValue++;
        indirectlyWaitingMethod(bString, localIntValue);

        // Here is a new method call for a waiting method
        anotherDirectlyWaitingMethod();
        // It' also ok to add a further wait call directly here
        this.wait(WaitMode.ALL, 500, Long.toHexString(System.currentTimeMillis()));
    }

    protected void directlyWaitingMethod(String strValue, int intValue) throws InterruptException {
        neverWaitingMethod(strValue, intValue);
        this.wait(WaitMode.ALL, 500, Long.toHexString(System.currentTimeMillis()));

        // It's also ok to add a further wait call here.
        // It is important to add the new wait call AFTER the existing code, ore more precise:
        // the new wait call MUST be after pre existing wait calls and after method calls that directly or indirectly
        // execute COPPER wait.
        this.wait(WaitMode.ALL, 500, Long.toHexString(System.currentTimeMillis()));
    }

    protected void indirectlyWaitingMethod(String strValue, int intValue) throws InterruptException {
        final Object localObject = 10867L;
        directlyWaitingMethod(strValue, intValue);
        // To add this wait call is also ok
        this.wait(WaitMode.ALL, 500, Long.toHexString(System.currentTimeMillis()));
        logger.debug("{}", localObject);
    }

    protected void neverWaitingMethod(String strValue, int intValue) {
        logger.debug("strValue=" + strValue + ", intValue=" + intValue);
        anotherNeverWaitingMethod(strValue, intValue);
    }

    protected void anotherNeverWaitingMethod(String strValue, int intValue) {
        logger.debug("strValue=" + strValue + ", intValue=" + intValue);
    }

    protected void anotherDirectlyWaitingMethod() throws InterruptException {
        logger.debug("anotherDirectlyWaitingMethod()");
        this.wait(WaitMode.ALL, 500, Long.toHexString(System.currentTimeMillis()));
    }
}
