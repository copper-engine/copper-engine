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
package org.copperengine.core.test.versioning.compatibility.check2;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.InterruptException;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompatibilityCheckWorkflow_Base extends PersistentWorkflow<Serializable> {

    private static final Logger logger = LoggerFactory.getLogger(CompatibilityCheckWorkflow_Base.class);

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
    }

    protected void directlyWaitingMethod(String strValue, int intValue) throws InterruptException {
        String localString = strValue;
        Integer localInteger = intValue;
        neverWaitingMethod(strValue, localInteger);
        this.wait(WaitMode.ALL, 500, TimeUnit.MILLISECONDS, Long.toHexString(System.currentTimeMillis()));
        logger.debug(localString);
    }

    protected void anotherDirectlyWaitingMethod(Long longValue, Integer intValue) throws InterruptException {
        neverWaitingMethod(longValue.toString(), intValue);
        this.wait(WaitMode.ALL, 500, TimeUnit.MILLISECONDS, Long.toHexString(System.currentTimeMillis()));
    }

    protected void indirectlyWaitingMethod(String strValue, int intValue) throws InterruptException {
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
