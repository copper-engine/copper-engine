/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.monitor.server;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import de.scoopgmbh.copper.monitor.core.adapter.ServerLogin;


public class SpringRemoteServerMainTest {
	
	@Test
	public void test_connect() throws Exception{
		new Thread(){
			@Override
			public void run() {
				new SpringRemoteServerMain().start(8080,"localhost");
			};
		}.start();
		Thread.sleep(5000);
		
		HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(ServerLogin.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://localhost:8080/serverLogin");
		httpInvokerProxyFactoryBean.setHttpInvokerRequestExecutor(new CommonsHttpInvokerRequestExecutor());
		httpInvokerProxyFactoryBean.afterPropertiesSet();
	    
	    ServerLogin serverLogin = (ServerLogin)httpInvokerProxyFactoryBean.getObject();
		assertNotNull(serverLogin);
//		serverLogin.login("", "").getAuditTrailMessage(6);
//		Registry registry = LocateRegistry.getRegistry("localhost",Registry.REGISTRY_PORT);
//		ServerLogin serverLogin = (ServerLogin) registry.lookup(ServerLogin.class.getSimpleName());
//		serverLogin.login("", "");
	}
}
