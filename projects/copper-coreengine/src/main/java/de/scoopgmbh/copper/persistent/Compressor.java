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
package de.scoopgmbh.copper.persistent;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Utility class for compressing and uncompressing byte arrays.
 * 
 * @author austermann
 *
 */
public class Compressor {
	
	private final Deflater deflater;
	private final Inflater inflater;
	private final byte[] buffer;
	
	/**
	 * creates a new instance 
	 * @param level compression level, see {@link Deflater}
	 * @param maxSize maximum compressed size of the byte array
	 */
	public Compressor(int level, int maxSize) {
		deflater = new Deflater(level);
		inflater = new Inflater();
		buffer = new byte[maxSize];
	}
	
	public byte[] compress(final byte[] bytes) {
		deflater.setInput(bytes);
		deflater.finish();
		int len = deflater.deflate(buffer);
		byte[] compressedBytes = new byte[len];
		System.arraycopy(buffer, 0, compressedBytes, 0, len);
		deflater.reset();
		return compressedBytes;
	}
	
	public byte[] uncompress(byte[] bytes) throws DataFormatException {
		inflater.setInput(bytes);
		int len = inflater.inflate(buffer);
		byte[] uncompressedBytes = new byte[len];
		System.arraycopy(buffer, 0, uncompressedBytes, 0, len);
		inflater.reset();
		return uncompressedBytes;
	}
	
	
	
}


