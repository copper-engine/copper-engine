package org.copperengine.core.persistent.lock;

import org.copperengine.core.persistent.DataSourceFactory;
import org.junit.Assert;
import org.junit.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class PersistentLockManagerDialectPostgresTest extends AbstractPersistentLockManagerDialectTest {

    @Override
    protected ComboPooledDataSource createDatasource() {
        return DataSourceFactory.createPostgresDatasource();
    }

    @Override
    protected PersistentLockManagerDialect createImplementation() {
        return new PersistentLockManagerDialectPostgres();
    }

    @Test
    public void testSupportsMultipleInstances() {
        Assert.assertFalse(createImplementation().supportsMultipleInstances());
    }

}
