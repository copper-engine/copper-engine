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
