package org.copperengine.core.persistent.cassandra.loadtest;

import java.util.Collections;
import java.util.List;

import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.copperengine.core.persistent.cassandra.TestData;
import org.copperengine.core.persistent.hybrid.DefaultTimeoutManager;
import org.copperengine.core.persistent.hybrid.HybridDBStorage;
import org.copperengine.core.persistent.hybrid.Storage;
import org.copperengine.ext.wfrepo.classpath.ClasspathWorkflowRepository;

public class DataCreator {

    public static void main(final String[] args) {
        final CassandraEngineFactory factory = new CassandraEngineFactory() {
            protected HybridDBStorage createStorage(ClasspathWorkflowRepository wfRepository, Storage cassandra) {
                return new HybridDBStorage(new StandardJavaSerializer(), wfRepository, cassandra, new DefaultTimeoutManager().startup(), executor) {
                    @Override
                    public List<Workflow<?>> dequeue(String ppoolId, int max) throws Exception {
                        return Collections.emptyList();
                    }
                };
            }
        };
        try {
            factory.createEngine(false);
            createData(factory.engine);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            factory.destroyEngine();
        }
    }

    private static void createData(PersistentScottyEngine engine) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4096; i++) {
            sb.append(i % 10);
        }
        final String payload = sb.toString();

        for (int i = 0; i < 500000; i++) {
            final String id = engine.createUUID();
            final TestData data = new TestData();
            data.id = id;
            data.someData = payload;
            final WorkflowInstanceDescr<TestData> wfInstanceDescr = new WorkflowInstanceDescr<TestData>("org.copperengine.core.persistent.cassandra.loadtest.workflows.LoadTestWorkflow", data, id, 1, null);
            engine.run(wfInstanceDescr);
        }
    }
}
