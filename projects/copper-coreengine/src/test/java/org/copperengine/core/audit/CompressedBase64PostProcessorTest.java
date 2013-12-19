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
package org.copperengine.core.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class CompressedBase64PostProcessorTest {

    @Test
    public void testSimple() throws Exception {
        simpleTest("");
        simpleTest("1");
        simpleTest("1234567890");
        simpleTest("1234567890abcdefghij");
    }

    @Test
    public void testNull() {
        CompressedBase64PostProcessor compressor = new CompressedBase64PostProcessor();

        compressor.deserialize(null);

        String nullSerialization = compressor.serialize(null);
        assertNull("Serialized null must be null.", nullSerialization);
        String nullDeserialization = compressor.deserialize(nullSerialization);
        assertNull("Deserialized repsesentation of null must be null again.", nullDeserialization);
    }

    private void simpleTest(String msg) {
        CompressedBase64PostProcessor compressor = new CompressedBase64PostProcessor();
        String nmsgSerialization = compressor.serialize(msg);
        assertNotNull("Serialized '" + msg + "' must not be null.", nmsgSerialization);
        String msgDeserialization = compressor.deserialize(nmsgSerialization);
        assertEquals("Deserialized repsesentation of '" + msgDeserialization + "' must be '" + msg + "' again.", msg, msgDeserialization);
    }

}
