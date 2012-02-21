/*
 * Copyright 2002-2012 SCOOP Software GmbH
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import de.scoopgmbh.copper.WorkflowFactory;
import de.scoopgmbh.copper.instrument.ScottyFindInterruptableMethodsVisitor;
import de.scoopgmbh.copper.util.FileUtil;

/**
 * A file system based workflow repository for COPPER with some Tomcat specific features.
 * 
 * @author bergmann
 *
 * @deprecated
 */
public class TomcatWorkflowRepository extends AbstractWorkflowRepository {

	/**
	 * For COPPER internal use only
	 *	 
	 */
	protected static final class VolatileState {
		public Map<String,Class<?>> wfMap;
		public ClassLoader classLoader;
		public long checksum;
		VolatileState(Map<String, Class<?>> wfMap, ClassLoader classLoader, long checksum) {
			this.wfMap = wfMap;
			this.classLoader = classLoader;
			this.checksum = checksum;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(TomcatWorkflowRepository.class);


	private String sourceDir;
	private String targetDir;
	protected volatile VolatileState volatileState;
	private Thread observerThread;
	private int checkIntervalMSec = 15000;
	private List<Runnable> preprocessors = Collections.emptyList();
	private boolean stopped = false;
	private HashSet<String> knownWorkflowClasses = new HashSet<String>(Arrays.asList(new String[]{"de/scoopgmbh/copper/Workflow","de/scoopgmbh/copper/persistent/PersistentWorkflow"}));

	protected synchronized void addKnownWorkflowClass (String classname) {
		this.knownWorkflowClasses.add(classname);
	}
	
	public synchronized void setCheckIntervalMSec(int checkIntervalMSec) {
		this.checkIntervalMSec = checkIntervalMSec;
	}

	public synchronized void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}

	public synchronized String getSourceDir() {
		return this.sourceDir;
	}

	public synchronized void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

	public synchronized String getTargetDir() {
		return this.targetDir;
	}

	public synchronized void setPreprocessors(List<Runnable> preprocessors) {
		if (preprocessors == null) throw new NullPointerException();
		this.preprocessors = preprocessors;
	}

	@Override
	public <E> WorkflowFactory<E> createWorkflowFactory(final String classname) throws ClassNotFoundException {
		return new WorkflowFactory<E>() {
			@SuppressWarnings("unchecked")
			@Override
			public Workflow<E> newInstance() throws InstantiationException, IllegalAccessException {
				return (Workflow<E>) volatileState.wfMap.get(classname).newInstance();
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
				FileUtil.deleteDirectory(dir);
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
								final long checksum = FileUtil.processChecksum(new File(sourceDir));
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

	protected synchronized VolatileState createWfClassMap() throws IOException, ClassNotFoundException {
		final File sourceDirectory = new File(sourceDir);
		if (!sourceDirectory.exists()) {
			throw new IllegalArgumentException("source directory "+sourceDir+" does not exist!");
		}

		final File baseTargetDir = new File(targetDir,Long.toHexString(System.currentTimeMillis()));
		final File compileTargetDir = new File(baseTargetDir,"classes");
		final File adaptedTargetDir = new File(baseTargetDir,"adapted");
		if (!compileTargetDir.exists()) compileTargetDir.mkdirs();
		if (!adaptedTargetDir.exists()) adaptedTargetDir.mkdirs();

		final long checksum = FileUtil.processChecksum(sourceDirectory,".java");

		compile(compileTargetDir);
		final Map<String, Clazz> clazzMap = findInterruptableMethods(compileTargetDir);
		instrumentWorkflows(adaptedTargetDir, clazzMap);

		final ClassLoader cl = createClassLoader(adaptedTargetDir, compileTargetDir);
		final Map<String,Class<?>> map = loadClasses(cl,clazzMap);
		return new VolatileState(map, cl, checksum);
	}

	private void compile(File compileTargetDir) throws IOException {
		logger.info("compiling workflow files");
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) throw new NullPointerException("No java compiler available! Did you start from a JDK? JRE will not work.");
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		List<String> options = new ArrayList<String>();
		options.add("-d");
		options.add(compileTargetDir.getAbsolutePath());
		options.add("-classpath");
		StringBuilder buf = new StringBuilder();
		URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		for ( URL url : loader.getURLs() ) {
			buf.append(url.getFile()).append(File.pathSeparator);
		}
		options.add(buf.toString());
		try {
			final StringWriter sw = new StringWriter();
			sw.append("Compilation failed!\n");
			final File[] files = FileUtil.findFiles(new File(sourceDir), ".java");
			if (files.length > 0) {
				final Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));
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

	private Map<String, Clazz> findInterruptableMethods(File compileTargetDir)
		throws FileNotFoundException, IOException 
	{
		logger.info("analysing workflow classes");
		// Find and visit all classes
		Map<String,Clazz> clazzMap = new HashMap<String, Clazz>();
		File[] classfiles = FileUtil.findFiles(compileTargetDir, ".class");
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
				if ( this.knownWorkflowClasses.contains(clazz.superClassname) ) {
					break inner;
				}
				clazz = clazzMap.get(clazz.superClassname);
				if (clazz == null) {
					break;
				}
			}
//			if (clazz == null) {
//				// this is no workflow 
//				clazzMap.remove(classname);
//			}
		}
		return clazzMap;
	}

	private ClassLoader createClassLoader (File adaptedTargetDir, File compileTargetDir) throws MalformedURLException, ClassNotFoundException 
	{
		logger.debug("creating workflow loader");
		URLClassLoader classLoader = new URLClassLoader(new URL[] { adaptedTargetDir.toURI().toURL(), compileTargetDir.toURI().toURL() }, Thread.currentThread().getContextClassLoader()) {
			@Override
			protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				Class<?> c = findLoadedClass(name);
				if (c == null) {
					try {
						c = super.findClass(name);
						//if (c.getAnnotation(Transformed.class) == null) throw new ClassFormatError("Copper workflow "+name+" is not transformed!");
						logger.info(c.getName()+" created");
					}
					catch (Exception e) {
						c = super.loadClass(name,false);
					}
				}
				if (resolve) {
					resolveClass(c);
				}
				return c;
			}
		};
		return classLoader;
	}

	private Map<String,Class<?>> loadClasses(ClassLoader classLoader, Map<String, Clazz> clazzMap)
		throws MalformedURLException, ClassNotFoundException 
	{
		logger.info("loading workflow classes");
		final Map<String,Class<?>> map = new HashMap<String, Class<?>>();
		for (Clazz clazz : clazzMap.values()) {
			String name = clazz.classname.replace('/', '.');
			Class<?> c = classLoader.loadClass(name);
			map.put(name, c);
		}
		return map;
	}


	public static void main(String[] args) {
		TomcatWorkflowRepository repo = new TomcatWorkflowRepository();
		repo.setSourceDir("src/workflow/java");
		repo.setTargetDir("target/compiled_workflow");
		repo.start();
	}

}
