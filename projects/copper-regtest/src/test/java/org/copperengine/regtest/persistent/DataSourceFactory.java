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
package org.copperengine.regtest.persistent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.copperengine.core.persistent.DerbyDbDialect;
import org.copperengine.core.persistent.H2Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DataSourceFactory {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactory.class);

    private static Properties createProperties() {
        try {
            Properties defaults = new Properties();
            logger.info("Loading properties from 'regtest.default.properties'...");
            defaults.load(DataSourceFactory.class.getResourceAsStream("/regtest.default.properties"));

            Properties specific = new Properties();
            String username = System.getProperty("user.name", "undefined");
            InputStream is = DataSourceFactory.class.getResourceAsStream("/regtest." + username + ".properties");
            if (is != null) {
                logger.info("Loading properties from 'regtest." + username + ".properties'...");
                specific.load(is);
            }

            Properties p = new Properties();
            p.putAll(defaults);
            p.putAll(specific);

            List<String> keys = new ArrayList<>();
            for (Object key : p.keySet()) {
                keys.add(key.toString());
            }
            Collections.sort(keys);
            for (String key : keys) {
                logger.info("Property {}='{}'", key, p.getProperty(key));
            }
            return p;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("failed to load properties", e);
        }
    }

    private static final Properties props = createProperties();

    private static ComboPooledDataSource createDataSource(final String propertyPrefix) {
        try {
            final boolean active = Boolean.parseBoolean(props.getProperty(propertyPrefix + "active", "false"));
            final String jdbcUrl = trim(props.getProperty(propertyPrefix + "jdbcURL"));
            final String user = trim(props.getProperty(propertyPrefix + "user"));
            final String password = trim(props.getProperty(propertyPrefix + "password"));
            final String driverClass = trim(props.getProperty(propertyPrefix + "driverClass"));
            final String preferredTestQuery = props.getProperty(propertyPrefix + "preferredTestQuery");
            final int minPoolSize = Integer.parseInt(props.getProperty(propertyPrefix + "minPoolSize", "1"));
            final int maxPoolSize = Integer.parseInt(props.getProperty(propertyPrefix + "maxPoolSize", "8"));
            if (active) {
                ComboPooledDataSource ds = new ComboPooledDataSource();
                ds.setJdbcUrl(jdbcUrl.replace("${NOW}", Long.toString(System.currentTimeMillis())));
                if (!isNullOrEmpty(user))
                    ds.setUser(user);
                if (!isNullOrEmpty(password))
                    ds.setPassword(password);
                if (!isNullOrEmpty(driverClass))
                    ds.setDriverClass(driverClass);
                if (!isNullOrEmpty(preferredTestQuery))
                    ds.setPreferredTestQuery(preferredTestQuery);
                ds.setMinPoolSize(minPoolSize);
                ds.setMaxPoolSize(maxPoolSize);
                return ds;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create datasource for prefix '" + propertyPrefix + "'", e);
        }
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    public static ComboPooledDataSource createOracleDatasource() {
        return createDataSource("copper.regtest.datasource.oracle.");
    }

    public static ComboPooledDataSource createMySqlDatasource() {
        return createDataSource("copper.regtest.datasource.mysql.");
    }

    public static ComboPooledDataSource createPostgresDatasource() {
        return createDataSource("copper.regtest.datasource.postgres.");
    }

    public static ComboPooledDataSource createDerbyDbDatasource() {
        try {
            ComboPooledDataSource dataSource = createDataSource("copper.regtest.datasource.derby.");
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
            ComboPooledDataSource dataSource = createDataSource("copper.regtest.datasource.h2.");
            H2Dialect.checkAndCreateSchema(dataSource);
            return dataSource;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("createH2Datasource failed", e);
        }
    }

    public static ComboPooledDataSource createOracleSimpleDatasource() {
        return createDataSource("copper.regtest.datasource.oracle_simple.");
    }

}
