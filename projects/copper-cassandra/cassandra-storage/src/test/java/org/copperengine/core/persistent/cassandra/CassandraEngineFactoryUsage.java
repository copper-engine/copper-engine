package org.copperengine.core.persistent.cassandra;

import java.util.Arrays;

import org.copperengine.core.util.PojoDependencyInjector;
import org.junit.Test;

public class CassandraEngineFactoryUsage {

    @Test
    public void test() {
        CassandraEngineFactory<PojoDependencyInjector> engineFactory = new CassandraEngineFactory<PojoDependencyInjector>(Arrays.asList("package.of.copper.workflow.classes")) {
            @Override
            protected PojoDependencyInjector createDependencyInjector() {
                return new PojoDependencyInjector();
            }
        };
        engineFactory.getEngine().startup();
    }

}
