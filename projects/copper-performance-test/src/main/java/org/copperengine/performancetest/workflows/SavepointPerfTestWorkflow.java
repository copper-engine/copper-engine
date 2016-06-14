package org.copperengine.performancetest.workflows;

import java.util.concurrent.TimeUnit;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.core.util.Backchannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavepointPerfTestWorkflow extends PersistentWorkflow<String> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(SavepointPerfTestWorkflow.class);

    private transient RuntimeStatisticsCollector statisticsCollector;
    private transient Backchannel backchannel;

    @AutoWire
    public void setBackchannel(Backchannel backchannel) {
        this.backchannel = backchannel;
    }

    @AutoWire
    public void setStatisticsCollector(RuntimeStatisticsCollector statisticsCollector) {
        this.statisticsCollector = statisticsCollector;
    }

    @Override
    public void main() throws Interrupt {
        logger.debug("Starting....");
        for (int i = 0; i < 10; i++) {
            final long startTS = System.nanoTime();
            savepoint();
            final long etNanos = System.nanoTime() - startTS;
            statisticsCollector.submit("savepoint.latency", 1, etNanos, TimeUnit.NANOSECONDS);
            logger.debug("Savepoint took {} msec", (double) etNanos / 1000000.0d);
        }
        logger.debug("Finished!");
        backchannel.notify(getId(), getId());
    }
}
