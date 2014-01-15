/*
 * Copyright 2002-2014 SCOOP Software GmbH
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

import javax.xml.bind.DatatypeConverter;

/**
 * Provides Base64 encoding and decoding as defined by RFC 2045.
 *
 * @author dmoebius
 * @since 3.0.1
 */
public class Base64 {

    /**
     * Encodes a byte[] containing binary data, into a String containing characters in the Base64 alphabet.
     *
     * @param data
     *         a byte array containing binary data
     * @return a String containing only Base64 character data
     */
    public static String encode(byte[] data) {
        return DatatypeConverter.printBase64Binary(data);
    }

    /**
     * Decodes a String containing containing characters in the Base64 alphabet into a byte array.
     *
     * @param data
     *         a String containing Base64 character data
     * @return a byte array containing binary data
     */
    public static byte[] decode(String data) {
        return DatatypeConverter.parseBase64Binary(data);
    }

}
