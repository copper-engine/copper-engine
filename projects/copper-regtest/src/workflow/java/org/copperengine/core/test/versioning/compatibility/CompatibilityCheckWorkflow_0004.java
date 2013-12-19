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
 * Compatible change example 0004
 * This class is a compatible version of {@link CompatibilityCheckWorkflow_Base}. The following change(s) are applied:
 * Renaming a method
 * 
 * @author austermann
 */
@WorkflowDescription(alias = CompatibilityCheckWorkflowDef.NAME, majorVersion = 1, minorVersion = 0, patchLevelVersion = 0004)
public class CompatibilityCheckWorkflow_0004 extends PersistentWorkflow<Serializable> {

    private static final Logger logger = LoggerFactory.getLogger(CompatibilityCheckWorkflow_0004.class);

    private static final long serialVersionUID = 1L;

    private String aString;
    private String bString;

    @Override
    public void main() throws InterruptException {
        aString = "A";
        int localIntValue = 1;
        directlyWaitingMethod_RENAMED(aString, localIntValue);
        bString = "B";
        localIntValue++;
        indirectlyWaitingMethod_RENAMED(bString, localIntValue);
    }

    protected void directlyWaitingMethod_RENAMED(String strValue, int intValue) throws InterruptException {
        neverWaitingMethod_RENAMED(strValue, intValue);
        this.wait(WaitMode.ALL, 500, Long.toHexString(System.currentTimeMillis()));
    }

    protected void indirectlyWaitingMethod_RENAMED(String strValue, int intValue) throws InterruptException {
        final Object localObject = 10867L;
        directlyWaitingMethod_RENAMED(strValue, intValue);
        logger.debug("{}", localObject);
    }

    protected void neverWaitingMethod_RENAMED(String strValue, int intValue) {
        logger.debug("strValue=" + strValue + ", intValue=" + intValue);
        anotherNeverWaitingMethod_RENAMED(strValue, intValue);
    }

    protected void anotherNeverWaitingMethod_RENAMED(String strValue, int intValue) {
        logger.debug("strValue=" + strValue + ", intValue=" + intValue);
    }

}
