/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.wfrepo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.CopperRuntimeException;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowDescription;
import de.scoopgmbh.copper.WorkflowFactory;
import de.scoopgmbh.copper.WorkflowVersion;
import de.scoopgmbh.copper.instrument.ScottyFindInterruptableMethodsVisitor;
import de.scoopgmbh.copper.util.FileUtil;

/**
 * A file system based workflow repository for COPPER.
 * 
 * @author austermann
 *
 */
public class FileBasedWorkflowRepository extends AbstractWorkflowRepository {

	private static final class VolatileState {
		final Map<String,Class<?>> wfMapLatest;
		final Map<String,Class<?>> wfMapVersioned;
		final Map<String,List<WorkflowVersion>> wfVersions;
		final ClassLoader classLoader;
		final long checksum;
		VolatileState(Map<String, Class<?>> wfMap, Map<String,Class<?>> wfMapVersioned, Map<String,List<WorkflowVersion>> wfVersions, ClassLoader classLoader, long checksum) {
			this.wfMapLatest = wfMap;
			this.wfMapVersioned = wfMapVersioned;
			this.classLoader = classLoader;
			this.checksum = checksum;
			this.wfVersions = wfVersions;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(FileBasedWorkflowRepository.class);

	private String targetDir;
	private volatile VolatileState volatileState;
	private Thread observerThread;
	private int checkIntervalMSec = 15000;
	private List<Runnable> preprocessors = Collections.emptyList();
	private volatile boolean stopped = false;
	private boolean loadNonWorkflowClasses = false;
	private List<CompilerOptionsProvider> compilerOptionsProviders = new ArrayList<CompilerOptionsProvider>();
	private List<String> sourceDirs = new ArrayList<String>();
	private List<String> sourceArchiveUrls = new ArrayList<String>();
	
	/**
	 * Sets the list of source archive URLs. The source archives must be ZIP compressed archives, containing COPPER workflows as .java files. 
	 * @param sourceArchiveUrls
	 */
	public void setSourceArchiveUrls(List<String> sourceArchiveUrls) {
		if (sourceArchiveUrls == null) throw new IllegalArgumentException();
		this.sourceArchiveUrls = new ArrayList<String>(sourceArchiveUrls);
	}

	/**
	 * Adds a source archive URL.
	 */
	public void addSourceArchiveUrl(String url) {
		if (url == null) throw new IllegalArgumentException();
		sourceArchiveUrls.add(url);
	}
	
	/**
	 * Returns the configured source archive URL(s).
	 */
	public List<String> getSourceArchiveUrls() {
		return Collections.unmodifiableList(sourceArchiveUrls);
	}
	
	/**
	 * Sets the list of CompilerOptionsProviders. They are called before compiling the workflow files to append compiler options.
	 */
	public void setCompilerOptionsProviders(List<CompilerOptionsProvider> compilerOptionsProviders) {
		if (compilerOptionsProviders == null) throw new NullPointerException();
		this.compilerOptionsProviders = new ArrayList<CompilerOptionsProvider>(compilerOptionsProviders);
	}
	
	/**
	 * Add a CompilerOptionsProvider. They are called before compiling the workflow files to append compiler options.
	 */
	public void addCompilerOptionsProvider(CompilerOptionsProvider cop) {
		this.compilerOptionsProviders.add(cop);
	}
	
	/**
	 * Returns the currently configured compiler options providers. 
	 */
	public List<CompilerOptionsProvider> getCompilerOptionsProviders() {
		return Collections.unmodifiableList(compilerOptionsProviders);
	}

	/**
	 * The repository will check the source directory every <code>checkIntervalMSec</code> milliseconds
	 * for changed workflow files. If it finds a changed file, all contained workflows are recompiled.
	 * 
	 * @param checkIntervalMSec
	 */
	public void setCheckIntervalMSec(int checkIntervalMSec) {
		this.checkIntervalMSec = checkIntervalMSec;
	}

	/**
	 * @deprecated use setSourceDirs instead
	 */
	public void setSourceDir(String sourceDir) {
		sourceDirs = new ArrayList<String>();
		sourceDirs.add(sourceDir);
	}
	
	/**
	 * Configures the local directory/directories that contain the COPPER workflows as <code>.java<code> files.
	 */
	public void setSourceDirs(List<String> sourceDirs) {
		if (sourceDirs == null) throw new IllegalArgumentException();
		this.sourceDirs = new ArrayList<String>(sourceDirs);
	}
	
	/**
	 * Returns the configures local directory/directories
	 */
	public List<String> getSourceDirs() {
		return Collections.unmodifiableList(sourceDirs);
	}

	/** 
	 * If true, this workflow repository's class loader will also load non-workflow classes, e.g.
	 * inner classes or helper classes.
	 * As this is maybe not always useful, use this property to enable or disable this feature.
	 */
	public void setLoadNonWorkflowClasses(boolean loadNonWorkflowClasses) {
		this.loadNonWorkflowClasses = loadNonWorkflowClasses;
	}
	
	/**
	 * mandatory parameter - must point to a local directory with read/write privileges. COPPER will store the 
	 * compiled workflow class files there.
	 * 
	 * @param targetDir
	 */
	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}
	
	public String getTargetDir() {
		return targetDir;
	}

	public void setPreprocessors(List<Runnable> preprocessors) {
		if (preprocessors == null) throw new NullPointerException();
		this.preprocessors = new ArrayList<Runnable>(preprocessors);
	}

	@Override
	public <E> WorkflowFactory<E> createWorkflowFactory(final String wfName) throws ClassNotFoundException {
		return createWorkflowFactory(wfName,null);
	}

	@Override
	public <E> WorkflowFactory<E> createWorkflowFactory(final String wfName, final WorkflowVersion version) throws ClassNotFoundException {
		if (wfName == null) throw new NullPointerException();
		if (stopped)
			throw new IllegalStateException("Repo is stopped");

		if (version == null) {
			if (!volatileState.wfMapLatest.containsKey(wfName)) {
				throw new ClassNotFoundException("Workflow "+wfName+" not found");
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
			throw new ClassNotFoundException("Workflow "+wfName+" with version "+version+" not found");
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
	public java.lang.Class<?> resolveClass(java.io.ObjectStreamClass desc) throws java.io.IOException ,ClassNotFoundException {
		return Class.forName(desc.getName(), false, volatileState.classLoader);
	};


	@Override
	public synchronized void start() {
		if (stopped) throw new IllegalStateException();
		try {
			if (volatileState == null) {
				File dir = new File(targetDir);
				deleteDirectory(dir);
				dir.mkdirs();

				for (Runnable preprocessor : preprocessors) {
					preprocessor.run();
				}
				volatileState = createWfClassMap();

				observerThread = new Thread("WfRepoObserver") {
					@Override
					public void run() {
						logger.info("Starting observation");
						while(!stopped) {
							try {
								Thread.sleep(checkIntervalMSec);
								for (Runnable preprocessor : preprocessors) {
									preprocessor.run();
								}
								final long checksum = processChecksum(sourceDirs);
								if (checksum != volatileState.checksum) {
									logger.info("Change detected - recreating workflow map");
									volatileState = createWfClassMap();
								}
							} 
							catch(InterruptedException e) {
								// ignore
							}
							catch (Exception e) {
								logger.error("",e);
							}
						}
						logger.info("Stopping observation");
					}
				};
				observerThread.setDaemon(true);
				observerThread.start();
			}
		} 
		catch (Exception e) {
			logger.error("start failed",e);
			throw new Error("start failed",e);
		}
	}

	@Override
	public synchronized void shutdown() {
		logger.info("shutting down...");
		if (stopped) return;
		stopped = true;
		observerThread.interrupt();
	}

	private synchronized VolatileState createWfClassMap() throws IOException, ClassNotFoundException {
		for (String dir : sourceDirs) {
			final File sourceDirectory = new File(dir);
			if (!sourceDirectory.exists()) {
				throw new IllegalArgumentException("source directory "+dir+" does not exist!");
			}
		}
		// Check that the URLs are ok
		if (!sourceArchiveUrls.isEmpty()) {
			for (String _url : sourceArchiveUrls) {
				try {
					URL url = new URL(_url);
					url.openStream().close();
				}
				catch(Exception e) {
					throw new IOException("Unable to open URL '"+_url+"'", e);
				}
			}
		}

		final Map<String,Class<?>> map = new HashMap<String, Class<?>>();
		final long checksum = processChecksum(sourceDirs);
		final File baseTargetDir = new File(targetDir,Long.toHexString(System.currentTimeMillis()));
		final File additionalSourcesDir = new File(baseTargetDir,"additionalSources");
		final File compileTargetDir = new File(baseTargetDir,"classes");
		final File adaptedTargetDir = new File(baseTargetDir,"adapted");
		if (!compileTargetDir.exists()) compileTargetDir.mkdirs();
		if (!adaptedTargetDir.exists()) adaptedTargetDir.mkdirs();
		if (!additionalSourcesDir.exists()) additionalSourcesDir.mkdirs();
		extractAdditionalSources(additionalSourcesDir, this.sourceArchiveUrls);

		compile(compileTargetDir,additionalSourcesDir);
		final Map<String, Clazz> clazzMap = findInterruptableMethods(compileTargetDir);
		instrumentWorkflows(adaptedTargetDir, clazzMap, compileTargetDir);
		final ClassLoader cl = createClassLoader(map, adaptedTargetDir, loadNonWorkflowClasses ? compileTargetDir : adaptedTargetDir, clazzMap);
		checkConstraints(map);
		
		final Map<String,Class<?>> wfMapLatest = new HashMap<String, Class<?>>(map.size());
		final Map<String,Class<?>> wfMapVersioned = new HashMap<String, Class<?>>(map.size());
		final Map<String,WorkflowVersion> latest = new HashMap<String, WorkflowVersion>(map.size());
		final Map<String,List<WorkflowVersion>> versions = new HashMap<String, List<WorkflowVersion>>();
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
		return new VolatileState(wfMapLatest, wfMapVersioned, versions, cl, checksum);
	}

	private String createAliasName(final String alias, final WorkflowVersion version) {
		return alias+"#"+version.format();
	}
	
	private void checkConstraints(Map<String, Class<?>> workflowClasses) throws CopperRuntimeException {
		for (Class<?> c : workflowClasses.values()) {
			if (c.getName().length() > 512) {
				throw new CopperRuntimeException("Workflow class names are limited to 256 characters");
			}
		}
	}

	private static void extractAdditionalSources(File additionalSourcesDir, List<String> sourceArchives) throws IOException {
		for (String _url : sourceArchives) {
			URL url = new URL(_url);
			ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(url.openStream()));
			try {
				ZipEntry entry;
				int size;
				byte[] buffer = new byte[2048];
				while((entry = zipInputStream.getNextEntry()) != null) {
					logger.info("Unzipping "+entry.getName());
					if (entry.isDirectory()) {
						File f = new File(additionalSourcesDir, entry.getName());
						f.mkdirs();
					}
					else {
						File f = new File(additionalSourcesDir, entry.getName());
						BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(f));
						while ((size = zipInputStream.read(buffer, 0, buffer.length)) != -1) {
							os.write(buffer, 0, size);
						}
						os.close();
					}
				}
			}
			finally {
				zipInputStream.close();
			}
		}
	}

	private Map<String, Clazz> findInterruptableMethods(File compileTargetDir) throws FileNotFoundException, IOException {
		logger.info("Analysing classfiles");
		// Find and visit all classes
		Map<String,Clazz> clazzMap = new HashMap<String, Clazz>();
		List<File> classfiles = findFiles(compileTargetDir, ".class");
		for (File f : classfiles) {
			ScottyFindInterruptableMethodsVisitor visitor = new ScottyFindInterruptableMethodsVisitor();
			InputStream is = new FileInputStream(f);
			try {
				ClassReader cr = new ClassReader(is);
				cr.accept(visitor, 0);
			}
			finally {
				is.close();
			}
			Clazz clazz = new Clazz();
			clazz.interruptableMethods = visitor.getInterruptableMethods();
			clazz.classfile = f;
			clazz.classname = visitor.getClassname();
			clazz.superClassname = visitor.getSuperClassname();
			clazzMap.put(clazz.classname, clazz);
		}

		// Remove all classes that are no workflow
		List<String> allClassNames = new ArrayList<String>(clazzMap.keySet());
		for (String classname : allClassNames) {
			Clazz clazz = clazzMap.get(classname);
			Clazz startClazz = clazz;
			inner : while(true) {
				startClazz.aggregatedInterruptableMethods.addAll(clazz.interruptableMethods);
				if ("de/scoopgmbh/copper/Workflow".equals(clazz.superClassname) || "de/scoopgmbh/copper/persistent/PersistentWorkflow".equals(clazz.superClassname)) {
					break inner;
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

	private void compile(File compileTargetDir, File additionalSourcesDir) throws IOException {
		logger.info("Compiling workflows");
		List<String> options = new ArrayList<String>();
		options.add("-d");
		options.add(compileTargetDir.getAbsolutePath());
		for (CompilerOptionsProvider cop : compilerOptionsProviders) {
			options.addAll(cop.getOptions());
		}
		logger.info("Compiler options: "+options.toString());
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) throw new NullPointerException("No java compiler available! Did you start from a JDK? JRE will not work.");
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		try {
			final StringWriter sw = new StringWriter();
			sw.append("Compilation failed!\n");
			final List<File> files = new ArrayList<File>();
			for (String dir : sourceDirs) {
				files.addAll(findFiles(new File(dir), ".java"));
			}
			files.addAll(findFiles(additionalSourcesDir,".java"));
			if (files.size() > 0) {
				final Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(files);
				final CompilationTask task = compiler.getTask(sw, fileManager, null, options, null, compilationUnits1);
				if (!task.call()) {
					logger.error(sw.toString());
					throw new CopperRuntimeException("Compilation failed, see logfile for details");
				}
			}
		}
		finally {
			fileManager.close();
		}
	}

	private List<File> findFiles(File rootDir, String fileExtension) {
		List<File> files = new ArrayList<File>();
		findFiles(rootDir, fileExtension, files);
		return files;
	}

	private void findFiles(final File rootDir, final String fileExtension, final List<File> files) {
		files.addAll(Arrays.asList(rootDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(fileExtension);
			}
		})));
		File[] subdirs = rootDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		for (File subdir : subdirs) {
			findFiles(subdir, fileExtension, files);
		}
	}

	private static long processChecksum(List<String> sourceDirs) {
		return FileUtil.processChecksum(sourceDirs,".java");
	}

	private static boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}

	public void addSourceDir(String dir) {
		sourceDirs.add(dir);
	}

	public static void main(String[] args) {
		FileBasedWorkflowRepository repo = new FileBasedWorkflowRepository();
		repo.addSourceDir("src/workflow/java");
		repo.setTargetDir("target/compiled_workflow");
		repo.start();
	}

	@Override
	public WorkflowVersion findLatestMajorVersion(String wfName, long majorVersion) {
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

	protected ClassLoader getClassLoader() {
		return volatileState.classLoader;
	}

}
