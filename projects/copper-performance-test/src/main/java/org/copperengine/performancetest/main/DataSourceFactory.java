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
package org.copperengine.performancetest.main;

import java.util.Properties;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DataSourceFactory {

    public static ComboPooledDataSource createDataSource(Properties props) {
        try {
            final String jdbcUrl = trim(props.getProperty(ConfigParameter.DS_JDBC_URL.getKey()));
            final String user = trim(props.getProperty(ConfigParameter.DS_USER.getKey()));
            final String password = trim(props.getProperty(ConfigParameter.DS_PASSWORD.getKey()));
            final String driverClass = trim(props.getProperty(ConfigParameter.DS_DRIVER_CLASS.getKey()));
            final int minPoolSize = Integer.valueOf(props.getProperty(ConfigParameter.DS_MIN_POOL_SIZE.getKey(), Integer.toString(Runtime.getRuntime().availableProcessors())));
            final int maxPoolSize = Integer.valueOf(props.getProperty(ConfigParameter.DS_MAX_POOL_SIZE.getKey(), Integer.toString(2 * Runtime.getRuntime().availableProcessors())));
            ComboPooledDataSource ds = new ComboPooledDataSource();
            ds.setJdbcUrl(jdbcUrl.replace("${NOW}", Long.toString(System.currentTimeMillis())));
            if (!isNullOrEmpty(user))
                ds.setUser(user);
            if (!isNullOrEmpty(password))
                ds.setPassword(password);
            if (!isNullOrEmpty(driverClass))
                ds.setDriverClass(driverClass);
            ds.setMinPoolSize(minPoolSize);
            ds.setInitialPoolSize(minPoolSize);
            ds.setMaxPoolSize(maxPoolSize);
            return ds;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create datasource", e);
        }
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

}
