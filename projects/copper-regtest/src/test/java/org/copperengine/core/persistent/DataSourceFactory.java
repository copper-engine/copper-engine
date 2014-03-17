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
package org.copperengine.core.persistent;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DataSourceFactory {

    public static ComboPooledDataSource createOracleDatasource() {
        try {
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            dataSource.setJdbcUrl("jdbc:oracle:thin:COPPER2/COPPER2@localhost:1521:orcl11g");
            dataSource.setDriverClass("oracle.jdbc.OracleDriver");
            dataSource.setMinPoolSize(1);
            dataSource.setMaxPoolSize(8);
            dataSource.setPreferredTestQuery("SELECT 1 FROM DUAL");
            return dataSource;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("createOracleDatasource failed", e);
        }
    }

    public static ComboPooledDataSource createMySqlDatasource() {
        try {
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            dataSource.setJdbcUrl("jdbc:mysql://localhost/COPPER2");
            dataSource.setDriverClass("com.mysql.jdbc.Driver");
            dataSource.setMinPoolSize(1);
            dataSource.setMaxPoolSize(8);
            dataSource.setUser("root");
            dataSource.setPassword("geheim");
            return dataSource;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("createMySqlDatasource failed", e);
        }
    }

    public static ComboPooledDataSource createPostgresDatasource() {
        try {
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
            dataSource.setDriverClass("com.mysql.jdbc.Driver");
            dataSource.setMinPoolSize(1);
            dataSource.setMaxPoolSize(8);
            dataSource.setUser("copper");
            dataSource.setPassword("copper4711");
            return dataSource;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("createPostgresDatasource failed", e);
        }
    }

    public static ComboPooledDataSource createDerbyDbDatasource() {
        try {
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            // dataSource.setJdbcUrl("jdbc:derby:./build/copperUnitTestDB;create=true");
            dataSource.setJdbcUrl("jdbc:derby:memory:copperUnitTestDB" + System.currentTimeMillis() + ";create=true");
            dataSource.setDriverClass("org.apache.derby.jdbc.EmbeddedDriver");
            dataSource.setMinPoolSize(1);
            dataSource.setMaxPoolSize(1);
            DerbyDbDialect.checkAndCreateSchema(dataSource);
            return dataSource;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("createDerbyDbDatasource failed", e);
        }
    }

    public static ComboPooledDataSource createH2Datasource() {
        try {
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            // dataSource.setJdbcUrl("jdbc:h2:./build/copperUnitTestH2DB/db;MVCC=TRUE;AUTO_SERVER=TRUE");
            dataSource.setJdbcUrl("jdbc:h2:mem:copperUnitTestH2DB;MVCC=TRUE");
            dataSource.setDriverClass("org.h2.Driver");
            dataSource.setMinPoolSize(1);
            dataSource.setMaxPoolSize(8);
            H2Dialect.checkAndCreateSchema(dataSource);
            return dataSource;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("createH2Datasource failed", e);
        }
    }
}
