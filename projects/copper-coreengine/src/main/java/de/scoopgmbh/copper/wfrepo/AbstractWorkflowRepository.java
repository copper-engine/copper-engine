/*
 * Copyright 2002-2011 SCOOP Software GmbH
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.instrument.ScottyClassAdapter;
import de.scoopgmbh.copper.instrument.Transformed;
import de.scoopgmbh.copper.instrument.TryCatchBlockHandler;

abstract class AbstractWorkflowRepository implements WorkflowRepository {
	
	private static final Logger logger = Logger.getLogger(AbstractWorkflowRepository.class);
	
	void instrumentWorkflows(File adaptedTargetDir, Map<String, Clazz> clazzMap) throws IOException {
		logger.info("Instrumenting classfiles");
		for (Clazz clazz : clazzMap.values()) {
			byte[] bytes;
			FileInputStream fis = new FileInputStream(clazz.classfile);
			try {
				ClassReader cr2 = new ClassReader(fis);
				ClassNode cn = new ClassNode();
				cr2.accept(cn, 0);

				//Now content of ClassNode can be modified and then serialized back into bytecode:
				new TryCatchBlockHandler().instrument(cn);

				ClassWriter cw2 = new ClassWriter(0);
				cn.accept(cw2);
				bytes = cw2.toByteArray();
				
				if (logger.isTraceEnabled()) {
					StringWriter sw = new StringWriter();
					new ClassReader(bytes).accept(new TraceClassVisitor(new PrintWriter(sw)), 0);
					logger.trace(sw.toString());
				}
				
				ClassReader cr = new ClassReader(bytes);
				ClassWriter cw = new ClassWriter(0);

				ClassVisitor cv = new ScottyClassAdapter(cw,clazz.aggregatedInterruptableMethods);
				cr.accept(cv,0);
				bytes = cw.toByteArray();
				
				ClassReader cr3 = new ClassReader(bytes);
				ClassWriter cw3 = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
				cr3.accept(cw3, ClassReader.SKIP_FRAMES);
				bytes = cw3.toByteArray();
				
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				CheckClassAdapter.verify(new ClassReader(cw.toByteArray()), false, pw);
				if (sw.toString().length() != 0) {
					logger.fatal("CheckClassAdapter.verify failed for class "+cn.name+":\n"+sw.toString());
				}
				else {
					logger.info("CheckClassAdapter.verify succeeded for class "+cn.name);
				}
				
			}
			finally {
				fis.close();
			}

			File adaptedClassfileName = new File(adaptedTargetDir,clazz.classname+".class");
			adaptedClassfileName.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(adaptedClassfileName);
			try {
				fos.write(bytes);
			}
			finally {
				fos.close();
			}
		}
	}
	
	ClassLoader createClassLoader(Map<String, Class<?>> map, File adaptedTargetDir, File compileTargetDir, Map<String, Clazz> clazzMap) throws MalformedURLException, ClassNotFoundException {
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
							if (c.getAnnotation(Transformed.class) == null) throw new ClassFormatError("Copper workflow "+name+" is not transformed!");
						}
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

		for (Clazz clazz : clazzMap.values()) {
			String name = clazz.classname.replace('/', '.');
			Class<?> c = classLoader.loadClass(name);
			map.put(name, c);
		}

		return classLoader;
	}
	

}
