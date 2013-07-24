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

import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.remoting.SecureRemoteInvocationExecutor;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.NetworkTrafficSelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import de.scoopgmbh.copper.audit.MessagePostProcessor;
import de.scoopgmbh.copper.management.ProcessingEngineMXBean;
import de.scoopgmbh.copper.monitoring.LoggingStatisticCollector;
import de.scoopgmbh.copper.monitoring.core.CopperMonitoringService;
import de.scoopgmbh.copper.monitoring.core.LoginService;
import de.scoopgmbh.copper.monitoring.server.debug.WorkflowInstanceIntrospector;
import de.scoopgmbh.copper.monitoring.server.logging.LogConfigManager;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataAccessQueue;
import de.scoopgmbh.copper.monitoring.server.persistent.DerbyMonitoringDbDialect;
import de.scoopgmbh.copper.monitoring.server.persistent.MonitoringDbStorage;
import de.scoopgmbh.copper.monitoring.server.persistent.OracleMonitoringDbDialect;
import de.scoopgmbh.copper.persistent.StandardJavaSerializer;
import de.scoopgmbh.copper.persistent.txn.TransactionController;

public class SpringRemotingServer {
	
	static final Logger logger = LoggerFactory.getLogger(SpringRemotingServer.class);
	private Server server;
	private final CopperMonitoringService copperMonitoringService;
	private final int port;
	private final String host; 
	private final DefaultLoginService loginService;
	
	public static SpringRemotingServer createWithDefaultsForDerby(List<ProcessingEngineMXBean> engines,
			MonitoringDataAccessQueue monitoringQueue,
			Realm realm, LoggingStatisticCollector runtimeStatisticsCollector,
			TransactionController transactionController,
			WorkflowInstanceIntrospector introspector,
			MessagePostProcessor messagePostProcessor,
			LogConfigManager logConfigManager){
		CopperMonitoringService copperMonitoringService = new DefaultCopperMonitoringService(
				new MonitoringDbStorage(transactionController,new DerbyMonitoringDbDialect(new StandardJavaSerializer())),
				runtimeStatisticsCollector,
				engines,
				monitoringQueue, 
				true,
				messagePostProcessor,
				introspector,
				logConfigManager);
	
		return new SpringRemotingServer(CopperMonitorServiceSecurityProxy.secure(copperMonitoringService), 8080,"localhost", new DefaultLoginService(realm));
	}
	
	public static SpringRemotingServer createWithDefaultsForOracle(
			List<ProcessingEngineMXBean> engines,
			MonitoringDataAccessQueue monitoringQueue,
			Realm realm,
			LoggingStatisticCollector runtimeStatisticsCollector,
			TransactionController transactionController,
			WorkflowInstanceIntrospector introspector,
			MessagePostProcessor messagePostProcessor,
			LogConfigManager logConfigManager){
		CopperMonitoringService copperMonitoringService = new DefaultCopperMonitoringService(
				new MonitoringDbStorage(transactionController,new OracleMonitoringDbDialect(new StandardJavaSerializer())),
				runtimeStatisticsCollector,
				engines,
				monitoringQueue, 
				true,
				messagePostProcessor,
				introspector,
				logConfigManager);
	
		return new SpringRemotingServer(CopperMonitorServiceSecurityProxy.secure(copperMonitoringService), 8080, "localhost", new DefaultLoginService(realm));
	}
	
	public SpringRemotingServer(CopperMonitoringService copperMonitoringService, int port, String host, DefaultLoginService loginService) {
		super();
		this.copperMonitoringService = copperMonitoringService;
		this.port = port;
		this.host = host;
		this.loginService = loginService;
	}

	public void start() {
		logger.info("Starting Copper-Monitor-Server (jetty)");

		server = new Server();
		NetworkTrafficSelectChannelConnector connector = new NetworkTrafficSelectChannelConnector();
		connector.setPort(port);
		connector.setHost(host);
//		connector.addNetworkTrafficListener();
		
		server.setConnectors(new Connector[] { connector });

		ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/", true, false);
	
		//Servlet adress is defined with the bean name
		//try to avoid xml config (dont sacrifice type safety)
		GenericWebApplicationContext genericWebApplicationContext = new GenericWebApplicationContext();
		genericWebApplicationContext.registerBeanDefinition("/loginService",
				BeanDefinitionBuilder.genericBeanDefinition(HttpInvokerServiceExporter.class).
				addPropertyValue("service", loginService).
				addPropertyValue("serviceInterface", LoginService.class.getName()).
				getBeanDefinition());
		genericWebApplicationContext.registerBeanDefinition("/copperMonitoringService",
				BeanDefinitionBuilder.genericBeanDefinition(HttpInvokerServiceExporter.class).
				addPropertyValue("service", copperMonitoringService).
				addPropertyValue("serviceInterface", CopperMonitoringService.class.getName()).
				addPropertyValue("remoteInvocationExecutor", createSecureRemoteInvocationExecutor()).
				getBeanDefinition());
		genericWebApplicationContext.refresh();
		
		DispatcherServlet dispatcherServlet = new DispatcherServlet(genericWebApplicationContext);
		ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
		servletContextHandler.addServlet(servletHolder, "/*");
		
		FilterHolder filterHolder = new FilterHolder();
		GzipFilter filter = new GzipFilter();
		filterHolder.setFilter(filter);
		EnumSet<DispatcherType> types = EnumSet.allOf(DispatcherType.class);
		servletContextHandler.addFilter(filterHolder, "/*", types);
		
		HandlerCollection handlers = new HandlerCollection();
		final RequestLogHandler requestLogHandler = new RequestLogHandler();
		NCSARequestLog requestLog = new NCSARequestLog();
		requestLog.setAppend(true);
		requestLog.setExtended(true);
		requestLog.setLogLatency(true);
		requestLogHandler.setRequestLog(requestLog);
		handlers.setHandlers(new Handler[] {servletContextHandler, requestLogHandler});
		server.setHandler(handlers);
		
	
		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private SecureRemoteInvocationExecutor createSecureRemoteInvocationExecutor(){
		final SecureRemoteInvocationExecutor secureRemoteInvocationExecutor = new SecureRemoteInvocationExecutor();
		return secureRemoteInvocationExecutor;
	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isRunning() {
		return server.isRunning();
	}

}
