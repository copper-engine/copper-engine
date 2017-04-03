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

package org.copperengine.ext.wfrepo.classpath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.copperengine.core.WorkflowDescription;
import org.copperengine.core.WorkflowVersion;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.instrument.ClassInfo;
import org.copperengine.core.instrument.ScottyFindInterruptableMethodsVisitor;
import org.copperengine.core.wfrepo.AbstractWorkflowRepository;
import org.copperengine.core.wfrepo.Clazz;
import org.copperengine.core.wfrepo.FileBasedWorkflowRepository;
import org.copperengine.management.FileBasedWorkflowRepositoryMXBean;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

/**
 * Easy to use implementation of the {@link WorkflowRepository} interface.
 * This workflow repository looks for its workflows in the normal classpath, just one or more package names of those
 * packages containing the workflow classes need to be configured.
 * <p>
 * Compared to the {@link FileBasedWorkflowRepository} this is quite easy to use, because workflows do not need to
 * reside in a dedicated source folder, they don't need to be deployed as java source files and no java compiler (JDK)
 * is needed to run the application.
 * <P>
 * On the other hand there is no hot deployment feature and the JMX interface does not show the java source code of the
 * deployed workflows. Anyhow, this workflow repo is probably suitable for most simple COPPER applications.
 * <p>
 * See ClasspathWorkflowRepositoryTest.java testExec() for sample usage.
 * 
 * <pre>
 * {
 *     final ClasspathWorkflowRepository wfRepo = new ClasspathWorkflowRepository(&quot;org.copperengine.ext.wfrepo.classpath.testworkflows&quot;);
 *     final TransientEngineFactory factory = new TransientEngineFactory() {
 *         protected WorkflowRepository createWorkflowRepository() {
 *             return wfRepo;
 *         }
 * 
 *         protected File getWorkflowSourceDirectory() {
 *             return null;
 *         }
 *     };
 *     TransientScottyEngine engine = factory.create();
 *     engine.run(&quot;org.copperengine.ext.wfrepo.classpath.testworkflows.TestWorkflowThree&quot;, &quot;foo&quot;);
 * }
 * </pre>
 *
 * 
 * @author austermann
 *
 */
public class ClasspathWorkflowRepository extends AbstractWorkflowRepository implements WorkflowRepository, FileBasedWorkflowRepositoryMXBean {

    private static final Logger logger = LoggerFactory.getLogger(ClasspathWorkflowRepository.class);

    private final List<String> wfPackages;

    private File adaptedTargetDir;
    private VolatileState volatileState;

    public ClasspathWorkflowRepository(final String wfPackage) {
        this(Collections.singletonList(wfPackage));
    }

    public ClasspathWorkflowRepository(final List<String> wfPackages) {
        this.wfPackages = new ArrayList<String>(wfPackages);
    }

    @Override
    public synchronized void start() {
        try {
            if (adaptedTargetDir != null) {
                return;
            }
            logger.info("Starting up with wfPackages={}", wfPackages);
            final ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            adaptedTargetDir = new File(System.getProperty("java.io.tmpdir") + "/cpwfrepo" + System.currentTimeMillis());
            logger.info("adaptedTargetDir={}", adaptedTargetDir);
            adaptedTargetDir.mkdirs();
            final Set<Class<?>> wfSet = findWorkflowClasses(wfPackages, tcl);
            logger.info("wfSet.size={}", wfSet.size());
            final Map<String, Clazz> clazzMap = findInterruptableMethods(wfSet, tcl);
            final Map<String, ClassInfo> classInfos = new HashMap<String, ClassInfo>();
            instrumentWorkflows(adaptedTargetDir, clazzMap, classInfos, tcl);

            for (Clazz clazz : clazzMap.values()) {
                ClassInfo info = classInfos.get(clazz.classname);
                if (info != null) {
                    ClassInfo superClassInfo = classInfos.get(clazz.superClassname);
                    info.setSuperClassInfo(superClassInfo);
                }
            }
            final Map<String, Class<?>> map = new HashMap<String, Class<?>>();
            final ClassLoader cl = super.createClassLoader(map, adaptedTargetDir, adaptedTargetDir, clazzMap);

            checkConstraints(map);

            final Map<String, Class<?>> wfClassMap = new HashMap<String, Class<?>>(map);
            final Map<String, Class<?>> wfMapLatest = new HashMap<String, Class<?>>(map.size());
            final Map<String, Class<?>> wfMapVersioned = new HashMap<String, Class<?>>(map.size());
            final Map<String, WorkflowVersion> latest = new HashMap<String, WorkflowVersion>(map.size());
            final Map<String, List<WorkflowVersion>> versions = new HashMap<String, List<WorkflowVersion>>();
            for (Class<?> wfClass : map.values()) {
                wfMapLatest.put(wfClass.getName(), wfClass); // workflow is always accessible by its name

                WorkflowDescription wfDesc = wfClass.getAnnotation(WorkflowDescription.class);
                if (wfDesc != null) {
                    final String alias = wfDesc.alias();
                    final WorkflowVersion version = new WorkflowVersion(wfDesc.majorVersion(), wfDesc.minorVersion(), wfDesc.patchLevelVersion());
                    wfMapVersioned.put(createAliasName(alias, version), wfClass);

                    WorkflowVersion existingLatest = latest.get(alias);
                    if (existingLatest == null || version.isLargerThan(existingLatest)) {
                        wfMapLatest.put(alias, wfClass);
                        latest.put(alias, version);
                    }

                    List<WorkflowVersion> versionsList = versions.get(alias);
                    if (versionsList == null) {
                        versionsList = new ArrayList<WorkflowVersion>();
                        versions.put(alias, versionsList);
                    }
                    versionsList.add(version);
                }
            }
            for (List<WorkflowVersion> vl : versions.values()) {
                Collections.sort(vl, new WorkflowVersion.Comparator());
            }
            if (logger.isTraceEnabled()) {
                for (Map.Entry<String, Class<?>> e : wfMapLatest.entrySet()) {
                    logger.trace("wfMapLatest.key={}, class={}", e.getKey(), e.getValue().getName());
                }
                for (Map.Entry<String, Class<?>> e : wfMapVersioned.entrySet()) {
                    logger.trace("wfMapVersioned.key={}, class={}", e.getKey(), e.getValue().getName());
                }
            }

            volatileState = new VolatileState(wfMapLatest, wfMapVersioned, versions, cl, 0L, wfClassMap, Collections.<String, String>emptyMap(), classInfos, createWorkflowClassInfoMap(wfMapLatest, Collections.<String, String>emptyMap()));

            logger.info("Startup finished");
        } catch (Exception e) {
            logger.error("startup failed", e);
            throw new Error("Startup failed", e);
        }
    }

    @Override
    public synchronized void shutdown() {
        try {
            if (adaptedTargetDir != null) {
                FileUtils.deleteDirectory(adaptedTargetDir);
                adaptedTargetDir = null;
            }
        } catch (IOException e) {
            logger.warn("Unable to delete directory {}", adaptedTargetDir);
        }
    }

    static Set<Class<?>> findWorkflowClasses(final List<String> wfPackages, final ClassLoader cl) throws Exception {
        final ClassPath cp = ClassPath.from(cl);
        final Set<Class<?>> set = new HashSet<Class<?>>();
        for (String wfPackage : wfPackages) {
            final ImmutableSet<com.google.common.reflect.ClassPath.ClassInfo> x = cp.getTopLevelClassesRecursive(wfPackage);
            for (com.google.common.reflect.ClassPath.ClassInfo ci : x) {
                final Class<?> c = cl.loadClass(ci.getName());
                set.add(c);
                set.addAll(Arrays.asList(c.getDeclaredClasses()));
                loadAnonymousInnerClasses(cl, set, c);
            }
        }
        return set;
    }

    private static void loadAnonymousInnerClasses(final ClassLoader cl, final Set<Class<?>> set, final Class<?> c) {
        // TODO Austermann, 19.10.2015 - find a better way to load the anonymous inner classes - this is just a work
        // arround
        try {
            for (int i = 1; i < Integer.MAX_VALUE; i++) {
                final String anonymousInnerclassName = c.getName() + "$" + i;
                set.add(cl.loadClass(anonymousInnerclassName));
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
    }

    private Map<String, Clazz> findInterruptableMethods(final Set<Class<?>> wfSet, ClassLoader cl) throws IOException {
        logger.info("Analysing classfiles");
        // Find and visit all classes
        Map<String, Clazz> clazzMap = new HashMap<String, Clazz>();
        for (Class<?> c : wfSet) {
            logger.info("analysing class {}", c.getName());
            ScottyFindInterruptableMethodsVisitor visitor = new ScottyFindInterruptableMethodsVisitor();
            URL url = cl.getResource(c.getName().replace(".", "/") + ".class");
            InputStream is = url.openStream();
            try {
                ClassReader cr = new ClassReader(is);
                cr.accept(visitor, 0);
            } finally {
                is.close();
            }
            Clazz clazz = new Clazz();
            clazz.interruptableMethods = visitor.getInterruptableMethods();
            clazz.classfile = url;
            clazz.classname = visitor.getClassname();
            clazz.superClassname = visitor.getSuperClassname();
            clazzMap.put(clazz.classname, clazz);
        }

        // Remove all classes that are no workflow
        List<String> allClassNames = new ArrayList<String>(clazzMap.keySet());
        for (String classname : allClassNames) {
            Clazz clazz = clazzMap.get(classname);
            Clazz startClazz = clazz;
            while (true) {
                startClazz.aggregatedInterruptableMethods.addAll(clazz.interruptableMethods);
                if ("org/copperengine/core/Workflow".equals(clazz.superClassname) || "org/copperengine/core/persistent/PersistentWorkflow".equals(clazz.superClassname)) {
                    break;
                }
                clazz = clazzMap.get(clazz.superClassname);
                if (clazz == null) {
                    break;
                }
            }
            if (clazz == null) {
                // this is no workflow
                clazzMap.remove(classname);
                final URL url = cl.getResource(classname.replace(".", "/") + ".class");
                final File target = new File(adaptedTargetDir, classname.replace(".", "/") + ".class");
                target.getParentFile().mkdirs();

                FileUtils.copyURLToFile(url, target);
            }
        }

        return clazzMap;
    }

    @Override
    public String getDescription() {
        return this.getClass().getSimpleName();
    }

    @Override
    public List<String> getSourceDirs() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getSourceArchiveUrls() {
        return Collections.emptyList();
    }

    @Override
    protected VolatileState getVolatileState() {
        return volatileState;
    }

}
