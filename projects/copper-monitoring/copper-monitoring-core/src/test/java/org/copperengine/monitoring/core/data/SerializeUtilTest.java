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
package org.copperengine.monitoring.core.data;

import static org.junit.Assert.assertEquals;

import org.copperengine.monitoring.core.data.SerializeUtil.MeasurePointIdSerializer;
import org.copperengine.monitoring.core.model.MeasurePointData;
import org.junit.Test;
import org.mockito.Mockito;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SerializeUtilTest {

    @Test
    public void test_measurepoint_save() {
        Kryo kryo = SerializeUtil.createKryo();
        Output output = new Output(66666);

        kryo.writeClassAndObject(output, new MeasurePointData("1111"));
        int oldPos = output.position();
        kryo.writeClassAndObject(output, new MeasurePointData("1111"));
        assertEquals(oldPos + 19, output.position());// only key is written not string again
        kryo.writeClassAndObject(output, new MeasurePointData("1111"));
        kryo.writeClassAndObject(output, new MeasurePointData("2222"));
        kryo.writeClassAndObject(output, new MeasurePointData("2222"));
        kryo.writeClassAndObject(output, new MeasurePointData("1111"));

        Input input = new Input(output.getBuffer());
        assertEquals("1111", ((MeasurePointData) kryo.readClassAndObject(input)).getMeasurePointId());
        assertEquals("1111", ((MeasurePointData) kryo.readClassAndObject(input)).getMeasurePointId());
        assertEquals("1111", ((MeasurePointData) kryo.readClassAndObject(input)).getMeasurePointId());
        assertEquals("2222", ((MeasurePointData) kryo.readClassAndObject(input)).getMeasurePointId());
        assertEquals("2222", ((MeasurePointData) kryo.readClassAndObject(input)).getMeasurePointId());
        assertEquals("1111", ((MeasurePointData) kryo.readClassAndObject(input)).getMeasurePointId());
    }

    @Test
    public void test_write() {
        Kryo kryo = Mockito.mock(Kryo.class);
        Output output = Mockito.mock(Output.class);

        MeasurePointIdSerializer measurePointIdSerializer = new MeasurePointIdSerializer();
        measurePointIdSerializer.write(kryo, output, "111");
        Mockito.verify(output).writeInt(-1);
        Mockito.verify(output).writeString("111");

        measurePointIdSerializer.write(kryo, output, "111");
        Mockito.verify(output).writeInt(0);

        Mockito.reset(output);
        measurePointIdSerializer.write(kryo, output, "111");
        Mockito.verify(output).writeInt(0);
        Mockito.verify(output, Mockito.never()).writeString("111");

        Mockito.reset(output);
        measurePointIdSerializer.write(kryo, output, "222");
        Mockito.verify(output).writeInt(-1);
        Mockito.verify(output, Mockito.never()).writeString("22");

        Mockito.reset(output);
        measurePointIdSerializer.write(kryo, output, "222");
        Mockito.verify(output).writeInt(1);

        Mockito.reset(output);
        measurePointIdSerializer.write(kryo, output, "111");
        Mockito.verify(output).writeInt(0);
        Mockito.verify(output, Mockito.never()).writeString("111");
    }

    @Test
    public void test_read() {
        Kryo kryo = Mockito.mock(Kryo.class);
        Input input = Mockito.mock(Input.class);

        Mockito.when(input.readString()).thenReturn("111");
        Mockito.when(input.readInt()).thenReturn(-1);
        MeasurePointIdSerializer measurePointIdSerializer = new MeasurePointIdSerializer();
        assertEquals("111", measurePointIdSerializer.read(kryo, input, String.class));

        Mockito.when(input.readString()).thenReturn("?");
        Mockito.when(input.readInt()).thenReturn(0);
        assertEquals("111", measurePointIdSerializer.read(kryo, input, String.class));

        Mockito.when(input.readString()).thenReturn("?");
        Mockito.when(input.readInt()).thenReturn(0);
        assertEquals("111", measurePointIdSerializer.read(kryo, input, String.class));

        Mockito.when(input.readString()).thenReturn("222");
        Mockito.when(input.readInt()).thenReturn(-1);
        assertEquals("222", measurePointIdSerializer.read(kryo, input, String.class));

        Mockito.when(input.readString()).thenReturn("?");
        Mockito.when(input.readInt()).thenReturn(1);
        assertEquals("222", measurePointIdSerializer.read(kryo, input, String.class));
    }

}
