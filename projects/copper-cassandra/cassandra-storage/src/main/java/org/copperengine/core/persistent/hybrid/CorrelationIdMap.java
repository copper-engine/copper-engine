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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CorrelationIdMap {

    private final Object mutex = new Object();
    private final Map<String, String> correlationId2wfIdMap = new HashMap<>();
    private final Map<String, List<String>> wfId2correlationIdMap = new HashMap<>();

    public String getWorkflowId(String correlationId) {
        synchronized (mutex) {
            return correlationId2wfIdMap.get(correlationId);
        }
    }

    public boolean containsWorkflowId(String workflowId) {
        synchronized (mutex) {
            return wfId2correlationIdMap.containsKey(workflowId);
        }
    }

    public void removeAll4Workflow(String workflowId) {
        synchronized (mutex) {
            List<String> list = wfId2correlationIdMap.remove(workflowId);
            if (list == null || list.isEmpty())
                return;
            for (String cid : list) {
                correlationId2wfIdMap.remove(cid);
            }
        }
    }

    public void addCorrelationId(String workflowId, String correlationId) {
        synchronized (mutex) {
            List<String> list = wfId2correlationIdMap.remove(workflowId);
            if (list == null) {
                list = new ArrayList<String>();
                wfId2correlationIdMap.put(workflowId, list);
            }
            list.add(correlationId);
            correlationId2wfIdMap.put(correlationId, workflowId);
        }
    }

    public void addCorrelationIds(String workflowId, List<String> correlationIds) {
        synchronized (mutex) {
            List<String> list = wfId2correlationIdMap.remove(workflowId);
            if (list == null) {
                list = new ArrayList<String>(correlationIds.size());
                wfId2correlationIdMap.put(workflowId, list);
            }
            list.addAll(correlationIds);
            for (String cid : correlationIds) {
                correlationId2wfIdMap.put(cid, workflowId);
            }
        }
    }

    public void addCorrelationIds(String workflowId, String[] correlationIds) {
        synchronized (mutex) {
            List<String> list = wfId2correlationIdMap.remove(workflowId);
            if (list == null) {
                list = new ArrayList<String>(correlationIds.length);
                wfId2correlationIdMap.put(workflowId, list);
            }
            for (String cid : correlationIds) {
                list.add(cid);
                correlationId2wfIdMap.put(cid, workflowId);
            }
        }
    }

}
