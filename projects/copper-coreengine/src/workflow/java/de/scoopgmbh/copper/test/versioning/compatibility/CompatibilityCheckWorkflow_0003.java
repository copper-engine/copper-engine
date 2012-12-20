/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.test.versioning.compatibility;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.WorkflowDescription;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;

/**
 * Compatible change example 0003
 * 
 * This class is a compatible version of {@link CompatibilityCheckWorkflow_Base}. The following change(s) are applied:
 * 
 * Adding a new method and using it somewhere/everywhere within the workflow
 *
 * Important: The new method does not use COPPERs wait directly or indirectly and it does not declare to throw InterruptException
 * 
 * @author austermann
 *
 */
@WorkflowDescription(alias=CompatibilityCheckWorkflowDef.NAME,majorVersion=1,minorVersion=0,patchLevelVersion=0003)
public class CompatibilityCheckWorkflow_0003 extends PersistentWorkflow<Serializable> {
	
	private static final Logger logger = LoggerFactory.getLogger(CompatibilityCheckWorkflow_0003.class);

	private static final long serialVersionUID = 1L;
	
	private String aString;
	private String bString;
	
	@Override
	public void main() throws InterruptException {
		NEW_NeverWaitingMethod("test",1);
		aString = "A";
		NEW_NeverWaitingMethod("test",1);
		int localIntValue = 1;
		NEW_NeverWaitingMethod("test",1);
		directlyWaitingMethod(aString, localIntValue);
		NEW_NeverWaitingMethod("test",1);
		bString = "B";
		NEW_NeverWaitingMethod("test",1);
		localIntValue++;
		NEW_NeverWaitingMethod("test",1);
		indirectlyWaitingMethod(bString, localIntValue);
		NEW_NeverWaitingMethod("test",1);
	}
	
	protected void directlyWaitingMethod(String strValue, int intValue) throws InterruptException {
		NEW_NeverWaitingMethod("test",1);
		neverWaitingMethod(strValue, intValue);
		NEW_NeverWaitingMethod("test",1);
		this.wait(WaitMode.ALL, 500, Long.toHexString(System.currentTimeMillis()));
		NEW_NeverWaitingMethod("test",1);
	}
	
	protected void indirectlyWaitingMethod(String strValue, int intValue) throws InterruptException {
		NEW_NeverWaitingMethod("test",1);
		final Object localObject = 10867L;
		NEW_NeverWaitingMethod("test",1);
		directlyWaitingMethod(strValue, intValue);
		NEW_NeverWaitingMethod("test",1);
		logger.debug("{}", localObject);
		NEW_NeverWaitingMethod("test",1);
	}
	
	protected void neverWaitingMethod(String strValue, int intValue) {
		logger.debug("strValue="+strValue+", intValue="+intValue);
		anotherNeverWaitingMethod(strValue, intValue);
		NEW_NeverWaitingMethod("test",1);
	}
	
	protected void anotherNeverWaitingMethod(String strValue, int intValue) {
		logger.debug("strValue="+strValue+", intValue="+intValue);
	}

	protected void NEW_NeverWaitingMethod(String strValue, int intValue) {
		logger.debug("NEW strValue="+strValue+", intValue="+intValue);
	}

}
