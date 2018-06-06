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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.WorkflowDescription;
import org.copperengine.core.WorkflowVersion;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.instrument.ClassInfo;
import org.copperengine.core.instrument.ScottyFindInterruptableMethodsVisitor;
import org.copperengine.core.util.FileUtil;
import org.copperengine.management.FileBasedWorkflowRepositoryMXBean;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file system based workflow repository for COPPER.
 * Workflow classes have to be deployed as Java files.
 * They have to reside in one ore more directories or/and in one or more jarfiles.
 * On system startup these Java files are automatically compiled and instrumented by this repository.
 * It offers hot deployment by observing the Java files in the configured directories. In case of modifications, all
 * Java files are compiled and instrumented again.
 * 
 * @author austermann
 */
public class FileBasedWorkflowRepository extends AbstractWorkflowRepository implements WorkflowRepository, FileBasedWorkflowRepositoryMXBean {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedWorkflowRepository.class);

    private String targetDir;
    private volatile VolatileState volatileState;
    private Thread observerThread;
    private int checkIntervalMSec = 15000;
    private List<Runnable> preprocessors = Collections.emptyList();
    private volatile boolean stopped = false;
    private boolean loadNonWorkflowClasses = true;
    private List<CompilerOptionsProvider> compilerOptionsProviders = new ArrayList<CompilerOptionsProvider>();
    private List<String> sourceDirs = new ArrayList<String>();
    private List<String> sourceArchiveUrls = new ArrayList<String>();

    /**
     * Sets the list of source archive URLs. The source archives must be ZIP compressed archives, containing COPPER
     * workflows as .java files.
     * @param sourceArchiveUrls
     *        urls where workflow class sources reside in
     */
    public void setSourceArchiveUrls(List<String> sourceArchiveUrls) {
        if (sourceArchiveUrls == null)
            throw new IllegalArgumentException();
        this.sourceArchiveUrls = new ArrayList<String>(sourceArchiveUrls);
    }

    /**
     * Adds a source archive URL.
     * @param url
     *        url to be added
     */
    public void addSourceArchiveUrl(String url) {
        if (url == null)
            throw new IllegalArgumentException();
        sourceArchiveUrls.add(url);
    }

    /**
     * @return the configured source archive URL(s).
     */
    public List<String> getSourceArchiveUrls() {
        return Collections.unmodifiableList(sourceArchiveUrls);
    }

    /**
     * Sets the list of CompilerOptionsProviders. They are called before compiling the workflow files to append compiler
     * options.
     * @param compilerOptionsProviders
     *        Options from those providers are used for compilation.
     */
    public void setCompilerOptionsProviders(List<CompilerOptionsProvider> compilerOptionsProviders) {
        if (compilerOptionsProviders == null)
            throw new NullPointerException();
        this.compilerOptionsProviders = new ArrayList<CompilerOptionsProvider>(compilerOptionsProviders);
    }

    /**
     * Add a CompilerOptionsProvider. They are called before compiling the workflow files to append compiler options.
     * @param cop
     *        Options from this provider are used for compilation.
     */
    public void addCompilerOptionsProvider(CompilerOptionsProvider cop) {
        this.compilerOptionsProviders.add(cop);
    }

    /**
     * @return the currently configured compiler options providers.
     */
    public List<CompilerOptionsProvider> getCompilerOptionsProviders() {
        return Collections.unmodifiableList(compilerOptionsProviders);
    }

    /**
     * The repository will check the source directory every <code>checkIntervalMSec</code> milliseconds for changed
     * workflow files. If it finds a changed file, all contained workflows are recompiled. The default is 15 seconds.
     *
     * @param checkIntervalMSec
     *        check interval in milliseconds
     */
    public void setCheckIntervalMSec(int checkIntervalMSec) {
        this.checkIntervalMSec = checkIntervalMSec;
    }

    /**
     * Configures the local directory/directories that contain the COPPER workflows as <code>.java</code> files.
     * @param sourceDirs
     *         List of those source directories
     */
    public void setSourceDirs(List<String> sourceDirs) {
        if (sourceDirs == null)
            throw new IllegalArgumentException();
        this.sourceDirs = new ArrayList<String>(sourceDirs);
    }

    /**
     * Configures the local directory/directories that contain the COPPER workflows as <code>.java</code> files.
     * @param sourceDirs
     *         List of those source directories
     */
    public void setSourceDirs(String... sourceDirs) {
        if (sourceDirs == null)
            throw new IllegalArgumentException();
        this.sourceDirs = Arrays.asList(sourceDirs);
    }

    /**
     * @return the configured local directory/directories
     */
    public List<String> getSourceDirs() {
        return Collections.unmodifiableList(sourceDirs);
    }

    /**
     * @param loadNonWorkflowClasses
     *        If true (which is the default), this workflow repository's class loader will also load non-workflow classes, e.g.
     *        inner classes or helper classes. As this is maybe not always useful, use this property to enable or disable this
     *        feature.
     */
    public void setLoadNonWorkflowClasses(boolean loadNonWorkflowClasses) {
        this.loadNonWorkflowClasses = loadNonWorkflowClasses;
    }

    /**
     * The target directory where COPPER will store the compiled workflow classes (mandatory). Must point to a local
     * directory with read/write privileges.
     * <b>Note:</b> On repository startup, this directory will be deleted and created freshly. So make sure, there are
     * no other files in there but only compilation results from COPPER.
     * @param targetDir
     *        the target dir.
     */
    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public String getTargetDir() {
        return targetDir;
    }

    public void setPreprocessors(List<Runnable> preprocessors) {
        if (preprocessors == null)
            throw new NullPointerException();
        this.preprocessors = new ArrayList<Runnable>(preprocessors);
    }

    static class ObserverThread extends Thread {

        final WeakReference<FileBasedWorkflowRepository> repository;

        public ObserverThread(FileBasedWorkflowRepository repository) {
            super("WfRepoObserver");
            this.repository = new WeakReference<FileBasedWorkflowRepository>(repository);
            setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("Starting observation");
            FileBasedWorkflowRepository repository = this.repository.get();
            while (repository != null && !repository.stopped) {
                try {
                    int checkIntervalMSec = repository.checkIntervalMSec;
                    repository = null;
                    Thread.sleep(checkIntervalMSec);
                    repository = this.repository.get();
                    if (repository == null)
                        break;
                    for (Runnable preprocessor : repository.preprocessors) {
                        preprocessor.run();
                    }
                    final long checksum = processChecksum(repository.sourceDirs);
                    if (checksum != repository.volatileState.checksum) {
                        logger.info("Change detected - recreating workflow map");
                        repository.volatileState = repository.createWfClassMap();
                    }
                } catch (InterruptedException e) {
                    // ignore
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
            logger.info("Stopping observation");
        }
    }

    @Override
    public synchronized void start() {
        if (stopped)
            throw new IllegalStateException();
        try {
            if (volatileState == null) {
                File dir = new File(targetDir);
                deleteDirectory(dir);
                dir.mkdirs();

                for (Runnable preprocessor : preprocessors) {
                    preprocessor.run();
                }
                volatileState = createWfClassMap();

                observerThread = new ObserverThread(this);
                observerThread.start();
            }
        } catch (Exception e) {
            logger.error("start failed", e);
            throw new Error("start failed", e);
        }
    }

    @Override
    public synchronized void shutdown() {
        logger.info("shutting down...");
        if (stopped)
            return;
        stopped = true;
        observerThread.interrupt();
        try {
            observerThread.join();
        } catch (Exception e) {
            /* ignore */
        }
    }

    private synchronized VolatileState createWfClassMap() throws IOException, ClassNotFoundException {
        for (String dir : sourceDirs) {
            final File sourceDirectory = new File(dir);
            if (!sourceDirectory.exists()) {
                throw new IllegalArgumentException("source directory " + dir + " does not exist!");
            }
        }
        // Check that the URLs are ok
        if (!sourceArchiveUrls.isEmpty()) {
            for (String _url : sourceArchiveUrls) {
                try {
                    URL url = new URL(_url);
                    url.openStream().close();
                } catch (Exception e) {
                    throw new IOException("Unable to open URL '" + _url + "'", e);
                }
            }
        }

        final Map<String, Class<?>> map = new HashMap<String, Class<?>>();
        final long checksum = processChecksum(sourceDirs);
        final File baseTargetDir = new File(targetDir, Long.toHexString(System.currentTimeMillis()));
        final File additionalSourcesDir = new File(baseTargetDir, "additionalSources");
        final File compileTargetDir = new File(baseTargetDir, "classes");
        final File adaptedTargetDir = new File(baseTargetDir, "adapted");
        if (!compileTargetDir.exists())
            compileTargetDir.mkdirs();
        if (!adaptedTargetDir.exists())
            adaptedTargetDir.mkdirs();
        if (!additionalSourcesDir.exists())
            additionalSourcesDir.mkdirs();
        extractAdditionalSources(additionalSourcesDir, this.sourceArchiveUrls);

        Map<String, File> sourceFiles = compile(compileTargetDir, additionalSourcesDir);
        final Map<String, Clazz> clazzMap = findInterruptableMethods(compileTargetDir);
        final Map<String, ClassInfo> clazzInfoMap = new HashMap<String, ClassInfo>();
        instrumentWorkflows(adaptedTargetDir, clazzMap, clazzInfoMap, new URLClassLoader(new URL[] { compileTargetDir.toURI().toURL() }, Thread.currentThread().getContextClassLoader()));
        for (Clazz clazz : clazzMap.values()) {
            // Workaround for https://github.com/spotbugs/spotbugs/issues/500:
            File f = sourceFiles.get(new StringBuilder(clazz.classname).append(".java").toString());
            // TODO: replace the above workaround with the following line when the spotbug issue has been solved
            // File f = sourceFiles.get(clazz.classname + ".java");
            ClassInfo info = clazzInfoMap.get(clazz.classname);
            if (info != null) {
                if (f != null) {
                    info.setSourceCode(readFully(f));
                }
                ClassInfo superClassInfo = clazzInfoMap.get(clazz.superClassname);
                info.setSuperClassInfo(superClassInfo);
            }
        }
        final ClassLoader cl = createClassLoader(map, adaptedTargetDir, loadNonWorkflowClasses ? compileTargetDir : adaptedTargetDir, clazzMap);
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

        final Map<String, String> sources = readJavaFiles(wfClassMap, sourceDirs, additionalSourcesDir);

        return new VolatileState(wfMapLatest, wfMapVersioned, versions, cl, checksum, wfClassMap, sources, clazzInfoMap, createWorkflowClassInfoMap(wfMapLatest, sources));
    }

    private byte[] readFully(File f) throws IOException {
        byte[] data = new byte[(int) f.length()];
        int c = 0;
        FileInputStream fistr = new FileInputStream(f);
        int read;
        while ((read = fistr.read(data, c, data.length - c)) > 0) {
            c += read;
        }
        fistr.close();
        if (c < data.length)
            throw new IOException("Premature end of file");
        return data;
    }

    private static void extractAdditionalSources(File additionalSourcesDir, List<String> sourceArchives) throws IOException {
        for (String _url : sourceArchives) {
            URL url = new URL(_url);
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(url.openStream()));
            try {
                ZipEntry entry;
                int size;
                byte[] buffer = new byte[2048];
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    logger.info("Unzipping " + entry.getName());
                    if (entry.isDirectory()) {
                        File f = new File(additionalSourcesDir, entry.getName());
                        f.mkdirs();
                    } else {
                        File f = new File(additionalSourcesDir, entry.getName());
                        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(f));
                        while ((size = zipInputStream.read(buffer, 0, buffer.length)) != -1) {
                            os.write(buffer, 0, size);
                        }
                        os.close();
                    }
                }
            } finally {
                zipInputStream.close();
            }
        }
    }

    private Map<String, Clazz> findInterruptableMethods(File compileTargetDir) throws IOException {
        logger.info("Analysing classfiles");
        // Find and visit all classes
        Map<String, Clazz> clazzMap = new HashMap<String, Clazz>();
        Collection<File> classfiles = findFiles(compileTargetDir, ".class").values();
        for (File f : classfiles) {
            ScottyFindInterruptableMethodsVisitor visitor = new ScottyFindInterruptableMethodsVisitor();
            InputStream is = new FileInputStream(f);
            try {
                ClassReader cr = new ClassReader(is);
                cr.accept(visitor, 0);
            } finally {
                is.close();
            }
            Clazz clazz = new Clazz();
            clazz.interruptableMethods = visitor.getInterruptableMethods();
            clazz.classfile = f.toURI().toURL();
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
            }
        }
        return clazzMap;
    }

    private Map<String, File> compile(File compileTargetDir, File additionalSourcesDir) throws IOException {
        logger.info("Compiling workflows");
        List<String> options = new ArrayList<String>();
        options.add("-g");
        options.add("-d");
        options.add(compileTargetDir.getAbsolutePath());
        for (CompilerOptionsProvider cop : compilerOptionsProviders) {
            options.addAll(cop.getOptions());
        }
        if(Collections.disjoint(options, Arrays.asList("--class-path", "-classpath", "-cp"))) {
            String modulePath = System.getProperty("jdk.module.path", "");
            if(!modulePath.isEmpty()) {
                options.add("-cp");
                options.add(modulePath);
            }
        }
        logger.info("Compiler options: " + options.toString());
        JavaCompiler compiler = getJavaCompiler();
        if (compiler == null)
            throw new IllegalStateException("No Java compiler available! Please make sure that either tools.jar is provided, or that you start with a full JDK (not an JRE!), or that any other JSR-199 compatible Java compiler is available on the classpath, e.g. the Eclipse compiler ecj");
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        final Map<String, File> files = new HashMap<String, File>();
        try {
            final StringWriter sw = new StringWriter();
            sw.append("Compilation failed!\n");
            for (String dir : sourceDirs) {
                files.putAll(findFiles(new File(dir), ".java"));
            }
            files.putAll(findFiles(additionalSourcesDir, ".java"));
            if (files.size() > 0) {
                final Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(files.values());
                final CompilationTask task = compiler.getTask(sw, fileManager, null, options, null, compilationUnits1);
                if (!task.call()) {
                    logger.error(sw.toString());
                    throw new CopperRuntimeException("Compilation failed, see logfile for details");
                }
            }
        } finally {
            fileManager.close();
        }
        return files;
    }

    private JavaCompiler getJavaCompiler() {
        JavaCompiler systemJavaCompiler = ToolProvider.getSystemJavaCompiler();
        if (systemJavaCompiler != null) {
            return systemJavaCompiler;
        }
        logger.debug("System java compiler not found; searching for other java compilers...");
        ServiceLoader<JavaCompiler> loader = ServiceLoader.load(JavaCompiler.class);
        Iterator<JavaCompiler> it = loader.iterator();
        if (it.hasNext()) {
            JavaCompiler javaCompiler = it.next();
            logger.debug("Found java compiler: {}", javaCompiler);
            return javaCompiler;
        }
        return null;
    }

    private Map<String, String> readJavaFiles(final Map<String, Class<?>> wfClassMap, List<String> _sourceDirs, File additionalSourcesDir) throws IOException {
        final List<File> sourceDirs = new ArrayList<File>();
        for (String dir : _sourceDirs) {
            sourceDirs.add(new File(dir));
        }
        sourceDirs.add(additionalSourcesDir);

        final Map<String, String> map = new HashMap<String, String>(wfClassMap.size());
        for (Class<?> wfClass : wfClassMap.values()) {
            final String classname = wfClass.getName();
            final String path = classname.replace(".", "/") + ".java";
            for (File sourceDir : sourceDirs) {
                final File javaFile = new File(sourceDir, path);
                if (javaFile.exists() && javaFile.isFile() && javaFile.canRead()) {
                    final BufferedReader br = new BufferedReader(new FileReader(javaFile));
                    try {
                        String line;
                        StringBuilder sb = new StringBuilder(32 * 1024);
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        map.put(classname, sb.toString());
                    } finally {
                        br.close();
                    }
                }
            }
        }

        return map;
    }

    private Map<String, File> findFiles(File rootDir, String fileExtension) {
        Map<String, File> files = new HashMap<String, File>();
        findFiles(rootDir, fileExtension, files, "");
        return files;
    }

    private void findFiles(final File rootDir, final String fileExtension, final Map<String, File> files, final String pathPrefix) {
        final File[] result = rootDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(fileExtension);
            }
        });

        if (result != null) {
            for (File f : result) {
                files.put(pathPrefix + f.getName(), f);
            }
        }
        

        final File[] subdirs = rootDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (subdirs != null) {
            for (File subdir : subdirs) {
                findFiles(subdir, fileExtension, files, pathPrefix + subdir.getName() + "/");
            }
        }
    }

    private static long processChecksum(List<String> sourceDirs) {
        return FileUtil.processChecksum(sourceDirs, ".java");
    }

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return (path.delete());
    }

    public void addSourceDir(String dir) {
        sourceDirs.add(dir);
    }

    protected ClassLoader getClassLoader() {
        return volatileState.classLoader;
    }

    @Override
    public String getDescription() {
        return "Filebased Repository";
    }

    @Override
    protected VolatileState getVolatileState() {
        return volatileState;
    }

    /**
     * Sets the list of compiler options. They are called before compiling the workflow files to append compiler
     * options (internally invokes {@link FileBasedWorkflowRepository#setCompilerOptionsProviders(List)}.
     * @param options
     *        list of options to be set for the compiler.
     */
    public void setCompilerOptions(String... options) {
        List<CompilerOptionsProvider> compilerOptionsProviders = new ArrayList<>();
        ConfigurableStringOptionsProvider x = new ConfigurableStringOptionsProvider();
        x.setOptions(Arrays.asList(options));
        compilerOptionsProviders.add(x);
        setCompilerOptionsProviders(compilerOptionsProviders);
    }

}
