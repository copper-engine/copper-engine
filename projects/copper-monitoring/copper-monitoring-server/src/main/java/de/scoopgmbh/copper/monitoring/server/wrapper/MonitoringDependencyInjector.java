package de.scoopgmbh.copper.monitoring.server.wrapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import de.scoopgmbh.copper.AbstractDependencyInjector;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataCollector;

/**
 * Add Monitoring for DependencyInjector
 * @author hbrackmann
 *
 */
public class MonitoringDependencyInjector extends AbstractDependencyInjector{
	
	private final AbstractDependencyInjector abstractDependencyInjector;
	private final MonitoringDataCollector monitoringDataCollector;
	
	public MonitoringDependencyInjector(AbstractDependencyInjector abstractDependencyInjector, MonitoringDataCollector monitoringDataCollector) {
		super();
		this.abstractDependencyInjector = abstractDependencyInjector;
		this.monitoringDataCollector = monitoringDataCollector;
	}

	@Override
	protected Object getBean(String beanId) {
		//protected Workaround
		Object adapter;
		try {
			final Method method = abstractDependencyInjector.getClass().getDeclaredMethod("getBean", String.class);
			method.setAccessible(true);
			adapter = method.invoke(abstractDependencyInjector,beanId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	    if (adapter!=null && adapter.getClass().getInterfaces().length>0){
	    	return java.lang.reflect.Proxy.newProxyInstance(adapter.getClass().getClassLoader(), adapter.getClass().getInterfaces(), new DependencyHandler(adapter,monitoringDataCollector));
	    } else {
	    	return adapter;
	    }
	}	

	private static class DependencyHandler implements InvocationHandler {
		private final Object adapter;
		private final MonitoringDataCollector monitoringDataCollector;
		
		public DependencyHandler(Object adapter, MonitoringDataCollector monitoringDataCollector) {
			this.adapter = adapter;
			this.monitoringDataCollector = monitoringDataCollector;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			monitoringDataCollector.submitAdapterCalls(method, args, adapter);
			Object result = method.invoke(this.adapter, args);
			return result;
		}
	}

  

}
