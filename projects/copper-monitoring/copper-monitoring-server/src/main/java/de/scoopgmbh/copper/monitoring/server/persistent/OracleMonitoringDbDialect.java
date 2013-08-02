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
package de.scoopgmbh.copper.monitoring.server.persistent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;

import com.google.common.base.Throwables;

import de.scoopgmbh.copper.audit.BatchingAuditTrail;
import de.scoopgmbh.copper.audit.MessagePostProcessor;
import de.scoopgmbh.copper.persistent.DatabaseDialect;
import de.scoopgmbh.copper.persistent.Serializer;

/**
 * Oracle implementation of the {@link DatabaseDialect} interface
 * 
 * @author austermann
 *
 */
public class OracleMonitoringDbDialect extends BaseDatabaseMonitoringDialect {
	
	private static final Logger logger = LoggerFactory.getLogger(OracleMonitoringDbDialect.class);
	
	public OracleMonitoringDbDialect(Serializer serializer, MessagePostProcessor messagePostProcessor,BatchingAuditTrail auditTrail) {
		super(serializer, messagePostProcessor,auditTrail);
	}

	@Override
	public String getResultLimitingQuery(String query, long limit) {
		return "SELECT * FROM (\n" + 
							query+ 
				"			\n)\n" + 
				"			WHERE rownum <= "+limit;
	}
	
	@Override
	protected String createWorkflowInstanceListQuery(){
		return "SELECT ID,STATE,PRIORITY,LAST_MOD_TS,PPOOL_ID,TIMEOUT,CREATION_TS,\n" + 
				"       ERR.\"EXCEPTION\",       ERR.ERROR_TS,  LAST_MOD_TS FINISHED_TS, CLASSNAME\n" + 
				"FROM COP_WORKFLOW_INSTANCE  MASTER, (select WORKFLOW_INSTANCE_ID, MAX(ROWID) keep (dense_rank last ORDER BY ERROR_TS) \"RID\" from COP_WORKFLOW_INSTANCE_ERROR GROUP BY WORKFLOW_INSTANCE_ID) ERR_RID, COP_WORKFLOW_INSTANCE_ERROR ERR\n" + 
				"WHERE\r\n" + 
				"	(? is null or PPOOL_ID=?) AND \n" + 
				"	(? is null or CLASSNAME like ?) AND \n" + 
				"	(? is null or STATE=?) AND \n" + 
				"	(? is null or CREATION_TS>=?) AND \n" + 
				"	(? is null or CREATION_TS<=?) AND \n" + 
				"	(? is null or ID<=?) AND \n" +
				"	(? is null or PRIORITY=?) AND\n" + 
				"	ERR_RID.WORKFLOW_INSTANCE_ID(+) = MASTER.ID AND\n" + 
				"	ERR.ROWID(+) = ERR_RID.RID";
	}
	
	@Override
	protected String getSelectMessagesQuery(boolean ignoreProcceded) {
		return "SELECT CORRELATION_ID, r.response, r.long_response, RESPONSE_TS, RESPONSE_TIMEOUT FROM COP_RESPONSE r "
				+(!ignoreProcceded?"":"WHERE not exists(select * from cop_wait w where r.CORRELATION_ID=w.CORRELATION_ID)");
	}
	
	
	
	
	@Override
	public String selectDatabaseMonitoringHtmlReport(Connection con) {
		PreparedStatement selectStmt = null;
		try {
			ResultSet resultSet = con.createStatement().executeQuery("SELECT DBMS_SQLTUNE.report_sql_monitor_list( \r\n" + 
					"					  type         => 'HTML', \r\n" + 
					"					  report_level => 'ALL') AS report \r\n" + 
					"					 FROM dual");
			
			String result="";
			while (resultSet.next()) {
				result = resultSet.getString(1);
			}
			resultSet.close();

			return result;
		} catch (SQLException e) {
			logger.error("", e);
			return crearteErrorMessage(e);
		} finally {
			JdbcUtils.closeStatement(selectStmt);
		}
	}
	
	private String crearteErrorMessage(Throwable e){
		String text="for exception table not found you probably missing grants:" +
			"you can try:" +
			"GRANT ADVISOR TO COPPER2;\n" + 
			"GRANT SELECT_CATALOG_ROLE TO COPPER2;\n" + 
			"GRANT EXECUTE ON DBMS_SQLTUNE TO COPPER2\n;" +
			"\n" +
			"GRANT SELECT ANY DICTIONARY TO COPPER2\n" +
			"\n" +
			"or\n" +
			"\n" +
			"GRANT select on sys.dba_hist_sqltext to COPPER2;\n" + 
			"GRANT select on sys.dba_hist_sqlstat to COPPER2;\n" + 
			"GRANT select on sys.dba_hist_sqlbind to COPPER2;\n" + 
			"GRANT select on sys.dba_hist_optimizer_env to COPPER2;\n" + 
			"GRANT select on sys.dba_hist_snapshot to COPPER2;\n" + 
			"GRANT select on sys.dba_advisor_tasks to COPPER2;\n" + 
			"GRANT select on dba_hist_active_sess_history to COPPER2;\n" + 
			"GRANT select on v_$sql to COPPER2;\r\n" + 
			"GRANT select on v_$sql_bind_capture to COPPER2;\n" + 
			"GRANT select on v_$sqlarea_plan_hash to COPPER2;\n" + 
			"GRANT select on sys.v_$database to COPPER2;\n" + 
			"GRANT select on sys.v_$instance to COPPER2;" +
			
			"\n\n"+Throwables.getStackTraceAsString(e);
		return "<code>"+text.replace("\n", "<br/>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")+"</code>";
	}

	@Override
	public String selectDatabaseMonitoringHtmlDetailReport(String sqlid, Connection con) {
		PreparedStatement selectStmt = null;
		try {
			selectStmt = con.prepareStatement(
					"SELECT DBMS_SQLTUNE.report_sql_monitor(\n" + 
					"  sql_id       => ?,\n" + 
					"  type         => 'HTML',\n" + 
					"  report_level => 'ALL') AS report FROM dual"
					);
			selectStmt.setString(1, sqlid);
			ResultSet resultSet = selectStmt.executeQuery();
			
			String result="";
			while (resultSet.next()) {
				result = resultSet.getString(1);
			}
			resultSet.close();

			return result;
		} catch (SQLException e) {
			logger.error("", e);
			return crearteErrorMessage(e);
		} finally {
			JdbcUtils.closeStatement(selectStmt);
		}
	}
	
	@Override
	public String getRecommendationsReport(String sqlid, Connection con) {
		PreparedStatement selectStmt = null;
		try {
			selectStmt = con.prepareStatement(
					"DECLARE\r\n" + 
					"l_sql_tune_task_id VARCHAR2(100);\r\n" + 
					"BEGIN\r\n" + 
					"l_sql_tune_task_id := DBMS_SQLTUNE.create_tuning_task (\r\n" + 
					"sql_id => ?,\r\n" + 
					"scope => DBMS_SQLTUNE.scope_comprehensive,\r\n" + 
					"time_limit => 60,\r\n" + 
					"task_name => 'AWR_tuning_task',\r\n" + 
					"description => 'Tuning task for statement 19v5guvsgcd1v in AWR.');\r\n" + 
					"\r\n" + 
					"END;"  
					);
			selectStmt.setString(1, sqlid);
			selectStmt.execute();
			JdbcUtils.closeStatement(selectStmt);
			
			con.createStatement().execute("call DBMS_SQLTUNE.execute_tuning_task(task_name => 'AWR_tuning_task')");
			
			selectStmt = con.prepareStatement("SELECT DBMS_SQLTUNE.report_tuning_task('AWR_tuning_task') AS recommendations FROM dual");
			selectStmt.executeQuery();
			ResultSet resultSet = selectStmt.executeQuery();
		
			selectStmt = con.prepareStatement("call DBMS_SQLTUNE.drop_tuning_task (task_name => 'AWR_tuning_task')");
			selectStmt.executeQuery();
			
			String result="";
			while (resultSet.next()) {
				result = resultSet.getString(1);
			}
			resultSet.close();

			return result;
		} catch (SQLException e) {
			logger.error("", e);
			return crearteErrorMessage(e);
		} finally {
			JdbcUtils.closeStatement(selectStmt);
		}
	}

}
