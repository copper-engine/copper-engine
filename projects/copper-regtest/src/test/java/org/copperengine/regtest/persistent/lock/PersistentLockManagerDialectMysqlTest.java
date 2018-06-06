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
package org.copperengine.regtest.persistent.lock;

import org.copperengine.core.persistent.lock.PersistentLockManagerDialect;
import org.copperengine.core.persistent.lock.PersistentLockManagerDialectSQL;
import org.copperengine.regtest.persistent.DataSourceFactory;
import org.copperengine.regtest.test.persistent.DataSourceType;
import org.copperengine.regtest.test.persistent.PersistentEngineTestContext;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class PersistentLockManagerDialectMysqlTest extends AbstractPersistentLockManagerDialectTest {

    private static boolean dbmsAvailable = false;
    static {
        dbmsAvailable = new PersistentEngineTestContext(DataSourceType.MySQL, false).isDbmsAvailable();
    }

    @Override
    protected ComboPooledDataSource createDatasource() {
        return DataSourceFactory.createMySqlDatasource();
    }

    @Override
    protected PersistentLockManagerDialect createImplementation() {
        return new PersistentLockManagerDialectSQL();
    }

    @Test
    public void testSupportsMultipleInstances() {
        Assume.assumeFalse(skipTests());
        Assert.assertFalse(createImplementation().supportsMultipleInstances());
    }

    @Override
    protected boolean skipTests() {
        return !dbmsAvailable;
    }
}
