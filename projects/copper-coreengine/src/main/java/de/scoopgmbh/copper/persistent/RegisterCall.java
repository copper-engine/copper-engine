/*
 * Copyright 2002-2011 SCOOP Software GmbH
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
package de.scoopgmbh.copper.persistent;

import java.sql.Timestamp;
import java.util.Arrays;

import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.Workflow;

class RegisterCall {
	
	public Workflow<?> workflow;
	public WaitMode waitMode;
	public Long timeout;
	public String[] correlationIds;
	public Timestamp timeoutTS;
	
	public RegisterCall(Workflow<?> workflow, WaitMode waitMode, Long timeout, String[] correlationIds) {
		super();
		this.waitMode = waitMode;
		this.timeout = timeout;
		this.correlationIds = correlationIds;
		this.workflow = workflow;
		this.timeoutTS = timeout != null ? new Timestamp(System.currentTimeMillis()+timeout) : null;
	}

	@Override
	public String toString() {
		return "RegisterCall [correlationIds="
				+ Arrays.toString(correlationIds) + ", timeout=" + timeout
				+ ", timeoutTS=" + timeoutTS + ", waitMode=" + waitMode
				+ ", workflow=" + workflow + "]";
	}


}
