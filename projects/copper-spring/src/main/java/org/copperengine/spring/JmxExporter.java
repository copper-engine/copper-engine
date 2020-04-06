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
package org.copperengine.spring;

import java.util.Map;

import org.copperengine.core.common.AbstractJmxExporter;
import org.copperengine.management.AuditTrailMXBean;
import org.copperengine.management.AuditTrailQueryMXBean;
import org.copperengine.management.BatcherMXBean;
import org.copperengine.management.DBStorageMXBean;
import org.copperengine.management.DatabaseDialectMXBean;
import org.copperengine.management.ProcessingEngineMXBean;
import org.copperengine.management.ProcessorPoolMXBean;
import org.copperengine.management.StatisticsCollectorMXBean;
import org.copperengine.management.WorkflowRepositoryMXBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Automatically exports <em>all</em> COPPER MXBeans, which are available in the Spring Application Context, to the JMX
 * MBeanServer.
 *
 * @author austermann
 */
public class JmxExporter extends AbstractJmxExporter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected Map<String, WorkflowRepositoryMXBean> getWorkflowRepositoryMXBeans() {
        return applicationContext.getBeansOfType(WorkflowRepositoryMXBean.class);
    }

    @Override
    protected Map<String, ProcessingEngineMXBean> getProcessingEngineMXBeans() {
        return applicationContext.getBeansOfType(ProcessingEngineMXBean.class);
    }

    @Override
    protected Map<String, ProcessorPoolMXBean> getProcessorPoolMXBeans() {
        return applicationContext.getBeansOfType(ProcessorPoolMXBean.class);
    }

    @Override
    protected Map<String, StatisticsCollectorMXBean> getStatisticsCollectorMXBeans() {
        return applicationContext.getBeansOfType(StatisticsCollectorMXBean.class);
    }

    @Override
    protected Map<String, AuditTrailMXBean> getAuditTrailMXBeans() {
        return applicationContext.getBeansOfType(AuditTrailMXBean.class);
    }

    @Override
    protected Map<String, BatcherMXBean> getBatcherMXBeans() {
        return applicationContext.getBeansOfType(BatcherMXBean.class);
    }

    @Override
    protected Map<String, DatabaseDialectMXBean> getDatabaseDialectMXBeans() {
        return applicationContext.getBeansOfType(DatabaseDialectMXBean.class);
    }

    @Override
    protected Map<String, DBStorageMXBean> getDBStorageMXBeans() {
        return applicationContext.getBeansOfType(DBStorageMXBean.class);
    }

    @Override
    protected Map<String, AuditTrailQueryMXBean> getAuditTrailQueryMXBeans() {
        return applicationContext.getBeansOfType(AuditTrailQueryMXBean.class);
    }

}
