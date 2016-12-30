package org.copperengine.core.persistent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

class IdCache {

    private static final class ResponseEntry {
        String correlationId;
        long ttlTS;

        public ResponseEntry(String responseId, String correlationId, long ttlTS) {
            this.correlationId = correlationId;
            this.ttlTS = ttlTS;
        }

    }

    private final Object mutex = new Object();
    private final Map<String, List<String>> cid2reponseId = new HashMap<String, List<String>>();
    private final LinkedHashMap<String, ResponseEntry> responseMap = new LinkedHashMap<>();
    private final int maxSize;
    private final long ttlMsec;

    public IdCache(int maxSize, long ttl, TimeUnit timeunit) {
        this.maxSize = maxSize;
        this.ttlMsec = timeunit.toMillis(ttl);
    }

    public void put(String responseId, String correlationId) {
        ResponseEntry entry = new ResponseEntry(responseId, correlationId, System.currentTimeMillis() + ttlMsec);
        synchronized (mutex) {
            if (responseMap.size() == maxSize) {
                Entry<String, ResponseEntry> head = responseMap.entrySet().iterator().next();
                __remove(head.getKey());
            }

            responseMap.put(responseId, entry);
            List<String> responseIds = cid2reponseId.get(correlationId);
            if (responseIds == null) {
                responseIds = new ArrayList<>(2);
                cid2reponseId.put(correlationId, responseIds);
            }
            responseIds.add(responseId);
        }
    }

    public boolean remove(String responseId) {
        synchronized (mutex) {
            return __remove(responseId);
        }
    }

    private boolean __remove(String responseId) {
        ResponseEntry entry = responseMap.remove(responseId);
        if (entry != null) {
            List<String> responseIds = cid2reponseId.get(entry.correlationId);
            if (responseIds != null) {
                responseIds.remove(responseId);
                if (responseIds.isEmpty()) {
                    cid2reponseId.remove(entry.correlationId);
                }
            }
            return true;
        }
        return false;
    }

    public boolean contains(String correlationId) {
        synchronized (mutex) {
            return __contains(correlationId);
        }
    }

    private boolean __contains(String correlationId) {
        List<String> responseIds = cid2reponseId.get(correlationId);
        if (responseIds == null)
            return false;

        for (String responseId : responseIds) {
            ResponseEntry entry = responseMap.get(responseId);
            if (entry.ttlTS < System.currentTimeMillis()) {
                // entry too old - ignored
            }
            else {
                return true;
            }
        }
        return false;
    }

    public boolean contains(String... correlationIds) {
        synchronized (mutex) {
            for (String cid : correlationIds) {
                if (!__contains(cid))
                    return false;
            }
            return true;
        }
    }

}
