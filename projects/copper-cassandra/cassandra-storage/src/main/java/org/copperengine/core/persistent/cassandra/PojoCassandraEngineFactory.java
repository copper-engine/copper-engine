package org.copperengine.core.persistent.cassandra;

import java.util.List;

import org.copperengine.core.util.PojoDependencyInjector;

public class PojoCassandraEngineFactory extends CassandraEngineFactory<PojoDependencyInjector> {

    public PojoCassandraEngineFactory(final List<String> wfPackges, final List<String> cassandraHosts) {
        super(wfPackges);
        setCassandraHosts(cassandraHosts);
    }

    @Override
    protected PojoDependencyInjector createDependencyInjector() {
        return new PojoDependencyInjector();
    }

}
