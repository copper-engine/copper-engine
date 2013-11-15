package de.scoopgmbh.copper.tranzient;

import java.sql.Timestamp;

import org.junit.Test;

import de.scoopgmbh.copper.tranzient.DefaultEarlyResponseContainer.EarlyResponse;

public class DefaultEarlyResponseContainerTest {

	@Test
	public void test() {
		EarlyResponse x = new DefaultEarlyResponseContainer.EarlyResponse(null, Long.MAX_VALUE-100);
		org.junit.Assert.assertEquals(x.ts, Long.MAX_VALUE);
	}

}
