package de.scoopgmbh.copper.management;

public interface DatabaseDialectMXBean {
	
	/**
	 * Sets the default removal timeout for stale responses in the underlying database. A response is stale/timed out when
	 * there is no workflow instance waiting for it within the specified amount of time. 
	 * @param defaultStaleResponseRemovalTimeout
	 */
	public void setDefaultStaleResponseRemovalTimeout(int defaultStaleResponseRemovalTimeout);
	
	public int getDefaultStaleResponseRemovalTimeout();
	
	public void setDbBatchingLatencyMSec(int dbBatchingLatencyMSec);
	
	public int getDbBatchingLatencyMSec();
	
	public void setRemoveWhenFinished(boolean removeWhenFinished);
	
	public boolean isRemoveWhenFinished();
	
	public String getDialectDescription();
	
}
