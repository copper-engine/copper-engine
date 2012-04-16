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
package de.scoopgmbh.copper;

import java.sql.Connection;

/**
 * Callback interface used by COPPER to enable execution of custom code in the context of a workflow instance' wait invocation.
 * 
 * @author austermann
 *
 */
public interface WaitHook {
	
	/**
	 * Called for one single wait invocation of the corresponding workflow instance.
	 * This method may be called multiple times, e.g. in case of a rollback with subsequent retry.
	 * Attention! You may not commit or rollback the transaction currently hold by the specified
	 * connection (if not null). This will be done by COPPER. You may just use this to hook custom code into 
	 * the transaction context.  
	 *  
	 * @param wf Workflow instance
	 * @param con DB connection or null, in case there is no DB connection in the scope
	 * @throws Exception
	 */
	public void onWait(Workflow<?> wf, Connection con) throws Exception;
}
