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
package de.scoopgmbh.copper.audit;

import junit.framework.TestCase;

public class CompressedBase64PostProcessorTest extends TestCase {

	public void testSimple() throws Exception {
		simpleTest("");
		simpleTest("1");
		simpleTest("1234567890");
		simpleTest("1234567890abcdefghij");
	}

	public void testNull() {
		CompressedBase64PostProcessor compressor = new CompressedBase64PostProcessor();
		NullPointerException npe = null;
		try {
			compressor.deserialize(null);
		} catch (RuntimeException e) {
			npe = (NullPointerException) e.getCause();
		}
		assertNotNull("Deserialization of null causes a NullPointerException", npe);
		String nullSerialization = compressor.serialize(null);
		assertNotNull("Serialized null must not be null.", nullSerialization);
		String nullDeserialization = compressor.deserialize(nullSerialization);
		assertSame("Deserialized repsesentation of null must be null again.", null, nullDeserialization);
	}

	private void simpleTest(String msg) {
		CompressedBase64PostProcessor compressor = new CompressedBase64PostProcessor();
		String nmsgSerialization = compressor.serialize(msg);
		assertNotNull("Serialized '" + msg + "' must not be null.", nmsgSerialization);
		String msgDeserialization = compressor.deserialize(nmsgSerialization);
		assertEquals("Deserialized repsesentation of '" + msgDeserialization + "' must be '" + msg + "' again.", msg, msgDeserialization);
	}

}
