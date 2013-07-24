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
import static org.junit.Assert.assertNull;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.spring.remoting.SecureRemoteInvocationFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import de.scoopgmbh.copper.monitoring.core.CopperMonitoringService;
import de.scoopgmbh.copper.monitoring.core.LoginService;
import de.scoopgmbh.copper.monitoring.server.testfixture.LogbackFixture;
import de.scoopgmbh.copper.monitoring.server.testfixture.LogbackFixture.NoErrorLogContentAssertion;

public class SpringRemotingServerTest {
	
	private static final String LOGIN_SERVICE = "http://localhost:8087/loginService";
	private static final String COPPER_MONITORING_SERVICE = "http://localhost:8087/copperMonitoringService";


	@BeforeClass
	public static void before(){
		LogManager.getRootLogger().setLevel(Level.INFO);
		new LogbackFixture().assertNoError(new NoErrorLogContentAssertion(){
			SpringRemotingServer springRemotingServer;
			@Override
			public void executeLogCreatingAction() {
				FutureTask<Void> futureTask = new FutureTask<Void>(new Runnable() {
					@Override
					public void run() {
						final SimpleAccountRealm realm = new SimpleAccountRealm();
						realm.addAccount("user1", "pass1");
						springRemotingServer = new SpringRemotingServer(CopperMonitorServiceSecurityProxy.secure(Mockito.mock(CopperMonitoringService.class))  ,8087,"localhost", new DefaultLoginService(realm));
						springRemotingServer.start();
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
			}
		});
	}
	
	@Test
	public void test_with_valid_user() throws Exception{
		
		String sessionId;
		{
			HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
			httpInvokerProxyFactoryBean.setServiceInterface(LoginService.class);
			httpInvokerProxyFactoryBean.setServiceUrl(LOGIN_SERVICE);
			httpInvokerProxyFactoryBean.setHttpInvokerRequestExecutor(new CommonsHttpInvokerRequestExecutor());
			httpInvokerProxyFactoryBean.afterPropertiesSet();
			LoginService loginService = (LoginService)httpInvokerProxyFactoryBean.getObject();
			sessionId = loginService.doLogin("user1", "pass1");
		}
	
		{
			final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
			httpInvokerProxyFactoryBean.setServiceInterface(CopperMonitoringService.class);
			httpInvokerProxyFactoryBean.setServiceUrl(COPPER_MONITORING_SERVICE);
			httpInvokerProxyFactoryBean.setRemoteInvocationFactory(new SecureRemoteInvocationFactory(sessionId));
			httpInvokerProxyFactoryBean.setHttpInvokerRequestExecutor(new CommonsHttpInvokerRequestExecutor());
			httpInvokerProxyFactoryBean.afterPropertiesSet();
			
			
			CopperMonitoringService copperMonitorService = (CopperMonitoringService)httpInvokerProxyFactoryBean.getObject();
			assertNotNull(copperMonitorService);
			try {
				copperMonitorService.getSettings();
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}
	
	}
	
	@Test(expected=RemoteAccessException.class)
	public void test_with_invalid_user() throws Exception{
		
		String sessionId;
		{
			HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
			httpInvokerProxyFactoryBean.setServiceInterface(LoginService.class);
			httpInvokerProxyFactoryBean.setServiceUrl(LOGIN_SERVICE);
			httpInvokerProxyFactoryBean.setHttpInvokerRequestExecutor(new CommonsHttpInvokerRequestExecutor());
			httpInvokerProxyFactoryBean.afterPropertiesSet();
			LoginService loginService = (LoginService)httpInvokerProxyFactoryBean.getObject();
			sessionId = loginService.doLogin("userXXXX", "passXXXX");
			assertNull(sessionId);
		}
	
		{
			final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
			httpInvokerProxyFactoryBean.setServiceInterface(CopperMonitoringService.class);
			httpInvokerProxyFactoryBean.setServiceUrl(COPPER_MONITORING_SERVICE);
			httpInvokerProxyFactoryBean.setRemoteInvocationFactory(new SecureRemoteInvocationFactory(sessionId));
			httpInvokerProxyFactoryBean.setHttpInvokerRequestExecutor(new CommonsHttpInvokerRequestExecutor());
			httpInvokerProxyFactoryBean.afterPropertiesSet();
			
			
			CopperMonitoringService copperMonitorService = (CopperMonitoringService)httpInvokerProxyFactoryBean.getObject();
			assertNotNull(copperMonitorService);
			try {
				copperMonitorService.getSettings();
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}
	
	}
	
	
	@Test(expected=RemoteAccessException.class)
	public void test_without_user() throws RemoteException{
		HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(CopperMonitoringService.class);
		httpInvokerProxyFactoryBean.setServiceUrl(COPPER_MONITORING_SERVICE);;
		httpInvokerProxyFactoryBean.setRemoteInvocationFactory(new SecureRemoteInvocationFactory("dgfdgdg"));
		httpInvokerProxyFactoryBean.setHttpInvokerRequestExecutor(new CommonsHttpInvokerRequestExecutor());
		httpInvokerProxyFactoryBean.afterPropertiesSet();
	    
		CopperMonitoringService copperMonitorService = (CopperMonitoringService)httpInvokerProxyFactoryBean.getObject();
		assertNotNull(copperMonitorService);
		copperMonitorService.getSettings();
	}
}
