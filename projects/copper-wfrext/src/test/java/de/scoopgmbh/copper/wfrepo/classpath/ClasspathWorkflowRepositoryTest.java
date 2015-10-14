package de.scoopgmbh.copper.wfrepo.classpath;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.tranzient.TransientEngineFactory;
import org.copperengine.core.tranzient.TransientScottyEngine;
import org.junit.Assert;
import org.junit.Test;

public class ClasspathWorkflowRepositoryTest {

    private static final String TESTWORKFLOWS_PACKAGE = "org.copperengine.core.wfrepo.testworkflows";

    @Test
    public void testFindWorkflowClasses() throws Exception {
        Set<Class<?>> set = ClasspathWorkflowRepository.findWorkflowClasses(Collections.singletonList(TESTWORKFLOWS_PACKAGE), Thread.currentThread().getContextClassLoader());
        Assert.assertEquals(4, set.size());
        Assert.assertNotNull(set.contains(Class.forName("org.copperengine.core.wfrepo.testworkflows.TestWorkflowThree")));
    }

    @Test
    public void testExec() throws Exception {
        final ClasspathWorkflowRepository wfRepo = new ClasspathWorkflowRepository(TESTWORKFLOWS_PACKAGE);
        try {
            final TransientEngineFactory factory = new TransientEngineFactory() {
                @Override
                protected WorkflowRepository createWorkflowRepository() {
                    return wfRepo;
                }

                @Override
                protected File getWorkflowSourceDirectory() {
                    return null;
                }
            };
            TransientScottyEngine engine = factory.create();
            engine.run("org.copperengine.core.wfrepo.testworkflows.TestWorkflowThree", "foo");
            Thread.sleep(1000);
        } finally {
            // wfRepo.shutdown();
        }
    }
}
