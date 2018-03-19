package org.copperengine.core.persistent;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.copperengine.core.ProcessingState;
import org.copperengine.core.Workflow;
import org.copperengine.core.internal.WorkflowAccessor;
import org.copperengine.core.util.FunctionWithException;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonSQLHelper {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSqlDialect.class);

    static StringBuilder appendDates(StringBuilder sql, List<Object> params, WorkflowInstanceFilter filter) {
        if (filter.getCreationTS() != null) {
            if (filter.getCreationTS().getFrom() != null) {
                sql.append(" AND x.CREATION_TS >= ?");
                params.add(new Date(filter.getCreationTS().getFrom().getTime()));
            }
            if (filter.getCreationTS().getTo() != null) {
                sql.append(" AND x.CREATION_TS < ?");
                params.add(filter.getCreationTS().getTo());
            }
        }
        if (filter.getLastModTS() != null) {
            if (filter.getLastModTS().getFrom() != null) {
                sql.append(" AND x.LAST_MOD_TS >= ?");
                params.add(new Date(filter.getLastModTS().getFrom().getTime()));
            }
            if (filter.getLastModTS().getTo() != null) {
                sql.append(" AND x.LAST_MOD_TS < ?");
                params.add(new Date(filter.getLastModTS().getTo().getTime()));
            }
        }

        return sql;
    }

    static StringBuilder appendStates(StringBuilder sql, List<Object> params, WorkflowInstanceFilter filter) {
        if (filter.getStates() != null && !filter.getStates().isEmpty()) {
            List<String> filterStates = new ArrayList<>();
            if (filter.getStates().contains(ProcessingState.ENQUEUED.name())) {
                filterStates.add("" + DBProcessingState.ENQUEUED.ordinal());
            }
            if (filter.getStates().contains(ProcessingState.ERROR.name())) {
                filterStates.add("" + DBProcessingState.ERROR.ordinal());
            }
            if (filter.getStates().contains(ProcessingState.WAITING.name())) {
                filterStates.add("" + DBProcessingState.WAITING.ordinal());
            }
            if (filter.getStates().contains(ProcessingState.INVALID.name())) {
                filterStates.add("" + DBProcessingState.INVALID.ordinal());
            }
            if (filter.getStates().contains(ProcessingState.FINISHED.name())) {
                filterStates.add("" + DBProcessingState.FINISHED.ordinal());
            }


            if (!filterStates.isEmpty()) {
                sql.append(" AND x.STATE in (" + String.join(", ", Collections.nCopies(filterStates.size(), "?"))  + ")");
                params.addAll(filterStates);
            }
        }
        return sql;
    }

    public static List<Workflow<?>> processResult(String sql, List<Object> params, String sqlQueryErrorData, Connection con, FunctionWithException<ResultSet, PersistentWorkflow<?>> decode) throws SQLException {
        final List<Workflow<?>> result = new ArrayList<>();
        try (PreparedStatement pStmtQueryWFIs = con.prepareStatement(sql.toString()); PreparedStatement pStmtQueryErrorData = con.prepareStatement(sqlQueryErrorData)) {
            for (int i=1; i<=params.size(); i++) {
                pStmtQueryWFIs.setObject(i, params.get(i-1));
            }
            ResultSet rs = pStmtQueryWFIs.executeQuery();
            while (rs.next()) {
                try {
                    final PersistentWorkflow<?> wf = decode.apply(rs);
                    if (wf.getProcessingState() == ProcessingState.ERROR) {
                        pStmtQueryErrorData.setString(1, wf.getId());
                        try (ResultSet rsErrorData = pStmtQueryErrorData.executeQuery()) {
                            if (rsErrorData.next()) {
                                final org.copperengine.core.persistent.ErrorData errorData = new org.copperengine.core.persistent.ErrorData();
                                errorData.setExceptionStackTrace(rsErrorData.getString("EXCEPTION"));
                                errorData.setErrorTS(rsErrorData.getTimestamp("ERROR_TS"));
                                WorkflowAccessor.setErrorData(wf, errorData);
                            }
                        }
                    }
                    result.add(wf);
                } catch (Exception e) {
                    logger.error("decoding of '" + rs.getString("ID") + "' failed: " + e.toString(), e);
                }
            }
        }
        return result;
    }

    public static int processCountResult(StringBuilder sql, List<Object> params, Connection con) throws SQLException {
        try (PreparedStatement pStmtQueryWFIs = con.prepareStatement(sql.toString())) {
            for (int i=1; i<=params.size(); i++) {
                pStmtQueryWFIs.setObject(i, params.get(i-1));
            }
            ResultSet rs = pStmtQueryWFIs.executeQuery();
            while (rs.next()) {
                try {
                    int workflowCount = rs.getInt("WF_NUMBER");
                    logger.debug("Counted " + workflowCount + " workflows");
                    return workflowCount;
                } catch (Exception e) {
                    logger.error("decoding of '" + rs + "' failed: " + e.toString(), e);
                }
            }
        }

        throw new SQLException("Failed to get result of SQL request for counting workflow instances");
    }
}
