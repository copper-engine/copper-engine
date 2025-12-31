/*
 * Copyright 2002-2015 SCOOP Software GmbH
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TimeoutProcessorTest {

    @Test
    void test() {
        final long now = System.currentTimeMillis();
        Assertions.assertEquals(new Timestamp(Long.MAX_VALUE), TimeoutProcessor.processTimout(Long.MAX_VALUE, 3000L));
        Assertions.assertEquals(new Timestamp(Long.MAX_VALUE), TimeoutProcessor.processTimout(null, Long.MAX_VALUE));
        Assertions.assertEquals(now + 3000L, TimeoutProcessor.processTimout(3000L, 4000L, now).getTime());
        Assertions.assertEquals(now + 4000L, TimeoutProcessor.processTimout(null, 4000L, now).getTime());
    }

}
