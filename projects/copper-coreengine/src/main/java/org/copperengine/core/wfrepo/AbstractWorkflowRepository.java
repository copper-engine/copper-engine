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
package org.copperengine.core.wfrepo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowDescription;
import org.copperengine.core.WorkflowFactory;
import org.copperengine.core.WorkflowVersion;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.instrument.ClassInfo;
import org.copperengine.core.instrument.ScottyClassAdapter;
import org.copperengine.core.instrument.Transformed;
import org.copperengine.core.instrument.TryCatchBlockHandler;
import org.copperengine.management.FileBasedWorkflowRepositoryMXBean;
import org.copperengine.management.model.WorkflowClassInfo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWorkflowRepository implements WorkflowRepository, FileBasedWorkflowRepositoryMXBean {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWorkflowRepository.class);

    private static final int flags = ClassReader.EXPAND_FRAMES;

    protected static final class VolatileState {
        public final Map<String, Class<?>> wfClassMap;
        public final Map<String, Class<?>> wfMapLatest;
        public final Map<String, Class<?>> wfMapVersioned;
        public final Map<String, List<WorkflowVersion>> wfVersions;
        public final Map<String, String> javaSources;
        public final Map<String, ClassInfo> classInfoMap;
        public final Map<String, WorkflowClassInfo> workflowClassInfoMap;
        public final ClassLoader classLoader;
        public final long checksum;

        public VolatileState(Map<String, Class<?>> wfMap, Map<String, Class<?>> wfMapVersioned, Map<String, List<WorkflowVersion>> wfVersions, ClassLoader classLoader, long checksum, Map<String, Class<?>> wfClassMap, Map<String, String> javaSources, Map<String, ClassInfo> classInfoMap, Map<String, WorkflowClassInfo> workflowClassInfoMap) {
            this.wfMapLatest = wfMap;
            this.wfMapVersioned = wfMapVersioned;
            this.classLoader = classLoader;
            this.checksum = checksum;
            this.wfVersions = wfVersions;
            this.wfClassMap = wfClassMap;
            this.javaSources = javaSources;
            this.classInfoMap = classInfoMap;
            this.workflowClassInfoMap = workflowClassInfoMap;
        }
    }

    @Override
    public <E> WorkflowFactory<E> createWorkflowFactory(final String wfName) throws ClassNotFoundException {
        return createWorkflowFactory(wfName, null);
    }

    @Override
    public <E> WorkflowFactory<E> createWorkflowFactory(final String wfName, final WorkflowVersion version) throws ClassNotFoundException {
        if (wfName == null)
            throw new NullPointerException();

        final VolatileState volatileState = getVolatileState();
        if (version == null) {
            if (!volatileState.wfMapLatest.containsKey(wfName)) {
                throw new ClassNotFoundException("Workflow " + wfName + " not found");
            }
            return new WorkflowFactory<E>() {
                @SuppressWarnings("unchecked")
                @Override
                public Workflow<E> newInstance() throws InstantiationException, IllegalAccessException {
                    return (Workflow<E>) volatileState.wfMapLatest.get(wfName).newInstance();
                }
            };
        }

        final String alias = createAliasName(wfName, version);
        if (!volatileState.wfMapVersioned.containsKey(alias)) {
            throw new ClassNotFoundException("Workflow " + wfName + " with version " + version + " not found");
        }
        return new WorkflowFactory<E>() {
            @SuppressWarnings("unchecked")
            @Override
            public Workflow<E> newInstance() throws InstantiationException, IllegalAccessException {
                return (Workflow<E>) volatileState.wfMapVersioned.get(alias).newInstance();
            }
        };
    }

    @Override
    public java.lang.Class<?> resolveClass(String classname) throws java.io.IOException, ClassNotFoundException {
        final VolatileState volatileState = getVolatileState();
        return Class.forName(classname, false, volatileState.classLoader);
    }

    @Override
    public WorkflowVersion findLatestMajorVersion(String wfName, long majorVersion) {
        final VolatileState volatileState = getVolatileState();
        final List<WorkflowVersion> versionsList = volatileState.wfVersions.get(wfName);
        if (versionsList == null)
            return null;

        WorkflowVersion rv = null;
        for (WorkflowVersion v : versionsList) {
            if (v.getMajorVersion() > majorVersion) {
                break;
            }
            rv = v;
        }
        return rv;
    }

    @Override
    public WorkflowVersion findLatestMinorVersion(String wfName, long majorVersion, long minorVersion) {
        final VolatileState volatileState = getVolatileState();
        final List<WorkflowVersion> versionsList = volatileState.wfVersions.get(wfName);
        if (versionsList == null)
            return null;

        WorkflowVersion rv = null;
        for (WorkflowVersion v : versionsList) {
            if ((v.getMajorVersion() > majorVersion) || (v.getMajorVersion() == majorVersion && v.getMinorVersion() > minorVersion)) {
                break;
            }
            rv = v;
        }
        return rv;
    }

    @Override
    public ClassInfo getClassInfo(@SuppressWarnings("rawtypes") Class<? extends Workflow> wfClazz) {
        return getVolatileState().classInfoMap.get(wfClazz.getCanonicalName().replace(".", "/"));
    }

    @Override
    public List<WorkflowClassInfo> getWorkflows() {
        return new ArrayList<>(getVolatileState().workflowClassInfoMap.values());
    }

    @Override
    public WorkflowClassInfo[] queryWorkflowsSubset(int max, int offset) {
        WorkflowClassInfo wfInfo[] = getVolatileState().workflowClassInfoMap.values().toArray(new WorkflowClassInfo[0]);
        int available = wfInfo.length - offset;
        if (available > max && max > 0) {
            available = max;
        }
        WorkflowClassInfo subset[]= new WorkflowClassInfo[available];
        int counter = 0;
        for (int i = offset; i < (available + offset); i++) {
            subset[counter] = wfInfo[i];
            counter++;
        }
        return subset;
    }

    @Override
    public int getWorkflowRepoSize() {
        return getVolatileState().workflowClassInfoMap.values().size();
    }

    protected static Map<String, WorkflowClassInfo> createWorkflowClassInfoMap(final Map<String, Class<?>> wfClassMap, final Map<String, String> javaSources) {
        final Map<String, WorkflowClassInfo> map = new HashMap<>();
        for (Class<?> wfClass : wfClassMap.values()) {
            WorkflowClassInfo wfi = new WorkflowClassInfo();
            wfi.setClassname(wfClass.getName());
            wfi.setSourceCode(javaSources.get(wfClass.getName()));
            if (wfi.getSourceCode() == null)
                wfi.setSourceCode("NA");
            WorkflowDescription wfDesc = wfClass.getAnnotation(WorkflowDescription.class);
            if (wfDesc != null) {
                wfi.setAlias(wfDesc.alias());
                wfi.setMajorVersion(wfDesc.majorVersion());
                wfi.setMinorVersion(wfDesc.minorVersion());
                wfi.setPatchLevel(wfDesc.patchLevelVersion());
            }
            map.put(wfClass.getName(), wfi);
        }
        return map;
    }

    protected void instrumentWorkflows(File adaptedTargetDir, Map<String, Clazz> clazzMap, Map<String, ClassInfo> classInfos, ClassLoader tmpClassLoader) throws IOException {
        logger.info("Instrumenting classfiles");
        for (Clazz clazz : clazzMap.values()) {
            byte[] bytes;
            InputStream is = clazz.classfile.openStream();
            try {
                ClassReader cr2 = new ClassReader(is);
                ClassNode cn = new ClassNode();
                cr2.accept(cn, flags);
                traceClassNode(clazz.classname + " - original", cn);

                // Now content of ClassNode can be modified and then serialized back into bytecode:
                new TryCatchBlockHandler().instrument(cn);

                ClassWriter cw2 = new ClassWriter(0);
                cn.accept(cw2);
                bytes = cw2.toByteArray();
                traceBytes(clazz.classname + " - after TryCatchBlockHandler", bytes);

                ClassReader cr = new ClassReader(bytes);
                ClassWriter cw = new ClassWriter(0);

                ScottyClassAdapter cv = new ScottyClassAdapter(cw, clazz.aggregatedInterruptableMethods);
                cr.accept(cv, flags);
                classInfos.put(clazz.classname, cv.getClassInfo());
                bytes = cw.toByteArray();
                traceBytes(clazz.classname + " - after ScottyClassAdapter", bytes);

                // Recompute frames, etc.
                ClassReader cr3 = new ClassReader(bytes);
                ClassWriter cw3 = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                cr3.accept(cw3, ClassReader.SKIP_FRAMES);
                bytes = cw3.toByteArray();
                traceBytes(clazz.classname + " - after COMPUTE_FRAMES", bytes);

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                CheckClassAdapter.verify(new ClassReader(cw.toByteArray()), tmpClassLoader, false, pw);
                if (sw.toString().length() != 0) {
                    logger.error("CheckClassAdapter.verify failed for class " + cn.name + ":\n" + sw.toString());
                } else {
                    logger.info("CheckClassAdapter.verify succeeded for class " + cn.name);
                }

            } finally {
                is.close();
            }

            File adaptedClassfileName = new File(adaptedTargetDir, clazz.classname + ".class");
            adaptedClassfileName.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(adaptedClassfileName);
            try {
                fos.write(bytes);
            } finally {
                fos.close();
            }
        }
    }

    private static void traceClassNode(String message, ClassNode cn) {
        if (logger.isTraceEnabled()) {
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            traceBytes(message, cw.toByteArray());
        }
    }

    private static void traceBytes(String message, byte[] bytes) {
        if (logger.isTraceEnabled()) {
            StringWriter sw = new StringWriter();
            new ClassReader(bytes).accept(new TraceClassVisitor(new PrintWriter(sw)), 0);
            logger.trace(message + ":\n{}", sw.toString());
        }
    }

    protected ClassLoader createClassLoader(Map<String, Class<?>> map, File adaptedTargetDir, File compileTargetDir, Map<String, Clazz> clazzMap) throws MalformedURLException, ClassNotFoundException {
        logger.info("Creating classes");
        final Map<String, Clazz> clazzMapCopy = new HashMap<String, Clazz>(clazzMap);
        URLClassLoader classLoader = new URLClassLoader(new URL[] { adaptedTargetDir.toURI().toURL(), compileTargetDir.toURI().toURL() }, Thread.currentThread().getContextClassLoader()) {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    try {
                        c = super.findClass(name);
                        if (clazzMapCopy.containsKey(name)) {
                            // Check that the workflow class is transformed
                            if (c.getAnnotation(Transformed.class) == null)
                                throw new ClassFormatError("Copper workflow " + name + " is not transformed!");
                        }
                        logger.info(c.getName() + " created");
                    } catch (Exception e) {
                        c = super.loadClass(name, false);
                    }
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        };

        for (Clazz clazz : clazzMap.values()) {
            String name = clazz.classname.replace('/', '.');
            Class<?> c = classLoader.loadClass(name);
            map.put(name, c);
        }

        return classLoader;
    }

    protected void checkConstraints(Map<String, Class<?>> workflowClasses) throws CopperRuntimeException {
        for (Class<?> c : workflowClasses.values()) {
            if (c.getName().length() > 512) {
                throw new CopperRuntimeException("Workflow class names are limited to 512 characters");
            }
        }
    }

    protected String createAliasName(final String alias, final WorkflowVersion version) {
        return alias + "#" + version.format();
    }

    protected abstract VolatileState getVolatileState();

    @Override
    public WorkflowClassInfo getWorkflowInfo(String classname) {
        return getVolatileState().workflowClassInfoMap.get(classname);
    }


    public ClassLoader getWorkflowClassLoader() {
        return getVolatileState().classLoader;
    }
}
