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
package de.scoopgmbh.copper.monitor.server.persistent;

import de.scoopgmbh.copper.persistent.DatabaseDialect;

/**
 * Oracle implementation of the {@link DatabaseDialect} interface
 * 
 * @author austermann
 *
 */
public class OracleDialect extends BaseDatabaseMonitoringDialect {

	@Override
	public String getResultLimitingQuery(String query, long limit) {
		return "SELECT * FROM (\n" + 
							query+ 
				"			\n)\n" + 
				"			WHERE rownum <= "+limit;
	}
	
	protected String createWorkflowInstanceListQuery(){
		return "SELECT ID,STATE,PRIORITY,LAST_MOD_TS,PPOOL_ID,TIMEOUT,CREATION_TS,\n" + 
				"       ERR.\"EXCEPTION\",       ERR.ERROR_TS,  LAST_MOD_TS FINISHED_TS\n" + 
				"FROM COP_WORKFLOW_INSTANCE  MASTER, (select WORKFLOW_INSTANCE_ID, MAX(ROWID) keep (dense_rank last ORDER BY ERROR_TS) \"RID\" from COP_WORKFLOW_INSTANCE_ERROR GROUP BY WORKFLOW_INSTANCE_ID) ERR_RID, COP_WORKFLOW_INSTANCE_ERROR ERR\n" + 
				"WHERE\r\n" + 
				"	(? is null or PPOOL_ID=?) AND \n" + 
				"	(? is null or CLASSNAME=?) AND \n" + 
				"	(? is null or STATE=?) AND \n" + 
				"	(? is null or PRIORITY=?) AND\n" + 
				"	ERR_RID.WORKFLOW_INSTANCE_ID(+) = MASTER.ID AND\n" + 
				"	ERR.ROWID(+) = ERR_RID.RID";
	}

}
