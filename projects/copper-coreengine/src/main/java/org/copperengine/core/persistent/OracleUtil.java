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
package org.copperengine.core.persistent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Array;
import java.sql.Connection;

/**
 * Some utils to use oracle driver without link dependencies. This is due to the fact that oracle drivers are not
 * admissible in public repositories.
 * 
 * @author rscheel
 */
public class OracleUtil {

    static Constructor<?> arrayDescriptorCtor;
    static Constructor<? extends Array> arrayCtor;

    static
    {
        try {
            arrayDescriptorCtor = Class.forName("oracle.sql.ArrayDescriptor").getConstructor(String.class, Connection.class);
            @SuppressWarnings("unchecked")
            Class<? extends Array> arrayClass = (Class<? extends Array>) Class.forName("oracle.sql.ARRAY");
            arrayCtor = arrayClass.getConstructor(Class.forName("oracle.sql.ArrayDescriptor"), Connection.class, Object.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object createArrayDescriptor(String arrayTypeName, Connection nativeConnection) {
        try {
            return arrayDescriptorCtor.newInstance(arrayTypeName, nativeConnection);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Array createArray(Object arrayDescriptor, Connection nativeConnection, Object args) {
        try {
            return arrayCtor.newInstance(arrayDescriptor, nativeConnection, args);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Array createArray(String arrayTypeName, Connection nativeConnection, Object args) {
        return createArray(createArrayDescriptor(arrayTypeName, nativeConnection), nativeConnection, args);
    }

}
