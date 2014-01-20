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

import java.sql.Connection;

import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.audit.DummyPostProcessor;
import org.copperengine.core.db.utility.JdbcUtils;
import org.copperengine.core.persistent.H2Dialect;
import org.copperengine.core.persistent.StandardJavaSerializer;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class H2MonitoringDbDialectTest extends MonitoringDbDialectTestBase {

    @Override
    void intit() {
        ComboPooledDataSource datasource = new ComboPooledDataSource();
        datasource.setJdbcUrl("jdbc:h2:./build/copperUnitTestH2DB/db;MVCC=TRUE;AUTO_SERVER=TRUE");
        this.datasource = datasource;

        H2MonitoringDbDialect derbyMonitoringDbDialect = new H2MonitoringDbDialect(new StandardJavaSerializer(), new DummyPostProcessor(), new BatchingAuditTrail());
        this.monitoringDbDialect = derbyMonitoringDbDialect;

        H2Dialect databaseDialect = new H2Dialect();
        databaseDialect.setDataSource(datasource);
        databaseDialect.startup();
        this.databaseDialect = databaseDialect;

        Connection connection = null;
        try {
            connection = datasource.getConnection();
            connection.setAutoCommit(false);
            H2Dialect.dropSchema(datasource);
            H2Dialect.checkAndCreateSchema(datasource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.closeConnection(connection);
        }
    }

}
