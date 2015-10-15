/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.monitoring.example;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.copperengine.core.persistent.DatabaseDialect;
import org.copperengine.monitoring.example.util.DerbyCleanDbUtil;


public class DatabaseUtil {
    public final DatabaseDialect databaseDialect;
    public final DataSource dataSource;

    public DatabaseUtil(DatabaseDialect databaseDialect, DataSource dataSource) {
        super();
        this.databaseDialect = databaseDialect;
        this.dataSource = dataSource;
    }

    public void cleanDearbyDB(){
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            DerbyCleanDbUtil.dropSchema(connection.getMetaData(), "APP");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
