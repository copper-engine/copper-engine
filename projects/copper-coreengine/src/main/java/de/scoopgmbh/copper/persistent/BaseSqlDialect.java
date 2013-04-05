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
package de.scoopgmbh.copper.persistent;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.support.JdbcUtils;

import de.scoopgmbh.copper.audit.MessagePostProcessor;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassVersionInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummary;

/**
 * Base implementation of the {@link DatabaseDialect} for SQL databases
 * 
 */
public abstract class BaseSqlDialect implements DatabaseDialect {

	public WorkflowStateSummary selectTotalWorkflowStateSummary(Connection con){
		PreparedStatement selectStmt = null;
		try {
			selectStmt = con.prepareStatement("select state, count(*) from COP_WORKFLOW_INSTANCE group by state");
			ResultSet result = selectStmt.executeQuery();
			selectStmt.setFetchSize(10);
			
			Map<WorkflowInstanceState, Integer> numberOfWorkflowInstancesWithState = new HashMap<WorkflowInstanceState, Integer>();
			while (result.next()) {
				int status = result.getInt(1);
				int count = result.getInt(2);
				numberOfWorkflowInstancesWithState.put(DBProcessingState.fromKey(status).asWorkflowInstanceState(), count);
			}
			return new WorkflowStateSummary(numberOfWorkflowInstancesWithState);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			JdbcUtils.closeStatement(selectStmt);
		}
	}

	@Override
	public List<AuditTrailInfo> selectAuditTrails(String workflowClass, String workflowInstanceId, String correlationId, Integer level, long resultRowLimit,
			Connection con) {

		PreparedStatement selectStmt = null;
		try {
			selectStmt = con.prepareStatement(getResultLimitingQuery(
					"SELECT SEQ_ID,OCCURRENCE,CONVERSATION_ID,LOGLEVEL,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID,MESSAGE_TYPE FROM COP_AUDIT_TRAIL_EVENT a\n" + 
					"LEFT OUTER JOIN COP_WORKFLOW_INSTANCE i ON a.INSTANCE_ID=i.ID \n" + 
					"WHERE\n" + 
					"	(? is null or i.CLASSNAME=?) AND \n" + 
					"	(? is null or a.INSTANCE_ID=?) AND \n" + 
					"	(? is null or a.CORRELATION_ID=?) AND \n" + 
					"	(? is null or a.LOGLEVEL=?)",resultRowLimit));
			int pIdx = 1;
			selectStmt.setString(pIdx++, workflowClass);
			selectStmt.setString(pIdx++, workflowClass);
			selectStmt.setString(pIdx++, workflowInstanceId);
			selectStmt.setString(pIdx++, workflowInstanceId);
			selectStmt.setString(pIdx++, correlationId);
			selectStmt.setString(pIdx++, correlationId);
			selectStmt.setObject(pIdx++, level,java.sql.Types.INTEGER);
			selectStmt.setObject(pIdx++, level,java.sql.Types.INTEGER);
			
			selectStmt.setFetchSize(100);
			
			ResultSet resultSet = selectStmt.executeQuery();
			
			ArrayList<AuditTrailInfo> logs = new ArrayList<AuditTrailInfo>();
			while (resultSet.next()) {
				AuditTrailInfo auditTrailInfo = new AuditTrailInfo();
				auditTrailInfo.setId(resultSet.getLong(1));
				auditTrailInfo.setOccurrence(resultSet.getTimestamp(2)!=null?new Date(resultSet.getTimestamp(2).getTime()):null);
				auditTrailInfo.setConversationId(resultSet.getString(3));
				auditTrailInfo.setLoglevel(resultSet.getInt(4));
				auditTrailInfo.setContext(resultSet.getString(5));
				auditTrailInfo.setWorkflowInstanceId(resultSet.getString(6));
				auditTrailInfo.setCorrelationId(resultSet.getString(7));
				auditTrailInfo.setTransactionId(resultSet.getString(8));
				auditTrailInfo.setMessageType(resultSet.getString(9));
				logs.add(auditTrailInfo);
			}
			return logs;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			JdbcUtils.closeStatement(selectStmt);
		}
	}

	@Override
	public String selectAuditTrailMessage(long id, Connection con, MessagePostProcessor messagePostProcessor) {
		PreparedStatement selectStmt = null;
		try {
			selectStmt = con.prepareStatement("SELECT LONG_MESSAGE FROM COP_AUDIT_TRAIL_EVENT WHERE SEQ_ID=?");
			selectStmt.setLong(1, id);
			ResultSet result = selectStmt.executeQuery();
			
			while (result.next()) {
				Clob message = result.getClob(1);
				if ((int)message.length() > 0) {  
					return messagePostProcessor.deserialize(message.getSubString(1, (int) message.length()));
			    } 
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			JdbcUtils.closeStatement(selectStmt);
		}
		return "";
	}	
	
	/**wrap query to limit result rows
	 * e.g Oracle: SELECT * from T WHERE ROWNUM <= 10 
	 * @return 
	 */
	public abstract String getResultLimitingQuery(String query, long limit);

	@Override
	public List<WorkflowSummary> selectWorkflowStateSummary(String poolid, String classname, long resultRowLimit,Connection con) {
		PreparedStatement selectStmt = null;
		try {
			selectStmt = con.prepareStatement(getResultLimitingQuery(
					"select CLASSNAME, STATE, count(*) from COP_WORKFLOW_INSTANCE \n" + 
					"WHERE\n" + 
					"	(? is null or PPOOL_ID=?) AND \n" + 
					"	(? is null or CLASSNAME=?) GROUP BY CLASSNAME,STATE",resultRowLimit));
			int pIdx = 1;
			selectStmt.setString(pIdx++, poolid);
			selectStmt.setString(pIdx++, poolid);
			selectStmt.setString(pIdx++, classname);
			selectStmt.setString(pIdx++, classname);
			
			selectStmt.setFetchSize(100);
			ResultSet resultSet = selectStmt.executeQuery();
			
			Map<String, WorkflowSummary> classNameToSummery = new HashMap<String, WorkflowSummary>();
			while (resultSet.next()) {
				String instanceClassname=resultSet.getString(1); 
				WorkflowSummary summery = classNameToSummery.get(instanceClassname);
				if (summery==null){
					summery= new WorkflowSummary();
					summery.setClassDescription(new WorkflowClassVersionInfo(instanceClassname, "", 1L, 1L, 1L));
					classNameToSummery.put(classname, summery);
				}
				int status = resultSet.getInt(2);
				int count = resultSet.getInt(3);
				
				summery.setStateSummery(new WorkflowStateSummary(new HashMap<WorkflowInstanceState, Integer>()));
				summery.getStateSummery().getNumberOfWorkflowInstancesWithState().put(DBProcessingState.fromKey(status).asWorkflowInstanceState(), count);
			}
			return new ArrayList<WorkflowSummary>(classNameToSummery.values());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			JdbcUtils.closeStatement(selectStmt);
		}
	}
	
	@Override
	public List<WorkflowInstanceInfo> selectWorkflowInstanceList(String poolid, String classname,
			WorkflowInstanceState state, Integer priority, long resultRowLimit,Connection con) {
		PreparedStatement selectStmt = null;
		try {
			selectStmt = con.prepareStatement(getResultLimitingQuery(
					"SELECT ID,STATE,PRIORITY,LAST_MOD_TS,PPOOL_ID,TIMEOUT,CREATION_TS FROM COP_WORKFLOW_INSTANCE \n" + 
					"WHERE\n" + 
					"	(? is null or PPOOL_ID=?) AND \n" + 
					"	(? is null or CLASSNAME=?) AND \n" + 
					"	(? is null or STATE=?) AND \n" + 
					"	(? is null or PRIORITY=?)",resultRowLimit));
			int pIdx = 1;
			selectStmt.setString(pIdx++, null);
			selectStmt.setString(pIdx++, null);
			selectStmt.setString(pIdx++, classname);
			selectStmt.setString(pIdx++, classname);
			selectStmt.setObject(pIdx++, state==null?null:DBProcessingState.fromWorkflowInstanceState(state).key,java.sql.Types.INTEGER);
			selectStmt.setObject(pIdx++, state==null?null:DBProcessingState.fromWorkflowInstanceState(state).key,java.sql.Types.INTEGER);
			selectStmt.setObject(pIdx++, priority,java.sql.Types.INTEGER);
			selectStmt.setObject(pIdx++, priority,java.sql.Types.INTEGER);
			
			selectStmt.setFetchSize(100);
			
			ResultSet resultSet = selectStmt.executeQuery();
			
			ArrayList<WorkflowInstanceInfo> instances = new ArrayList<WorkflowInstanceInfo>();
			while (resultSet.next()) {
				WorkflowInstanceInfo workflowInstanceInfo = new WorkflowInstanceInfo();
				workflowInstanceInfo.setId(resultSet.getString(1));
				workflowInstanceInfo.setState(DBProcessingState.fromKey(resultSet.getInt(2)).asWorkflowInstanceState() );
				workflowInstanceInfo.setPriority(resultSet.getInt(3));
				workflowInstanceInfo.setLastActivityTimestamp(new Date(resultSet.getTimestamp(4).getTime()));
				workflowInstanceInfo.setProcessorPoolId(resultSet.getString(5));
				workflowInstanceInfo.setTimeout(resultSet.getTimestamp(6)!=null?new Date(resultSet.getTimestamp(6).getTime()):null);
				workflowInstanceInfo.setStartTime(new Date(resultSet.getTimestamp(7).getTime()));
				
//				workflowInstanceInfo.setErrorInfos(errorInfos);
//				workflowInstanceInfo.setFinishTime(finishTime);
//				workflowInstanceInfo.setLastErrorTime(lastErrorTime);
//				workflowInstanceInfo.setOverallLifetimeInMs(overallLifetimeInMs)
				instances.add(workflowInstanceInfo);
			}
			return instances;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			JdbcUtils.closeStatement(selectStmt);
		}
	}
}
