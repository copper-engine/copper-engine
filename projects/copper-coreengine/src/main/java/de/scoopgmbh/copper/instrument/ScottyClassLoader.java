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
package de.scoopgmbh.copper.instrument;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class ScottyClassLoader extends ClassLoader {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ScottyClassLoader.class);
	
	private final ScottyDynamicFindInterruptableMethodsVisitor scottyInterruptableMethodsVisitor = new ScottyDynamicFindInterruptableMethodsVisitor(this);
	private final Map<String,Class<?>> classes = new HashMap<String, Class<?>>();
	
	public ScottyClassLoader() {
		super();
	}

	public ScottyClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)	throws ClassNotFoundException {
		if (name.endsWith("WF")) {
			Class<?> c = classes.get(name);
			if (c != null)
				return c;

			String resource = name.replace('.', '/') + ".class";

			for (int i=0; i<2; i++) {
				try {
					InputStream is2 = getResourceAsStream(resource);
					ClassReader cr2 = new ClassReader(is2);
					cr2.accept(scottyInterruptableMethodsVisitor, 0);
					is2.close();
				}
				catch(Exception e) {
					throw new ClassNotFoundException(name, e);
				}
			}

			InputStream is = getResourceAsStream(resource);
			byte[] b;

			// adapts the class on the fly
			try {
				ClassReader cr = new ClassReader(is);
				ClassWriter cw = new ClassWriter(0);
				ClassVisitor cv = new ScottyClassAdapter(cw,scottyInterruptableMethodsVisitor.getInterruptableMethods());
				cr.accept(cv, 0);
				b = cw.toByteArray();
				is.close();
			} 
			catch (Exception e) {
				throw new ClassNotFoundException(name, e);
			}

			// optional: stores the adapted class on disk
			try {
				String fileName = "adapted/" + resource;
				FileOutputStream fos = new FileOutputStream(fileName);
				fos.write(b);
				fos.close();
			} catch (Exception e) {
			}
			
			// returns the adapted class
			c = defineClass(name, b, 0, b.length);
			classes.put(name, c);
			return c;
		}
		else {
			return super.loadClass(name, resolve);
		}
	}

}
