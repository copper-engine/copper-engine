package de.scoopgmbh.copper.persistent;

import junit.framework.Assert;
import junit.framework.TestCase;

public class OracleScottyDBStorageTest extends TestCase {

	public void testcomputeLockId() {
		Assert.assertTrue(OracleScottyDBStorage.computeLockId("polygenelubricants") >= 0);
		Assert.assertTrue(OracleScottyDBStorage.computeLockId("polygenelubricants") < 1073741823);

		Assert.assertTrue(OracleScottyDBStorage.computeLockId("234234") >= 0);
		Assert.assertTrue(OracleScottyDBStorage.computeLockId("234234") < 1073741823);

		Assert.assertTrue(OracleScottyDBStorage.computeLockId("test") >= 0);
		Assert.assertTrue(OracleScottyDBStorage.computeLockId("test") < 1073741823);
}
}
