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

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.copperengine.core.ProcessingState;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.slf4j.Logger;

import com.google.common.util.concurrent.ListenableFuture;

public class StorageCache implements Storage {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(StorageCache.class);

    private final Storage delegate;
    private final Map<String, SoftReference<WorkflowInstance>> wfCache;
    private final Map<String, SoftReference<String>> earCache;
    private final CacheStats cacheStatsWfCache = new CacheStats();
    private final CacheStats cacheStatsEarCache = new CacheStats();

    public StorageCache(Storage delegate) {
        if (delegate == null)
            throw new IllegalArgumentException("delegate");
        this.delegate = delegate;

        wfCache = new ConcurrentHashMap<>();
        earCache = new ConcurrentHashMap<>();
    }

    public void logCacheStats() {
        logger.info("cacheStatsWfCache  = {}", cacheStatsWfCache);
        logger.info("cacheStatsEarCache = {}", cacheStatsEarCache);
    }

    @Override
    public void safeWorkflowInstance(WorkflowInstance wfi, boolean initialInsert) throws Exception {
        wfCache.put(wfi.id, new SoftReference<WorkflowInstance>(wfi));
        delegate.safeWorkflowInstance(wfi, initialInsert);
    }

    @Override
    public ListenableFuture<Void> deleteWorkflowInstance(String wfId) throws Exception {
        wfCache.remove(wfId);
        return delegate.deleteWorkflowInstance(wfId);
    }

    @Override
    public WorkflowInstance readWorkflowInstance(String wfId) throws Exception {
        SoftReference<WorkflowInstance> entry = wfCache.get(wfId);
        if (entry != null) {
            WorkflowInstance wfi = entry.get();
            if (wfi != null) {
                cacheStatsWfCache.incNumberOfReads(true);
                return wfi;
            }
        }
        WorkflowInstance wfi = delegate.readWorkflowInstance(wfId);
        if (wfi != null) {
            wfCache.put(wfi.id, new SoftReference<WorkflowInstance>(wfi));
        }
        cacheStatsWfCache.incNumberOfReads(false);
        return wfi;
    }

    @Override
    public void initialize(HybridDBStorageAccessor internalStorageAccessor, int numberOfThreads) throws Exception {
        delegate.initialize(internalStorageAccessor, numberOfThreads);
    }

    @Override
    public ListenableFuture<Void> safeEarlyResponse(String correlationId, String serializedResponse) throws Exception {
        earCache.put(correlationId, new SoftReference<String>(serializedResponse));
        return delegate.safeEarlyResponse(correlationId, serializedResponse);
    }

    @Override
    public String readEarlyResponse(String correlationId) throws Exception {
        final SoftReference<String> entry = earCache.get(correlationId);
        if (entry != null) {
            String resp = entry.get();
            if (resp != null) {
                cacheStatsEarCache.incNumberOfReads(true);
                return resp;
            }
        }
        cacheStatsEarCache.incNumberOfReads(false);
        return delegate.readEarlyResponse(correlationId);
    }

    @Override
    public ListenableFuture<Void> deleteEarlyResponse(String correlationId) throws Exception {
        earCache.remove(correlationId);
        return delegate.deleteEarlyResponse(correlationId);
    }

    @Override
    public ListenableFuture<Void> updateWorkflowInstanceState(String wfId, ProcessingState state) throws Exception {
        wfCache.remove(wfId);
        return delegate.updateWorkflowInstanceState(wfId, state);
    }

    @Override
    public List<WorkflowInstance> queryWorkflowInstances(WorkflowInstanceFilter filter) throws Exception {
        return delegate.queryWorkflowInstances(filter);
    }

    @Override
    public int countWorkflowInstances(WorkflowInstanceFilter filter) throws Exception {
        return delegate.countWorkflowInstances(filter);
    }

}
