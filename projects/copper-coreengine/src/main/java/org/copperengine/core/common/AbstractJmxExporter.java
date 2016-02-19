/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.core.common;

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

import org.copperengine.management.AuditTrailMXBean;
import org.copperengine.management.AuditTrailQueryMXBean;
import org.copperengine.management.BatcherMXBean;
import org.copperengine.management.DatabaseDialectMXBean;
import org.copperengine.management.ProcessingEngineMXBean;
import org.copperengine.management.ProcessorPoolMXBean;
import org.copperengine.management.StatisticsCollectorMXBean;
import org.copperengine.management.WorkflowRepositoryMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for JMX bean exporters.
 *
 * @author dmoebius
 * @since 3.1
 */
public abstract class AbstractJmxExporter {

    private static final Logger logger = LoggerFactory.getLogger("org.copperengine.common.JmxExporter");

    private Set<ObjectName> objectNames = new HashSet<ObjectName>();
    private MBeanServer mBeanServer;

    public void startup() throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        mBeanServer = ManagementFactory.getPlatformMBeanServer();
        register(mBeanServer, getWorkflowRepositoryMXBeans(), "copper.workflowrepo");
        register(mBeanServer, getProcessingEngineMXBeans(), "copper.engine");
        register(mBeanServer, getProcessorPoolMXBeans(), "copper.processorpool");
        register(mBeanServer, getStatisticsCollectorMXBeans(), "copper.monitoring.statistics");
        register(mBeanServer, getAuditTrailMXBeans(), "copper.db");
        register(mBeanServer, getBatcherMXBeans(), "copper.db");
        register(mBeanServer, getDatabaseDialectMXBeans(), "copper.db");
        register(mBeanServer, getAuditTrailQueryMXBeans(), "copper.audittrail");
    }

    public void shutdown() throws MBeanRegistrationException, InstanceNotFoundException {
        for (ObjectName name : objectNames) {
            mBeanServer.unregisterMBean(name);
            logger.info("unregistered " + name);
        }
    }

    /** return a map with entries { "name" -> WorkflowRepositoryMXBean }. The map may be empty. */
    protected abstract Map<String, WorkflowRepositoryMXBean> getWorkflowRepositoryMXBeans();

    /** return a map with entries { "name" -> ProcessingEngineMXBean }. The map may be empty. */
    protected abstract Map<String, ProcessingEngineMXBean> getProcessingEngineMXBeans();

    /** return a map with entries { "name" -> ProcessorPoolMXBean }. The map may be empty. */
    protected abstract Map<String, ProcessorPoolMXBean> getProcessorPoolMXBeans();

    /** return a map with entries { "name" -> StatisticsCollectorMXBean }. The map may be empty. */
    protected abstract Map<String, StatisticsCollectorMXBean> getStatisticsCollectorMXBeans();

    /** return a map with entries { "name" -> AuditTrailMXBean }. The map may be empty. */
    protected abstract Map<String, AuditTrailMXBean> getAuditTrailMXBeans();

    /** return a map with entries { "name" -> BatcherMXBean }. The map may be empty. */
    protected abstract Map<String, BatcherMXBean> getBatcherMXBeans();

    /** return a map with entries { "name" -> DatabaseDialectMXBean }. The map may be empty. */
    protected abstract Map<String, DatabaseDialectMXBean> getDatabaseDialectMXBeans();

    /** return a map with entries { "name" -> AuditTrailQueryMXBean }. The map may be empty. */
    protected abstract Map<String, AuditTrailQueryMXBean> getAuditTrailQueryMXBeans();

    private void register(MBeanServer mBeanServer, Map<String, ?> map, String domain) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            ObjectName name = new ObjectName(domain, "name", entry.getKey());
            mBeanServer.registerMBean(entry.getValue(), name);
            objectNames.add(name);
            logger.info("registered at JMX: " + name.toString());
        }
    }

}
