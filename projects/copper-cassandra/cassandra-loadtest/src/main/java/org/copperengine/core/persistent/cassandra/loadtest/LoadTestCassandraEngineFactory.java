/**
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
package org.copperengine.core.persistent.cassandra.loadtest;

import java.util.Arrays;

import org.copperengine.core.persistent.cassandra.CassandraSessionManager;
import org.copperengine.core.util.Backchannel;
import org.copperengine.core.util.BackchannelDefaultImpl;
import org.copperengine.core.util.PojoDependencyInjector;
import org.copperengine.ext.util.Supplier2Provider;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class LoadTestCassandraEngineFactory extends org.copperengine.core.persistent.cassandra.CassandraEngineFactory<PojoDependencyInjector> {

    public final Supplier<Backchannel> backchannel;
    public final Supplier<DummyResponseSender> dummyResponseSender;
    protected final boolean truncate = false;

    public LoadTestCassandraEngineFactory() {
        super(Arrays.asList("org.copperengine.core.persistent.cassandra.loadtest.workflows"));
        super.setCassandraHosts(Arrays.asList("nuc1.scoop-gmbh.de"));

        backchannel = Suppliers.memoize(new Supplier<Backchannel>() {
            @Override
            public Backchannel get() {
                return new BackchannelDefaultImpl();
            }
        });
        dummyResponseSender = Suppliers.memoize(new Supplier<DummyResponseSender>() {
            @Override
            public DummyResponseSender get() {
                return new DummyResponseSender(scheduledExecutorService.get(), engine.get());
            }
        });
        dependencyInjector.get().register("dummyResponseSender", new Supplier2Provider<>(dummyResponseSender));
        dependencyInjector.get().register("backchannel", new Supplier2Provider<>(backchannel));
    }

    @Override
    protected CassandraSessionManager createCassandraSessionManager() {
        final CassandraSessionManager csm = super.createCassandraSessionManager();
        if (truncate) {
            csm.getSession().execute("truncate COP_WORKFLOW_INSTANCE");
            csm.getSession().execute("truncate COP_EARLY_RESPONSE");
            csm.getSession().execute("truncate COP_WFI_ID");
        }
        return csm;
    }

    @Override
    protected PojoDependencyInjector createDependencyInjector() {
        return new PojoDependencyInjector();
    }

    public Backchannel getBackchannel() {
        return backchannel.get();
    }

}
