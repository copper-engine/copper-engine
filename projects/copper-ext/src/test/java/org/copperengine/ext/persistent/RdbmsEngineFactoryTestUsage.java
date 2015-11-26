package org.copperengine.ext.persistent;

import java.util.Arrays;

import javax.sql.DataSource;

import org.copperengine.core.util.PojoDependencyInjector;

public class RdbmsEngineFactoryTestUsage {

    public void testUsage() {
        RdbmsEngineFactory<PojoDependencyInjector> engineFactory = new RdbmsEngineFactory<PojoDependencyInjector>(Arrays.asList("package.of.copper.workflow.classes")) {
            @Override
            protected PojoDependencyInjector createDependencyInjector() {
                return new PojoDependencyInjector();
            }

            @Override
            protected DataSource createDataSource() {
                return null;
            }
        };
        engineFactory.getEngine().startup();

    }

}
