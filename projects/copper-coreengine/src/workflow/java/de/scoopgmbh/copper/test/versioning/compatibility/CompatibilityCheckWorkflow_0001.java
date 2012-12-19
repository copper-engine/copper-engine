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
 * Compatible change example 0001
 * 
 * renaming of variable/parameter names;
 * 
 * @author austermann
 *
 */
@WorkflowDescription(alias=CompatibilityCheckWorkflowDef.NAME,majorVersion=1,minorVersion=0,patchLevelVersion=0001)
public class CompatibilityCheckWorkflow_0001 extends PersistentWorkflow<Serializable> {
	
	private static final Logger logger = LoggerFactory.getLogger(CompatibilityCheckWorkflow_0001.class);

	private static final long serialVersionUID = 1L;
	
	private String aString_RENAMED;
	private String bString;
	
	@Override
	public void main() throws InterruptException {
		aString_RENAMED = "A";
		int localIntValue_RENAMED = 1;
		directlyWaitingMethod(aString_RENAMED, localIntValue_RENAMED);
		bString = "B";
		localIntValue_RENAMED++;
		indirectlyWaitingMethod(bString, localIntValue_RENAMED);
	}
	
	protected void directlyWaitingMethod(String strValue_RENAMED, int intValue) throws InterruptException {
		neverWaitingMethod(strValue_RENAMED, intValue);
		this.wait(WaitMode.ALL, 500, Long.toHexString(System.currentTimeMillis()));
	}
	
	protected void indirectlyWaitingMethod(String strValue, int intValue_RENAMED) throws InterruptException {
		final Object localObject = 10867L;
		directlyWaitingMethod(strValue, intValue_RENAMED);
		logger.debug("{}", localObject);
	}
	
	protected void neverWaitingMethod(String strValue_XXX_RENAMED, int intValue) {
		logger.debug("strValue="+strValue_XXX_RENAMED+", intValue="+intValue);
		anotherNeverWaitingMethod(strValue_XXX_RENAMED, intValue);
	}
	
	protected void anotherNeverWaitingMethod(String strValue, int intValue) {
		logger.debug("strValue="+strValue+", intValue="+intValue);
	}
	

}
