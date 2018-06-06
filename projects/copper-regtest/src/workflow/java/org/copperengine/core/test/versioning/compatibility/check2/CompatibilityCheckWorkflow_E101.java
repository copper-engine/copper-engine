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
package org.copperengine.regtest.test.versioning.compatibility.check2;

import java.io.Serializable;

import org.copperengine.core.Interrupt;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Incompatible change example E101
 * This class is a incompatible version of
 * {@link org.copperengine.regtest.test.versioning.compatibility.check2.CompatibilityCheckWorkflow_Base}. The following
 * change(s) are applied:
 * Replacing method calls of methods that directly or indirectly use COPPER wait
 *
 * @author austermann
 */
public class CompatibilityCheckWorkflow_E101 extends PersistentWorkflow<Serializable> {

    private static final Logger logger = LoggerFactory.getLogger(CompatibilityCheckWorkflow_E101.class);

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private String aString;
    private String bString;

    @Override
    public void main() throws Interrupt {
        aString = "A";
        int localIntValue = 1;
        // directlyWaitingMethod(aString, localIntValue);
        anotherDirectlyWaitingMethod(4711L, localIntValue); // REPLACED HERE
        bString = "B";
        localIntValue++;
        indirectlyWaitingMethod(bString, localIntValue);
    }

    protected void directlyWaitingMethod(String strValue, int intValue) throws Interrupt {
        String localString = strValue;
        Integer localInteger = intValue;
        neverWaitingMethod(strValue, localInteger);
        this.wait(WaitMode.ALL, 500, Long.toHexString(System.currentTimeMillis()));
        logger.debug(localString);
    }

    protected void anotherDirectlyWaitingMethod(Long longValue, Integer intValue) throws Interrupt {
        neverWaitingMethod(longValue.toString(), intValue);
        this.wait(WaitMode.ALL, 500, Long.toHexString(System.currentTimeMillis()));
    }

    protected void indirectlyWaitingMethod(String strValue, int intValue) throws Interrupt {
        final Object localObject = 10867L;
        directlyWaitingMethod(strValue, intValue);
        logger.debug("{}", localObject);
    }

    protected void neverWaitingMethod(String strValue, int intValue) {
        logger.debug("strValue=" + strValue + ", intValue=" + intValue);
        anotherNeverWaitingMethod(strValue, intValue);
    }

    protected void anotherNeverWaitingMethod(String strValue, int intValue) {
        logger.debug("strValue=" + strValue + ", intValue=" + intValue);
    }
}
