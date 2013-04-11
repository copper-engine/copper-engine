package de.scoopgmbh.copper.monitor.server;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import de.scoopgmbh.copper.monitor.adapter.ServerLogin;


public class SpringRemoteServerMainTest {
	
	@Test
	public void test_connect() throws Exception{
		new Thread(){
			@Override
			public void run() {
				new SpringRemoteServerMain().start();
			};
		}.start();
		Thread.sleep(1000);
		
		HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(ServerLogin.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://localhost/ServerLogin");
		httpInvokerProxyFactoryBean.setHttpInvokerRequestExecutor(new CommonsHttpInvokerRequestExecutor());
		httpInvokerProxyFactoryBean.afterPropertiesSet();
	    
	    ServerLogin serverLogin = (ServerLogin)httpInvokerProxyFactoryBean.getObject();
		assertNotNull(serverLogin);
//		Registry registry = LocateRegistry.getRegistry("localhost",Registry.REGISTRY_PORT);
//		ServerLogin serverLogin = (ServerLogin) registry.lookup(ServerLogin.class.getSimpleName());
//		serverLogin.login("", "");
	}
}
