package de.scoopgmbh.copper.monitoring.server.wrapper;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Test;
import org.mockito.Mockito;

import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataCollector;
import de.scoopgmbh.copper.util.PojoDependencyInjector;


public class MonitoringDependencyInjectorTest {

	@Test
	public void test_no_interface(){
		final PojoDependencyInjector pojoDependencyInjector = new PojoDependencyInjector();
		final Object bean = new Object();
		pojoDependencyInjector.register("test", bean);
		final MonitoringDataCollector mock = Mockito.mock(MonitoringDataCollector.class);
		MonitoringDependencyInjector monitoringDependencyInjector = new MonitoringDependencyInjector(pojoDependencyInjector, mock);
		monitoringDependencyInjector.getBean("test").toString();
	}	
	
	private static interface Test42{
		void abc();
	}
	
	@Test
	public void test_interface(){
		final PojoDependencyInjector pojoDependencyInjector = new PojoDependencyInjector();
		final Test42 bean = new Test42(){
			@Override
			public void abc() {
				//empty
			}
		};
		pojoDependencyInjector.register("test", bean);
		final MonitoringDataCollector mock = Mockito.mock(MonitoringDataCollector.class);
		MonitoringDependencyInjector monitoringDependencyInjector = new MonitoringDependencyInjector(pojoDependencyInjector, mock);
		((Test42)monitoringDependencyInjector.getBean("test")).abc();
		
		Mockito.verify(mock).submitAdapterCalls(Mockito.any(Method.class), Mockito.any(Object[].class), Mockito.any());

	}
	
	@Test
	public void test_forward(){
		final PojoDependencyInjector pojoDependencyInjector = new PojoDependencyInjector();
		final Object bean = new Object();
		pojoDependencyInjector.register("test", bean);
		final MonitoringDataCollector mock = Mockito.mock(MonitoringDataCollector.class);
		MonitoringDependencyInjector monitoringDependencyInjector = new MonitoringDependencyInjector(pojoDependencyInjector, mock);
		assertEquals(bean, monitoringDependencyInjector.getBean("test"));
	}
	
}
