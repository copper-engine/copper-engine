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
package org.copperengine.monitoring.core.data;

import java.util.HashMap;

import org.copperengine.monitoring.core.model.MeasurePointData;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

/**
 * We use Kryo instead of java serialization to create small serialized output
 */
public class SerializeUtil {

    private static Kryo kryo;

    public static Kryo createKryo() {
        kryo = new Kryo();
        FieldSerializer<MeasurePointData> someClassSerializer = new FieldSerializer<MeasurePointData>(kryo, MeasurePointData.class);
        someClassSerializer.getField("measurePointId").setSerializer(new MeasurePointIdSerializer());
        kryo.register(MeasurePointData.class, someClassSerializer);
        return kryo;
    }

    /**
     * Measure points have often same id, to save space the id is only saved once
     */
    public static class MeasurePointIdSerializer extends Serializer<String> {
        HashMap<Integer, String> keyToString = new HashMap<Integer, String>();
        HashMap<String, Integer> stringToKey = new HashMap<String, Integer>();

        {
            setImmutable(true);
            setAcceptsNull(true);
        }

        @Override
        public void write(Kryo kryo, Output output, String object) {
            if (stringToKey.containsKey(object)) {
                output.writeInt(stringToKey.get(object));
            } else {
                output.writeInt(-1);
                output.writeString(object);
                stringToKey.put(object, stringToKey.size());
            }
        }

        @Override
        public String read(Kryo kryo, Input input, Class<String> type) {
            int key = input.readInt();
            if (key == -1) {
                final String string = input.readString();
                keyToString.put(keyToString.size(), string);
                return string;
            } else {
                return keyToString.get(key);
            }
        }
    }

}
