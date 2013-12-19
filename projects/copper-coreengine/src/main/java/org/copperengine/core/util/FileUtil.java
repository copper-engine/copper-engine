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
package org.copperengine.core.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

public class FileUtil {

	public static long processChecksum(File directory) {
		return processChecksum(directory, null);
	}

	public static long processChecksum(List<String> directories, String fileSuffix) {
		final CRC32 crc32 = new CRC32();
		for (String dir : directories) {
			processChecksum(new File(dir), crc32, fileSuffix);
		}
		return crc32.getValue();
	}

	
	public static long processChecksum(File directory, String fileSuffix) {
		final CRC32 crc32 = new CRC32();
		processChecksum(directory, crc32, fileSuffix);
		return crc32.getValue();
	}

	private static void processChecksum(File dir, CRC32 crc32, String fileSuffix) {
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				processChecksum(f, crc32, fileSuffix);
			}
			else {
				if (fileSuffix == null || f.getName().endsWith(fileSuffix)) {
					crc32.update(Long.toString(f.lastModified()).getBytes());
					crc32.update(f.getAbsolutePath().getBytes());
				}
			}
		}
	}
	
	public static File[] findFiles(File rootDir, String fileSuffix) {
		List<File> files = new ArrayList<File>();
		findFiles(rootDir, fileSuffix, files);
		return files.toArray(new File[files.size()]);
	}

	private static void findFiles(final File rootDir, final String fileSuffix, final List<File> files) {
		files.addAll(Arrays.asList(rootDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(fileSuffix);
			}
		})));
		File[] subdirs = rootDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		for (File subdir : subdirs) {
			findFiles(subdir, fileSuffix, files);
		}
	}
	
	public static boolean deleteDirectory(File path) {
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

}
