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
package org.copperengine.management;

import java.util.List;

import org.copperengine.management.model.EngineType;
import org.copperengine.management.model.WorkflowInfo;

public interface ProcessingEngineMXBean {
    public String getState();

    public String getEngineId();

    public EngineType getEngineType();

    public List<WorkflowInfo> queryWorkflowInstances();

    public List<WorkflowInfo> queryActiveWorkflowInstances(String className);

    public WorkflowInfo queryWorkflowInstance(String id);

    public int getNumberOfWorkflowInstances();

    public List<ProcessorPoolMXBean> getProcessorPools();

    public WorkflowRepositoryMXBean getWorkflowRepository();

    public String getDependencyInjectorType();

    public String getStatisticsCollectorType();
}