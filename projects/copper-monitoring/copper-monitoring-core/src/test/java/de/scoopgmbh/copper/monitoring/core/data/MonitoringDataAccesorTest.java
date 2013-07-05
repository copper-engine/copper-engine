package de.scoopgmbh.copper.monitoring.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;
import de.scoopgmbh.copper.monitoring.core.model.LogEvent;


public class MonitoringDataAccesorTest {
	
	
	@Test
	public void test_kryo(){
		Kryo kryo = new Kryo();
		kryo.register(AdapterWfLaunchInfo.class);
		{
			Output output = new Output(1024);
			final AdapterWfLaunchInfo object = new AdapterWfLaunchInfo();
			object.setAdapterName("abc");
			kryo.writeClassAndObject(output, object);
			kryo.writeClassAndObject(output, new LogEvent());
			assertTrue(output.getBuffer().length>0);
			
			Input input = new Input(output.getBuffer());
			assertEquals(AdapterWfLaunchInfo.class, kryo.readClassAndObject(input).getClass());
			assertEquals(LogEvent.class, kryo.readClassAndObject(input).getClass());
		}
	}
	
	@Test
	public void test_add(){
		MonitoringDataStorage monitoringDataStorage;
		try {
			monitoringDataStorage = new MonitoringDataStorage(File.createTempFile("test", ".tmp").getParentFile(), "copperMonitorLog");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		final MonitoringDataAdder monitoringDataAdder = new MonitoringDataAdder(monitoringDataStorage);
		final MonitoringDataAccesor monitoringDataAccesor = new MonitoringDataAccesor(monitoringDataStorage);
		
		final AdapterWfLaunchInfo adapterWfLaunch = new AdapterWfLaunchInfo();
		adapterWfLaunch.setAdapterName("abc");
		adapterWfLaunch.setTimestamp(new Date());
		monitoringDataAdder.add(adapterWfLaunch);
		
		assertEquals(1, monitoringDataAccesor.getAdapterWfLaunches().size());
	}
	
	@Test
	public void test_add_different (){
		MonitoringDataStorage monitoringDataStorage;
		try {
			monitoringDataStorage = new MonitoringDataStorage(File.createTempFile("test", ".tmp").getParentFile(), "copperMonitorLog");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		final MonitoringDataAccesor monitoringDataAccesor = new MonitoringDataAccesor(monitoringDataStorage);
		final MonitoringDataAdder monitoringDataAdder = new MonitoringDataAdder(monitoringDataStorage);
		final AdapterWfLaunchInfo adapterWfLaunch = new AdapterWfLaunchInfo();
		adapterWfLaunch.setTimestamp(new Date());
		monitoringDataAdder.add(adapterWfLaunch);
		final LogEvent logEvent = new LogEvent();
		logEvent.setTime(new Date());
		monitoringDataAdder.add(logEvent);
		
		assertEquals(1, monitoringDataAccesor.getAdapterWfLaunches().size());
		assertEquals(1, monitoringDataAccesor.getLogEvents().size());
	}
	
	@Test
	public void test_getContent(){
		MonitoringDataStorage monitoringDataStorage;
		try {
			monitoringDataStorage = new MonitoringDataStorage(File.createTempFile("test", ".tmp").getParentFile(), "copperMonitorLog");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		final MonitoringDataAccesor monitoringDataAccesor = new MonitoringDataAccesor(monitoringDataStorage);
		final MonitoringDataAdder monitoringDataAdder = new MonitoringDataAdder(monitoringDataStorage);
		
		
		final AdapterWfLaunchInfo adapterWfLaunch = new AdapterWfLaunchInfo();
		adapterWfLaunch.setTimestamp(new Date());
		monitoringDataAdder.add(adapterWfLaunch);
		final LogEvent logEvent = new LogEvent();
		logEvent.setTime(new Date());
		
		for (Input input: monitoringDataAccesor.getData().getReadableInput().read()){
			assertNotNull(SerializeUtil.getKryo().readClassAndObject(input));
		}
		
	}
	


	
}
