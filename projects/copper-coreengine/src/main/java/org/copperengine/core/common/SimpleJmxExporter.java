/*
 * Copyright 2002-2014 SCOOP Software GmbH
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

import java.util.Collections;
import java.util.Map;

import org.copperengine.management.AuditTrailMXBean;
import org.copperengine.management.AuditTrailQueryMXBean;
import org.copperengine.management.BatcherMXBean;
import org.copperengine.management.DatabaseDialectMXBean;
import org.copperengine.management.ProcessingEngineMXBean;
import org.copperengine.management.ProcessorPoolMXBean;
import org.copperengine.management.StatisticsCollectorMXBean;
import org.copperengine.management.WorkflowRepositoryMXBean;

/**
 * Simple helper bean to export some known COPPER MXBeans to the JMX MBeanServer. There's a setter for each MXBean you
 * want to export. You don't have to set all MXBeans, just the ones you are interested in.
 *
 * @author dmoebius
 */
public class SimpleJmxExporter extends AbstractJmxExporter {

    private WorkflowRepositoryMXBean workflowRepositoryMXBean;
    private ProcessingEngineMXBean processingEngineMXBean;
    private ProcessorPoolMXBean processorPoolMXBean;
    private StatisticsCollectorMXBean statisticsCollectorMXBean;
    private AuditTrailMXBean auditTrailMXBean;
    private BatcherMXBean batcherMXBean;
    private DatabaseDialectMXBean databaseDialectMXBean;
    private AuditTrailQueryMXBean auditTrailQueryMXBean;

    public void setWorkflowRepositoryMXBean(WorkflowRepositoryMXBean workflowRepositoryMXBean) {
        this.workflowRepositoryMXBean = workflowRepositoryMXBean;
    }

    public void setProcessingEngineMXBean(ProcessingEngineMXBean processingEngineMXBean) {
        this.processingEngineMXBean = processingEngineMXBean;
    }

    public void setProcessorPoolMXBean(ProcessorPoolMXBean processorPoolMXBean) {
        this.processorPoolMXBean = processorPoolMXBean;
    }

    public void setStatisticsCollectorMXBean(StatisticsCollectorMXBean statisticsCollectorMXBean) {
        this.statisticsCollectorMXBean = statisticsCollectorMXBean;
    }

    public void setAuditTrailMXBean(AuditTrailMXBean auditTrailMXBean) {
        this.auditTrailMXBean = auditTrailMXBean;
    }

    public void setBatcherMXBean(BatcherMXBean batcherMXBean) {
        this.batcherMXBean = batcherMXBean;
    }

    public void setDatabaseDialectMXBean(DatabaseDialectMXBean databaseDialectMXBean) {
        this.databaseDialectMXBean = databaseDialectMXBean;
    }

    public void setAuditTrailQueryMXBean(AuditTrailQueryMXBean auditTrailQueryMXBean) {
        this.auditTrailQueryMXBean = auditTrailQueryMXBean;
    }

    @Override
    protected Map<String, WorkflowRepositoryMXBean> getWorkflowRepositoryMXBeans() {
        return createSingletonMap("workflowRepositoryMXBean", workflowRepositoryMXBean);
    }

    @Override
    protected Map<String, ProcessingEngineMXBean> getProcessingEngineMXBeans() {
        return createSingletonMap("processingEngineMXBean", processingEngineMXBean);
    }

    @Override
    protected Map<String, ProcessorPoolMXBean> getProcessorPoolMXBeans() {
        return createSingletonMap("processorPoolMXBean", processorPoolMXBean);
    }

    @Override
    protected Map<String, StatisticsCollectorMXBean> getStatisticsCollectorMXBeans() {
        return createSingletonMap("statisticsCollectorMXBean", statisticsCollectorMXBean);
    }

    @Override
    protected Map<String, AuditTrailMXBean> getAuditTrailMXBeans() {
        return createSingletonMap("auditTrailMXBean", auditTrailMXBean);
    }

    @Override
    protected Map<String, BatcherMXBean> getBatcherMXBeans() {
        return createSingletonMap("batcherMXBean", batcherMXBean);
    }

    @Override
    protected Map<String, DatabaseDialectMXBean> getDatabaseDialectMXBeans() {
        return createSingletonMap("databaseDialectMXBean", databaseDialectMXBean);
    }

    @Override
    protected Map<String, AuditTrailQueryMXBean> getAuditTrailQueryMXBeans() {
        return createSingletonMap("auditTrailQueryMXBean", auditTrailQueryMXBean);
    }

    private static <T> Map<String, T> createSingletonMap(String key, T object) {
        if (object != null) {
            return Collections.singletonMap(key, object);
        } else {
            return Collections.emptyMap();
        }
    }
}
