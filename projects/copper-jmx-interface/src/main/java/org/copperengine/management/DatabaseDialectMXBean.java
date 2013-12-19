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
package org.copperengine.management;

public interface DatabaseDialectMXBean {
	
	/**
	 * Sets the default removal timeout for stale responses in the underlying database. A response is stale/timed out when
	 * there is no workflow instance waiting for it within the specified amount of time. 
	 * @param defaultStaleResponseRemovalTimeout
	 */
	public void setDefaultStaleResponseRemovalTimeout(long defaultStaleResponseRemovalTimeout);
	
	public long getDefaultStaleResponseRemovalTimeout();
	
	public void setDbBatchingLatencyMSec(int dbBatchingLatencyMSec);
	
	public int getDbBatchingLatencyMSec();
	
	public void setRemoveWhenFinished(boolean removeWhenFinished);
	
	public boolean isRemoveWhenFinished();
	
	public String getDialectDescription();
	
}
