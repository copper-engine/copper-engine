package de.scoopgmbh.copper.persistent;

import java.sql.Timestamp;

import org.junit.Assert;
import org.junit.Test;

public class TimeoutProcessorTest {

	@Test
	public void test() {
		final long now = System.currentTimeMillis();
		Assert.assertEquals(new Timestamp(Long.MAX_VALUE), TimeoutProcessor.processTimout(Long.MAX_VALUE, 3000L));
		Assert.assertEquals(new Timestamp(Long.MAX_VALUE), TimeoutProcessor.processTimout(null, Long.MAX_VALUE));
		Assert.assertTrue(TimeoutProcessor.processTimout(3000L, 4000L).getTime()-(now+3000L) < 10L);
		Assert.assertTrue(TimeoutProcessor.processTimout(null, 4000L).getTime()-(now+4000L) < 10L);
	}

}
