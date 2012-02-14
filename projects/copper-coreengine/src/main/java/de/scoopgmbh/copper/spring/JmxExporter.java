/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.spring;

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import de.scoopgmbh.copper.management.AuditTrailMXBean;
import de.scoopgmbh.copper.management.BatcherMXBean;
import de.scoopgmbh.copper.management.ProcessingEngineMXBean;
import de.scoopgmbh.copper.management.ProcessorPoolMXBean;
import de.scoopgmbh.copper.management.StatisticsCollectorMXBean;


/**
 * Exports all COPPER MXBeans to the JMX MBeanServer.
 * 
 * @author austermann
 *
 */
public class JmxExporter implements ApplicationContextAware {
	
	private static final Logger logger = LoggerFactory.getLogger(JmxExporter.class);

	private ApplicationContext applicationContext;
	private Set<ObjectName> objectNames = new HashSet<ObjectName>();
	private MBeanServer mBeanServer;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public void startup() throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		mBeanServer = ManagementFactory.getPlatformMBeanServer();
		register(mBeanServer, applicationContext.getBeansOfType(ProcessingEngineMXBean.class), "copper.engine");
		register(mBeanServer, applicationContext.getBeansOfType(ProcessorPoolMXBean.class), "copper.processorpool");
		register(mBeanServer, applicationContext.getBeansOfType(StatisticsCollectorMXBean.class), "copper.monitoring.statistics");
		register(mBeanServer, applicationContext.getBeansOfType(AuditTrailMXBean.class), "copper.db");
		register(mBeanServer, applicationContext.getBeansOfType(BatcherMXBean.class), "copper.db");
	}

	private void register(MBeanServer mBeanServer, Map<?,?> map, String domain) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			ObjectName name = new ObjectName(domain, "name", entry.getKey().toString());
			mBeanServer.registerMBean(entry.getValue(), name );
			objectNames.add(name);
			logger.info("registered at JMX: "+name.toString());
		}
	}
	
	public void shutdown() throws MBeanRegistrationException, InstanceNotFoundException {
		for (ObjectName name : objectNames) {
			mBeanServer.unregisterMBean(name);
			logger.info("unregistered "+name);
		}
	}

	
}
