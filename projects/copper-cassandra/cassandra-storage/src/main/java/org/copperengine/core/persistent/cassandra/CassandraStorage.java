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
package org.copperengine.core.persistent.cassandra;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.NullArgumentException;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.ProcessingState;
import org.copperengine.core.WaitMode;
import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.core.persistent.SerializedWorkflow;
import org.copperengine.core.persistent.hybrid.HybridDBStorageAccessor;
import org.copperengine.core.persistent.hybrid.Storage;
import org.copperengine.core.persistent.hybrid.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.LoggingRetryPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Implementation of the {@link Storage} interface backed by a Apache Cassandra DB.
 * 
 * @author austermann
 *
 */
public class CassandraStorage implements Storage {

    private static final Logger logger = LoggerFactory.getLogger(CassandraStorage.class);

    private static final String CQL_UPD_WORKFLOW_INSTANCE_NOT_WAITING = "UPDATE COP_WORKFLOW_INSTANCE SET PPOOL_ID=?, PRIO=?, CREATION_TS=?, DATA=?, OBJECT_STATE=?, STATE=? WHERE ID=?";
    private static final String CQL_UPD_WORKFLOW_INSTANCE_WAITING = "UPDATE COP_WORKFLOW_INSTANCE SET PPOOL_ID=?, PRIO=?, CREATION_TS=?, DATA=?, OBJECT_STATE=?, WAIT_MODE=?, TIMEOUT=?, RESPONSE_MAP_JSON=?, STATE=? WHERE ID=?";
    private static final String CQL_UPD_WORKFLOW_INSTANCE_STATE = "UPDATE COP_WORKFLOW_INSTANCE SET STATE=? WHERE ID=?";
    private static final String CQL_UPD_WORKFLOW_INSTANCE_STATE_AND_RESPONSE_MAP = "UPDATE COP_WORKFLOW_INSTANCE SET STATE=?, RESPONSE_MAP_JSON=?  WHERE ID=?";
    private static final String CQL_DEL_WORKFLOW_INSTANCE_WAITING = "DELETE FROM COP_WORKFLOW_INSTANCE WHERE ID=?";
    private static final String CQL_SEL_WORKFLOW_INSTANCE = "SELECT * FROM COP_WORKFLOW_INSTANCE WHERE ID=?";
    private static final String CQL_INS_EARLY_RESPONSE = "INSERT INTO COP_EARLY_RESPONSE (CORRELATION_ID, RESPONSE) VALUES (?,?) USING TTL ?";
    private static final String CQL_DEL_EARLY_RESPONSE = "DELETE FROM COP_EARLY_RESPONSE WHERE CORRELATION_ID=?";
    private static final String CQL_SEL_EARLY_RESPONSE = "SELECT RESPONSE FROM COP_EARLY_RESPONSE WHERE CORRELATION_ID=?";
    private static final String CQL_INS_WFI_ID = "INSERT INTO COP_WFI_ID (ID) VALUES (?)";
    private static final String CQL_DEL_WFI_ID = "DELETE FROM COP_WFI_ID WHERE ID=?";
    private static final String CQL_SEL_WFI_ID_ALL = "SELECT * FROM COP_WFI_ID";

    private final Executor executor;
    private final Session session;
    private final Cluster cluster;
    private final Map<String, PreparedStatement> preparedStatements = new HashMap<>();
    private final JsonMapper jsonMapper = new JsonMapperImpl();
    private final ConsistencyLevel consistencyLevel;
    private final RuntimeStatisticsCollector runtimeStatisticsCollector;
    private final RetryPolicy alwaysRetry = new LoggingRetryPolicy(new AlwaysRetryPolicy());
    private int ttlEarlyResponseSeconds = 1 * 24 * 60 * 60; // one day
    private int initializationTimeoutSeconds = 1 * 24 * 60 * 60; // one day
    private boolean createSchemaOnStartup = true;

    public CassandraStorage(final CassandraSessionManager sessionManager, final Executor executor, final RuntimeStatisticsCollector runtimeStatisticsCollector) {
        this(sessionManager, executor, runtimeStatisticsCollector, ConsistencyLevel.LOCAL_QUORUM);

    }

    public CassandraStorage(final CassandraSessionManager sessionManager, final Executor executor, final RuntimeStatisticsCollector runtimeStatisticsCollector, final ConsistencyLevel consistencyLevel) {
        if (sessionManager == null)
            throw new NullArgumentException("sessionManager");

        if (consistencyLevel == null)
            throw new NullArgumentException("consistencyLevel");

        if (executor == null)
            throw new NullArgumentException("executor");

        if (runtimeStatisticsCollector == null)
            throw new NullArgumentException("runtimeStatisticsCollector");

        this.executor = executor;
        this.consistencyLevel = consistencyLevel;
        this.session = sessionManager.getSession();
        this.cluster = sessionManager.getCluster();
        this.runtimeStatisticsCollector = runtimeStatisticsCollector;

    }

    public void setCreateSchemaOnStartup(boolean createSchemaOnStartup) {
        this.createSchemaOnStartup = createSchemaOnStartup;
    }

    protected void prepareStatements() throws Exception {
        prepare(CQL_UPD_WORKFLOW_INSTANCE_NOT_WAITING);
        prepare(CQL_UPD_WORKFLOW_INSTANCE_WAITING);
        prepare(CQL_DEL_WORKFLOW_INSTANCE_WAITING);
        prepare(CQL_SEL_WORKFLOW_INSTANCE);
        prepare(CQL_UPD_WORKFLOW_INSTANCE_STATE);
        prepare(CQL_INS_EARLY_RESPONSE);
        prepare(CQL_DEL_EARLY_RESPONSE);
        prepare(CQL_SEL_EARLY_RESPONSE);
        prepare(CQL_UPD_WORKFLOW_INSTANCE_STATE_AND_RESPONSE_MAP);
        prepare(CQL_INS_WFI_ID);
        prepare(CQL_DEL_WFI_ID);
        prepare(CQL_SEL_WFI_ID_ALL, DefaultRetryPolicy.INSTANCE);
    }

    protected void createSchema(Session session, Cluster cluster) throws Exception {
        if (!createSchemaOnStartup)
            return;

        final KeyspaceMetadata metaData = cluster.getMetadata().getKeyspace(session.getLoggedKeyspace());
        if (metaData.getTable("COP_WORKFLOW_INSTANCE") != null) {
            logger.info("skipping schema creation");
            return;
        }

        logger.info("Creating tables...");
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(CassandraStorage.class.getResourceAsStream("copper-schema.cql")))) {
            StringBuilder cql = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                if (line.startsWith("--"))
                    continue;
                if (line.endsWith(";")) {
                    if (line.length() > 1)
                        cql.append(line.substring(0, line.length() - 1));
                    String cqlCmd = cql.toString();
                    cql = new StringBuilder();
                    logger.info("Executing CQL {}", cqlCmd);
                    session.execute(cqlCmd);
                }
                else {
                    cql.append(line).append(" ");
                }
            }
        }

    }

    public void setTtlEarlyResponseSeconds(int ttlEarlyResponseSeconds) {
        if (ttlEarlyResponseSeconds <= 0)
            throw new IllegalArgumentException();
        this.ttlEarlyResponseSeconds = ttlEarlyResponseSeconds;
    }

    public void setInitializationTimeoutSeconds(int initializationTimeoutSeconds) {
        if (initializationTimeoutSeconds <= 0)
            throw new IllegalArgumentException();
        this.initializationTimeoutSeconds = initializationTimeoutSeconds;
    }

    @Override
    public void safeWorkflowInstance(final WorkflowInstance cw, final boolean initialInsert) throws Exception {
        logger.debug("safeWorkflow({})", cw);
        new CassandraOperation<Void>(logger) {
            @Override
            protected Void execute() throws Exception {
                if (initialInsert) {
                    final PreparedStatement pstmt = preparedStatements.get(CQL_INS_WFI_ID);
                    final long startTS = System.nanoTime();
                    session.execute(pstmt.bind(cw.id));
                    runtimeStatisticsCollector.submit("wfii.ins", 1, System.nanoTime() - startTS, TimeUnit.NANOSECONDS);
                }
                if (cw.cid2ResponseMap == null || cw.cid2ResponseMap.isEmpty()) {
                    final PreparedStatement pstmt = preparedStatements.get(CQL_UPD_WORKFLOW_INSTANCE_NOT_WAITING);
                    final long startTS = System.nanoTime();
                    session.execute(pstmt.bind(cw.ppoolId, cw.prio, cw.creationTS, cw.serializedWorkflow.getData(), cw.serializedWorkflow.getObjectState(), cw.state.name(), cw.id));
                    runtimeStatisticsCollector.submit("wfi.update.nowait", 1, System.nanoTime() - startTS, TimeUnit.NANOSECONDS);
                }
                else {
                    final PreparedStatement pstmt = preparedStatements.get(CQL_UPD_WORKFLOW_INSTANCE_WAITING);
                    final String responseMapJson = jsonMapper.toJSON(cw.cid2ResponseMap);
                    final long startTS = System.nanoTime();
                    session.execute(pstmt.bind(cw.ppoolId, cw.prio, cw.creationTS, cw.serializedWorkflow.getData(), cw.serializedWorkflow.getObjectState(), cw.waitMode.name(), cw.timeout, responseMapJson, cw.state.name(), cw.id));
                    runtimeStatisticsCollector.submit("wfi.update.wait", 1, System.nanoTime() - startTS, TimeUnit.NANOSECONDS);
                }
                return null;
            }
        }.run();
    }

    @Override
    public ListenableFuture<Void> deleteWorkflowInstance(String wfId) throws Exception {
        logger.debug("deleteWorkflowInstance({})", wfId);
        session.executeAsync(preparedStatements.get(CQL_DEL_WFI_ID).bind(wfId));
        final PreparedStatement pstmt = preparedStatements.get(CQL_DEL_WORKFLOW_INSTANCE_WAITING);
        final long startTS = System.nanoTime();
        final ResultSetFuture rsf = session.executeAsync(pstmt.bind(wfId));
        return createSettableFuture(rsf, "wfi.delete", startTS);
    }

    private SettableFuture<Void> createSettableFuture(final ResultSetFuture rsf, final String mpId, final long startTsNanos) {
        final SettableFuture<Void> rv = SettableFuture.create();
        rsf.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    runtimeStatisticsCollector.submit(mpId, 1, System.nanoTime() - startTsNanos, TimeUnit.NANOSECONDS);
                    rsf.get();
                    rv.set(null);
                } catch (InterruptedException e) {
                    rv.setException(e);
                } catch (ExecutionException e) {
                    rv.setException(e.getCause());
                }

            }
        }, executor);
        return rv;
    }

    @Override
    public WorkflowInstance readWorkflowInstance(final String wfId) throws Exception {
        logger.debug("readCassandraWorkflow({})", wfId);
        return new CassandraOperation<WorkflowInstance>(logger) {
            @Override
            protected WorkflowInstance execute() throws Exception {
                final PreparedStatement pstmt = preparedStatements.get(CQL_SEL_WORKFLOW_INSTANCE);
                final long startTS = System.nanoTime();
                ResultSet rs = session.execute(pstmt.bind(wfId));
                Row row = rs.one();
                if (row == null) {
                    return null;
                }
                final WorkflowInstance cw = new WorkflowInstance();
                cw.id = wfId;
                cw.ppoolId = row.getString("PPOOL_ID");
                cw.prio = row.getInt("PRIO");
                cw.creationTS = row.getDate("CREATION_TS");
                cw.timeout = row.getDate("TIMEOUT");
                cw.waitMode = toWaitMode(row.getString("WAIT_MODE"));
                cw.serializedWorkflow = new SerializedWorkflow();
                cw.serializedWorkflow.setData(row.getString("DATA"));
                cw.serializedWorkflow.setObjectState(row.getString("OBJECT_STATE"));
                cw.cid2ResponseMap = toResponseMap(row.getString("RESPONSE_MAP_JSON"));
                cw.state = ProcessingState.valueOf(row.getString("STATE"));
                runtimeStatisticsCollector.submit("wfi.read", 1, System.nanoTime() - startTS, TimeUnit.NANOSECONDS);
                return cw;
            }
        }.run();
    }

    @Override
    public ListenableFuture<Void> safeEarlyResponse(String correlationId, String serializedResponse) throws Exception {
        logger.debug("safeEarlyResponse({})", correlationId);
        final long startTS = System.nanoTime();
        final ResultSetFuture rsf = session.executeAsync(preparedStatements.get(CQL_INS_EARLY_RESPONSE).bind(correlationId, serializedResponse, ttlEarlyResponseSeconds));
        return createSettableFuture(rsf, "ear.insert", startTS);
    }

    @Override
    public String readEarlyResponse(final String correlationId) throws Exception {
        logger.debug("readEarlyResponse({})", correlationId);
        return new CassandraOperation<String>(logger) {
            @Override
            protected String execute() throws Exception {
                final long startTS = System.nanoTime();
                final ResultSet rs = session.execute(preparedStatements.get(CQL_SEL_EARLY_RESPONSE).bind(correlationId));
                Row row = rs.one();
                runtimeStatisticsCollector.submit("ear.read", 1, System.nanoTime() - startTS, TimeUnit.NANOSECONDS);
                if (row != null) {
                    logger.debug("early response with correlationId {} found!", correlationId);
                    return row.getString("RESPONSE");
                }
                return null;
            }
        }.run();
    }

    @Override
    public ListenableFuture<Void> deleteEarlyResponse(String correlationId) throws Exception {
        logger.debug("deleteEarlyResponse({})", correlationId);
        final long startTS = System.nanoTime();
        final ResultSetFuture rsf = session.executeAsync(preparedStatements.get(CQL_DEL_EARLY_RESPONSE).bind(correlationId));
        return createSettableFuture(rsf, "ear.delete", startTS);
    }

    @Override
    public void initialize(final HybridDBStorageAccessor internalStorageAccessor, int numberOfThreads) throws Exception {
        createSchema(session, cluster);

        prepareStatements();

        // TODO instead of blocking the startup until all active workflow instances are read and resumed, it is
        // sufficient to read just their existing IDs in COP_WFI_ID and resume them in the background while already
        // starting the engine an accepting new instances.

        if (numberOfThreads <= 0)
            numberOfThreads = 1;
        logger.info("Starting to initialize with {} threads ...", numberOfThreads);
        final ExecutorService execService = Executors.newFixedThreadPool(numberOfThreads);
        final long startTS = System.currentTimeMillis();
        final ResultSet rs = session.execute(preparedStatements.get(CQL_SEL_WFI_ID_ALL).bind().setFetchSize(500).setConsistencyLevel(ConsistencyLevel.ONE));
        int counter = 0;
        Row row;
        while ((row = rs.one()) != null) {
            counter++;
            final String wfId = row.getString("ID");
            execService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        resume(wfId, internalStorageAccessor);
                    }
                    catch (Exception e) {
                        logger.error("resume failed", e);
                    }
                }
            });
        }
        logger.info("Read {} IDs in {} msec", counter, System.currentTimeMillis() - startTS);
        execService.shutdown();
        final boolean timeoutHappened = !execService.awaitTermination(initializationTimeoutSeconds, TimeUnit.SECONDS);
        if (timeoutHappened) {
            throw new CopperRuntimeException("initialize timed out!");
        }
        logger.info("Finished initialization - read {} rows in {} msec", counter, System.currentTimeMillis() - startTS);
        runtimeStatisticsCollector.submit("storage.init", counter, System.currentTimeMillis() - startTS, TimeUnit.MILLISECONDS);
    }

    private void resume(final String wfId, final HybridDBStorageAccessor internalStorageAccessor) throws Exception {
        logger.trace("resume(wfId={})", wfId);

        final ResultSet rs = session.execute(preparedStatements.get(CQL_SEL_WORKFLOW_INSTANCE).bind(wfId));
        final Row row = rs.one();
        if (row == null) {
            logger.warn("No workflow instance {} found - deleting row in COP_WFI_ID", wfId);
            session.executeAsync(preparedStatements.get(CQL_DEL_WFI_ID).bind(wfId));
            return;
        }

        final String ppoolId = row.getString("PPOOL_ID");
        final int prio = row.getInt("PRIO");
        final WaitMode waitMode = toWaitMode(row.getString("WAIT_MODE"));
        final Map<String, String> responseMap = toResponseMap(row.getString("RESPONSE_MAP_JSON"));
        final ProcessingState state = ProcessingState.valueOf(row.getString("STATE"));
        final Date timeout = row.getDate("TIMEOUT");
        final boolean timeoutOccured = timeout != null && timeout.getTime() <= System.currentTimeMillis();

        if (state == ProcessingState.ERROR || state == ProcessingState.INVALID) {
            return;
        }

        if (state == ProcessingState.ENQUEUED) {
            internalStorageAccessor.enqueue(wfId, ppoolId, prio);
            return;
        }

        if (responseMap != null) {
            final List<String> missingResponseCorrelationIds = new ArrayList<String>();
            int numberOfAvailableResponses = 0;
            for (Entry<String, String> e : responseMap.entrySet()) {
                final String correlationId = e.getKey();
                final String response = e.getValue();
                internalStorageAccessor.registerCorrelationId(correlationId, wfId);
                if (response != null) {
                    numberOfAvailableResponses++;
                }
                else {
                    missingResponseCorrelationIds.add(correlationId);
                }
            }
            boolean modified = false;
            if (!missingResponseCorrelationIds.isEmpty()) {
                // check for early responses
                for (String cid : missingResponseCorrelationIds) {
                    String earlyResponse = readEarlyResponse(cid);
                    if (earlyResponse != null) {
                        responseMap.put(cid, earlyResponse);
                        numberOfAvailableResponses++;
                        modified = true;
                    }
                }
            }
            if (modified || timeoutOccured) {
                final ProcessingState newState = (timeoutOccured || numberOfAvailableResponses == responseMap.size() || (numberOfAvailableResponses == 1 && waitMode == WaitMode.FIRST)) ? ProcessingState.ENQUEUED : ProcessingState.WAITING;
                final String responseMapJson = jsonMapper.toJSON(responseMap);
                session.execute(preparedStatements.get(CQL_UPD_WORKFLOW_INSTANCE_STATE_AND_RESPONSE_MAP).bind(newState.name(), responseMapJson, wfId));
                if (newState == ProcessingState.ENQUEUED) {
                    internalStorageAccessor.enqueue(wfId, ppoolId, prio);
                }
            }

        }
    }

    @Override
    public ListenableFuture<Void> updateWorkflowInstanceState(final String wfId, final ProcessingState state) throws Exception {
        logger.debug("updateWorkflowInstanceState({}, {})", wfId, state);
        final long startTS = System.nanoTime();
        final ResultSetFuture rsf = session.executeAsync(preparedStatements.get(CQL_UPD_WORKFLOW_INSTANCE_STATE).bind(state.name(), wfId));
        return createSettableFuture(rsf, "wfi.update.state", startTS);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> toResponseMap(String v) {
        return v == null ? null : jsonMapper.fromJSON(v, HashMap.class);
    }

    private WaitMode toWaitMode(String v) {
        return v == null ? null : WaitMode.valueOf(v);
    }

    private void prepare(String cql) {
        prepare(cql, alwaysRetry);
    }

    private void prepare(String cql, RetryPolicy petryPolicy) {
        logger.info("Preparing cql stmt {}", cql);
        PreparedStatement pstmt = session.prepare(cql);
        pstmt.setConsistencyLevel(consistencyLevel);
        pstmt.setRetryPolicy(petryPolicy);
        preparedStatements.put(cql, pstmt);
    }

}
