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

/**
 * Provides Base64 encoding and decoding as defined by RFC 2045.
 *
 * @author dmoebius
 * @since 3.0.1
 * @deprecated This class can be replaced by using the built-in class java.util.Base64
 */
@Deprecated
public class Base64 {
    private static final java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
    private static final java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();

    /**
     * Encodes a byte[] containing binary data, into a String containing characters in the Base64 alphabet.
     *
     * @param data
     *         a byte array containing binary data
     * @return a String containing only Base64 character data
     * @deprecated This function can be replaced by using the built-in class java.util.Base64
     */
    @Deprecated
    public static String encode(byte[] data) {
        return encoder.encodeToString(data);
    }

    /**
     * Decodes a String containing containing characters in the Base64 alphabet into a byte array.
     *
     * @param data
     *         a String containing Base64 character data
     * @return a byte array containing binary data
     * @deprecated This function can be replaced by using the built-in class java.util.Base64
     */
    @Deprecated
    public static byte[] decode(String data) {
        return decoder.decode(data);
    }
}
