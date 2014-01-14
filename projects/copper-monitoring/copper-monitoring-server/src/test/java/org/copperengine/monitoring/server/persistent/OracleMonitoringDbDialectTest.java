/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.server.persistent;

import static org.junit.Assert.assertFalse;

import java.beans.PropertyVetoException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.copperengine.core.EngineIdProviderBean;
import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.audit.DummyPostProcessor;
import org.copperengine.core.db.utility.JdbcUtils;
import org.copperengine.core.persistent.OracleDialect;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.junit.Ignore;
import org.junit.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Ignore
public class OracleMonitoringDbDialectTest extends MonitoringDbDialectTestBase {

    @Override
    void intit() {

        ComboPooledDataSource datasource_oracle = new ComboPooledDataSource();
        try {
            datasource_oracle.setDriverClass("oracle.jdbc.OracleDriver");
            datasource_oracle.setJdbcUrl("jdbc:oracle:thin:COPPER2/COPPER2@localhost:1521:HM");
            datasource_oracle.setMinPoolSize(1);
            datasource_oracle.setMaxPoolSize(8);
            datasource_oracle.setConnectionTesterClassName("org.copperengine.core.db.utility.oracle.c3p0.OracleConnectionTester");
            datasource_oracle.setConnectionCustomizerClassName("org.copperengine.core.db.utility.oracle.c3p0.OracleConnectionCustomizer");
            datasource_oracle.setIdleConnectionTestPeriod(15);
        } catch (PropertyVetoException e1) {
            throw new RuntimeException(e1);
        }
        this.datasource = datasource_oracle;

        cleanDB(datasource);

        final OracleDialect oracleDialect = new OracleDialect();
        // oracleDialect.setWfRepository(workflowRepository);
        oracleDialect.setDbBatchingLatencyMSec(0);
        oracleDialect.setEngineIdProvider(new EngineIdProviderBean("a"));
        oracleDialect.startup();
        this.databaseDialect = oracleDialect;

        OracleMonitoringDbDialect derbyMonitoringDbDialect = new OracleMonitoringDbDialect(new StandardJavaSerializer(), new DummyPostProcessor(), new BatchingAuditTrail());
        this.monitoringDbDialect = derbyMonitoringDbDialect;
    }

    @Test
    public void getRecommendationsReport() {

        try {
            PreparedStatement selectStmt = null;
            PreparedStatement identifyStmt = null;
            String sqlId = "";
            try {
                String sqltext = "SELECT /*+ MONITOR */  * FROM DUAL";
                selectStmt = datasource.getConnection().prepareStatement(sqltext);
                selectStmt.executeQuery();

                identifyStmt = datasource.getConnection().prepareStatement(
                        "select sql_id, plan_hash_value, exact_matching_signature, sql_plan_baseline from v$sql where sql_text = '" + sqltext + "'");
                ResultSet resultSet = identifyStmt.executeQuery();
                while (resultSet.next()) {
                    sqlId = resultSet.getString(1);
                }
                resultSet.close();

            } finally {
                JdbcUtils.closeStatement(selectStmt);
                JdbcUtils.closeStatement(identifyStmt);
            }

            String report = monitoringDbDialect.getRecommendationsReport(sqlId, datasource.getConnection());
            assertFalse(report.contains("java.sql.SQLSyntaxErrorException"));
            assertFalse(report.contains("java.sql.SQLException"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
