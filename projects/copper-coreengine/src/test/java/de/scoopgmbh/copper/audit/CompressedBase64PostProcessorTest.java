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
