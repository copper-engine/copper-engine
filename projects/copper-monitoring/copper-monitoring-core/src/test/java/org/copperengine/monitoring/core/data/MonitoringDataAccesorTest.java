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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.copperengine.monitoring.core.data.filter.TypeFilter;
import org.copperengine.monitoring.core.model.AdapterWfLaunchInfo;
import org.copperengine.monitoring.core.model.LogEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class MonitoringDataAccesorTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void test_kryo() {
        Kryo kryo = new Kryo();
        kryo.register(AdapterWfLaunchInfo.class);
        {
            Output output = new Output(1024);
            final AdapterWfLaunchInfo object = new AdapterWfLaunchInfo();
            object.setAdapterName("abc");
            kryo.writeClassAndObject(output, object);
            kryo.writeClassAndObject(output, new LogEvent());
            assertTrue(output.getBuffer().length > 0);

            Input input = new Input(output.getBuffer());
            assertEquals(AdapterWfLaunchInfo.class, kryo.readClassAndObject(input).getClass());
            assertEquals(LogEvent.class, kryo.readClassAndObject(input).getClass());
        }
    }

    @Test
    public void test_add() {
        MonitoringDataStorage monitoringDataStorage;
        try {
            monitoringDataStorage = new MonitoringDataStorage(testFolder.newFolder("test"), "copperMonitorLog");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final MonitoringDataAdder monitoringDataAdder = new MonitoringDataAdder(monitoringDataStorage);
        final MonitoringDataAccesor monitoringDataAccesor = new MonitoringDataAccesor(monitoringDataStorage);

        final AdapterWfLaunchInfo adapterWfLaunch = new AdapterWfLaunchInfo();
        adapterWfLaunch.setAdapterName("abc");
        adapterWfLaunch.setTimestamp(new Date());
        monitoringDataAdder.addMonitoringData(adapterWfLaunch);

        assertEquals(1, monitoringDataAccesor.getList(new TypeFilter<AdapterWfLaunchInfo>(AdapterWfLaunchInfo.class), null, null, 1000).size());
    }

    @Test
    public void test_add_different() {
        MonitoringDataStorage monitoringDataStorage;
        try {
            monitoringDataStorage = new MonitoringDataStorage(testFolder.newFolder("test"), "copperMonitorLog");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final MonitoringDataAccesor monitoringDataAccesor = new MonitoringDataAccesor(monitoringDataStorage);
        final MonitoringDataAdder monitoringDataAdder = new MonitoringDataAdder(monitoringDataStorage);
        final AdapterWfLaunchInfo adapterWfLaunch = new AdapterWfLaunchInfo();
        adapterWfLaunch.setTimestamp(new Date());
        monitoringDataAdder.addMonitoringData(adapterWfLaunch);
        final LogEvent logEvent = new LogEvent();
        logEvent.setTime(new Date());
        monitoringDataAdder.addMonitoringData(logEvent);

        assertEquals(1, monitoringDataAccesor.getList(new TypeFilter<AdapterWfLaunchInfo>(AdapterWfLaunchInfo.class), null, null, 1000).size());
        assertEquals(1, monitoringDataAccesor.getList(new TypeFilter<LogEvent>(LogEvent.class), null, null, 1000).size());
    }

}
