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
	 * Attention! You may not commit or rollback the transaction currently hold be the specified
	 * connection (if not null). This will be done by COPPER. You may just use this to hook custom code into 
	 * the transaction context.  
	 *  
	 * @param wf Workflow instance
	 * @param con DB connection or null, in case there is no DB connection in the scope
	 * @throws Exception
	 */
	public void onWait(Workflow<?> wf, Connection con) throws Exception;
}
