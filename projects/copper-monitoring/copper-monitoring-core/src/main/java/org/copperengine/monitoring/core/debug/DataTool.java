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
package org.copperengine.monitoring.core.debug;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.util.HashMap;

public class DataTool {

    static class ObjectId {
        final Object obj;

        public ObjectId(Object obj) {
            this.obj = obj;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(obj);
        }

        @Override
        public boolean equals(Object obj) {
            return ((ObjectId) obj).obj == this.obj;
        }
    }

    public static Data convert(Object o) {
        HashMap<ObjectId, Data> convertedObjects = new HashMap<ObjectId, Data>();
        return convert(convertedObjects, o);
    }

    private static Data convert(HashMap<ObjectId, Data> convertedObjects,
            Object o) {
        ObjectId objectId = new ObjectId(o);
        Data d = convertedObjects.get(objectId);
        if (d != null)
            return d;
        int nextId = convertedObjects.size();
        Class<? extends Object> clazz = o.getClass();
        if (CharSequence.class.isAssignableFrom(clazz)) {
            d = new PlainData(clazz.getCanonicalName(), nextId, o.toString().intern());
            convertedObjects.put(objectId, d);
        } else if (Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)) {
            d = new PlainData(clazz.getCanonicalName(), nextId, o);
            convertedObjects.put(objectId, d);
        } else if (clazz.isArray()) {
            if (clazz.getComponentType().isPrimitive()) {
                d = new PlainData(clazz.getCanonicalName(), nextId, o);
            } else {
                Object[] srcArray = (Object[]) o;
                Data[] array = new Data[srcArray.length];
                for (int i = 0; i < srcArray.length; ++i) {
                    if (srcArray[i] != null)
                        array[i] = convert(convertedObjects, srcArray[i]);
                }
                ArrayData sd = new ArrayData(clazz, nextId, array);
                convertedObjects.put(objectId, sd);
                d = sd;
            }
        } else {
            StructuredData sd = new StructuredData(clazz.getCanonicalName(), nextId);
            convertedObjects.put(objectId, sd);
            convertStructural(sd, convertedObjects, clazz, o);
            d = sd;
        }

        return d;
    }

    private static void convertStructural(
            StructuredData sd, HashMap<ObjectId, Data> convertedObjects, Class<?> clazz, Object o) {
        if (clazz.getSuperclass() != Object.class) {
            convertStructural(sd, convertedObjects, clazz.getSuperclass(), o);
        }
        for (Field f : clazz.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers()))
                    continue;
                f.setAccessible(true);
                Object member = f.get(o);
                if (member == null) {
                    sd.addMember(new Member(f.getName(), f.getType().getCanonicalName(), Data.NULL));
                } else {
                    Data d = convert(convertedObjects, member);
                    sd.addMember(new Member(f.getName(), f.getType().getCanonicalName(), d));
                }
            } catch (Exception e) { /* ignore */
            }
        }
    }

}
