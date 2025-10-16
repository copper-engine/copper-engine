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
package org.copperengine.core.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Base64Test {

    @Test
    public void testEncodeThenDecode() {
        for(int length : List.of(0 , 1 ,  2,  3,  1000000)) {
            byte[] data = createRandomData(length);
            String s = Base64.encode(data);
            byte[] result = Base64.decode(s);
            assertArrayEquals(data, result);
        }
    }

    private byte[] createRandomData(int length) {
        byte[] data = new byte[length];
        Random random = new Random();
        random.nextBytes(data);
        return data;
    }
}
