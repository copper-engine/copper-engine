package org.copperengine.core.persistent.cassandra;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.copperengine.core.util.Backchannel;
import org.copperengine.core.util.BackchannelDefaultImpl;
import org.copperengine.core.util.PojoDependencyInjector;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class UnitTestCassandraEngineFactory extends CassandraEngineFactory {

    public final Supplier<Backchannel> backchannel;
    public final Supplier<DummyResponseSender> dummyResponseSender;
    private final Supplier<ScheduledExecutorService> scheduledExecutorService;
    protected final boolean truncate;

    public UnitTestCassandraEngineFactory(boolean truncate) {
        super(Arrays.asList("org.copperengine.core.persistent.cassandra.workflows"));
        this.truncate = truncate;

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
        scheduledExecutorService = Suppliers.memoize(new Supplier<ScheduledExecutorService>() {
            @Override
            public ScheduledExecutorService get() {
                return Executors.newScheduledThreadPool(4);
            }
        });
        ((PojoDependencyInjector) dependencyInjector.get()).register("dummyResponseSender", dummyResponseSender.get());
        ((PojoDependencyInjector) dependencyInjector.get()).register("backchannel", backchannel.get());

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
    public void destroyEngine() {
        super.destroyEngine();
        scheduledExecutorService.get().shutdown();
    }

}
