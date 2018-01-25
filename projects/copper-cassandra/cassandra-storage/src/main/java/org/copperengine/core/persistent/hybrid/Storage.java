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
package org.copperengine.core.persistent.hybrid;

import java.util.List;

import org.copperengine.core.ProcessingState;
import org.copperengine.management.model.WorkflowInstanceFilter;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Map-like persistent storage for a {@link HybridDBStorage}
 * 
 * @author austermann
 *
 */
public interface Storage {

    public void safeWorkflowInstance(WorkflowInstance cw, boolean initialInsert) throws Exception;

    public ListenableFuture<Void> deleteWorkflowInstance(String wfId) throws Exception;

    public WorkflowInstance readWorkflowInstance(String wfId) throws Exception;

    public void initialize(HybridDBStorageAccessor internalStorageAccessor, int numberOfThreads) throws Exception;

    public ListenableFuture<Void> safeEarlyResponse(String correlationId, String serializedResponse) throws Exception;

    public String readEarlyResponse(String correlationId) throws Exception;

    public ListenableFuture<Void> deleteEarlyResponse(String correlationId) throws Exception;

    public ListenableFuture<Void> updateWorkflowInstanceState(String wfId, ProcessingState state) throws Exception;
    
    public List<WorkflowInstance> queryWorkflowInstances(WorkflowInstanceFilter filter) throws Exception;

    public int countWorkflowInstances(WorkflowInstanceFilter filter) throws Exception;

}
