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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.copperengine.management.AuditTrailMXBean;
import org.copperengine.management.AuditTrailQueryMXBean;
import org.copperengine.management.BatcherMXBean;
import org.copperengine.management.DBStorageMXBean;
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
 * @since 3.1
 */
public class SimpleJmxExporter extends AbstractJmxExporter {

    private Map<String, WorkflowRepositoryMXBean> workflowRepositoryMXBeans = new HashMap<>();
    private Map<String, ProcessingEngineMXBean> processingEngineMXBeans = new HashMap<>();
    private Map<String, ProcessorPoolMXBean> processorPoolMXBeans = new HashMap<>();
    private Map<String, StatisticsCollectorMXBean> statisticsCollectorMXBeans = new HashMap<>();
    private Map<String, AuditTrailMXBean> auditTrailMXBeans = new HashMap<>();
    private Map<String, BatcherMXBean> batcherMXBeans = new HashMap<>();
    private Map<String, DatabaseDialectMXBean> databaseDialectMXBeans = new HashMap<>();
    private Map<String, DBStorageMXBean> dbStorageMXBeans = new HashMap<>();
    private Map<String, AuditTrailQueryMXBean> auditTrailQueryMXBeans = new HashMap<>();

    public void addWorkflowRepositoryMXBean(String mxbeanName, WorkflowRepositoryMXBean workflowRepositoryMXBean) {
        this.workflowRepositoryMXBeans.put(mxbeanName, workflowRepositoryMXBean);
    }

    public void addProcessingEngineMXBean(String mxbeanName, ProcessingEngineMXBean processingEngineMXBean) {
        this.processingEngineMXBeans.put(mxbeanName, processingEngineMXBean);
    }

    public void addProcessorPoolMXBean(String mxbeanName, ProcessorPoolMXBean processorPoolMXBean) {
        this.processorPoolMXBeans.put(mxbeanName, processorPoolMXBean);
    }

    public void addStatisticsCollectorMXBean(String mxbeanName, StatisticsCollectorMXBean statisticsCollectorMXBean) {
        this.statisticsCollectorMXBeans.put(mxbeanName, statisticsCollectorMXBean);
    }

    public void addAuditTrailMXBean(String mxbeanName, AuditTrailMXBean auditTrailMXBean) {
        this.auditTrailMXBeans.put(mxbeanName, auditTrailMXBean);
    }

    public void addBatcherMXBean(String mxbeanName, BatcherMXBean batcherMXBean) {

        this.batcherMXBeans.put(mxbeanName, batcherMXBean);
    }

    public void addDatabaseDialectMXBean(String mxbeanName, DatabaseDialectMXBean databaseDialectMXBean) {
        this.databaseDialectMXBeans.put(mxbeanName, databaseDialectMXBean);
    }
    public void addDBStorageMXBean(String mxbeanName, DBStorageMXBean dbStorageMXBean) {
        this.dbStorageMXBeans.put(mxbeanName, dbStorageMXBean);
    }

    public void addAuditTrailQueryMXBean(String mxbeanName, AuditTrailQueryMXBean auditTrailQueryMXBean) {
        this.auditTrailQueryMXBeans.put(mxbeanName, auditTrailQueryMXBean);
    }

    @Override
    public Map<String, WorkflowRepositoryMXBean> getWorkflowRepositoryMXBeans() {
        return workflowRepositoryMXBeans;
    }

    public void setWorkflowRepositoryMXBeans(Map<String, WorkflowRepositoryMXBean> workflowRepositoryMXBeans) {
        this.workflowRepositoryMXBeans = workflowRepositoryMXBeans;
    }

    @Override
    public Map<String, ProcessingEngineMXBean> getProcessingEngineMXBeans() {
        return processingEngineMXBeans;
    }

    public void setProcessingEngineMXBeans(Map<String, ProcessingEngineMXBean> processingEngineMXBeans) {
        this.processingEngineMXBeans = processingEngineMXBeans;
    }

    @Override
    public Map<String, ProcessorPoolMXBean> getProcessorPoolMXBeans() {
        return processorPoolMXBeans;
    }

    public void setProcessorPoolMXBeans(Map<String, ProcessorPoolMXBean> processorPoolMXBeans) {
        this.processorPoolMXBeans = processorPoolMXBeans;
    }

    @Override
    public Map<String, StatisticsCollectorMXBean> getStatisticsCollectorMXBeans() {
        return statisticsCollectorMXBeans;
    }

    public void setStatisticsCollectorMXBeans(Map<String, StatisticsCollectorMXBean> statisticsCollectorMXBeans) {
        this.statisticsCollectorMXBeans = statisticsCollectorMXBeans;
    }

    @Override
    public Map<String, AuditTrailMXBean> getAuditTrailMXBeans() {
        return auditTrailMXBeans;
    }

    public void setAuditTrailMXBeans(Map<String, AuditTrailMXBean> auditTrailMXBeans) {
        this.auditTrailMXBeans = auditTrailMXBeans;
    }

    @Override
    public Map<String, BatcherMXBean> getBatcherMXBeans() {
        return batcherMXBeans;
    }

    public void setBatcherMXBeans(Map<String, BatcherMXBean> batcherMXBeans) {
        this.batcherMXBeans = batcherMXBeans;
    }

    @Override
    public Map<String, DatabaseDialectMXBean> getDatabaseDialectMXBeans() {
        return databaseDialectMXBeans;
    }

    public void setDatabaseDialectMXBeans(Map<String, DatabaseDialectMXBean> databaseDialectMXBeans) {
        this.databaseDialectMXBeans = databaseDialectMXBeans;
    }

    public Map<String, DBStorageMXBean> getDBStorageMXBeans() {
        return dbStorageMXBeans;
    }

    public void setDBStorageMXBeans(Map<String, DBStorageMXBean> dbStorageMXBeans) {
        this.dbStorageMXBeans = dbStorageMXBeans;
    }

    @Override
    public Map<String, AuditTrailQueryMXBean> getAuditTrailQueryMXBeans() {
        return auditTrailQueryMXBeans;
    }

    public void setAuditTrailQueryMXBeans(Map<String, AuditTrailQueryMXBean> auditTrailQueryMXBeans) {
        this.auditTrailQueryMXBeans = auditTrailQueryMXBeans;
    }

    private static <T> Map<String, T> createSingletonMap(String key, T object) {
        if (object != null) {
            return Collections.singletonMap(key, object);
        } else {
            return Collections.emptyMap();
        }
    }
}
