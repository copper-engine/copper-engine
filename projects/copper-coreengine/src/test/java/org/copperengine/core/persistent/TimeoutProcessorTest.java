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
package org.copperengine.core.persistent;

import java.sql.Timestamp;

import org.copperengine.core.persistent.TimeoutProcessor;
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
