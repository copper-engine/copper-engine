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
package de.scoopgmbh.copper.monitoring.server;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import de.scoopgmbh.copper.monitoring.core.CopperMonitorInterface;
import de.scoopgmbh.copper.monitoring.server.testfixture.LogFixture;
import de.scoopgmbh.copper.monitoring.server.testfixture.LogFixture.NoErrorLogContentAssertion;
import de.scoopgmbh.copper.monitoring.server.testfixture.SleepUtil;

@Ignore //TODO why xml not found on build server
public class SpringRemoteServerMainTest {
	
	@Test
	public void test_connect() throws Exception{
		
		new LogFixture().assertNoError(new NoErrorLogContentAssertion(){
			@Override
			public void executeLogCreatingAction() {
				FutureTask<Void> futureTask = new FutureTask<Void>(new Runnable() {
					@Override
					public void run() {
						new SpringRemoteServerMain(CopperDataProviderMock.copperMonitorInterfaceFactory,8087,"localhost").start();
					}
				},null);
				
				Thread thread=new Thread(futureTask);
				thread.start();
				
				try {
					futureTask.get();//dont swallow exception instead fail test
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
				
				SleepUtil.sleep(3000);
			}
		});
	
		
		HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(CopperMonitorInterface.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://localhost:8087/copperMonitorInterface");
		httpInvokerProxyFactoryBean.setHttpInvokerRequestExecutor(new CommonsHttpInvokerRequestExecutor());
		httpInvokerProxyFactoryBean.afterPropertiesSet();
	    
		CopperMonitorInterface serverLogin1 = (CopperMonitorInterface)httpInvokerProxyFactoryBean.getObject();
		assertNotNull(serverLogin1);
		serverLogin1.doLogin("", "");
		
		CopperMonitorInterface serverLogin2 = (CopperMonitorInterface)httpInvokerProxyFactoryBean.getObject();
		assertNotNull(serverLogin2);
		serverLogin2.doLogin("", "");
//		Assert.assertNotEquals(serverLogin1.toString(), serverLogin2.toString());
		
	}
}
