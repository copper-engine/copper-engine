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
package de.scoopgmbh.copper.monitoring;

import java.util.concurrent.TimeUnit;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.common.PriorityProcessorPool;
import de.scoopgmbh.copper.common.WorkflowRepository;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo.EngineTyp;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessorPoolInfo;

/**
 * Collects copper configuration informations and runtime statistics of named measure points for the purpose of monitoring the performance of an application.
 * It depends on the implementation how runtime statistics are handled. 
 * 
 * @author austermann
 *
 */
public interface MonitoringDataCollector {
	public void submitMeasurePoint(String measurePointId, int elementCount, long elapsedTime, TimeUnit timeUnit);
	public void resgisterEngine(final String engineId, final EngineTyp typ, final ProcessingEngine engine);
	public void registerPool(final ProcessorPoolInfo processorPoolInfo, final String engineId, final PriorityProcessorPool pool);
	public void registerWorkflowRepository(WorkflowRepository workflowRepository);
}
