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
package org.copperengine.core.audit;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.copperengine.core.batcher.AbstractBatchCommand;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.batcher.BatchExecutor;
import org.copperengine.core.batcher.CommandCallback;
import org.copperengine.core.db.utility.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchInsertIntoAutoTrail {

    public static final class Command extends AbstractBatchCommand<Executor, Command> {

        final AuditTrailEvent data;
        final boolean isOracle;
        final String sqlStmt;
        final List<Method> propertyGetters;

        public Command(AuditTrailEvent data, boolean isOracle, String sqlStmt, List<Method> propertyGetters, CommandCallback<Command> callback, int timeout) {
            super(callback, System.currentTimeMillis() + timeout);
            if (data == null)
                throw new NullPointerException();
            if (sqlStmt == null)
                throw new NullPointerException();
            if (propertyGetters == null)
                throw new NullPointerException();
            this.data = data;
            this.isOracle = isOracle;
            this.sqlStmt = sqlStmt;
            this.propertyGetters = propertyGetters;
        }

        @Override
        public Executor executor() {
            return Executor.INSTANCE;
        }

    }

    public static final class Executor extends BatchExecutor<Executor, Command> {

        private static final Executor INSTANCE = new Executor();
        private static final Logger logger = LoggerFactory.getLogger(Executor.class);

        @Override
        public int maximumBatchSize() {
            return 50;
        }

        @Override
        public int preferredBatchSize() {
            return 20;
        }

        @SuppressWarnings("resource")
        @Override
        public void doExec(final Collection<BatchCommand<Executor, Command>> commands, final Connection con) throws Exception {
            if (commands.isEmpty())
                return;

            final Command firstCommand = (Command) commands.iterator().next();
            final boolean isOracle = firstCommand.isOracle;
            final String sqlStmt = firstCommand.sqlStmt;
            final List<Method> propertyGetters = firstCommand.propertyGetters;

            PreparedStatement preparedStmt = null;
            try {

                preparedStmt = con.prepareStatement(sqlStmt);
                for (BatchCommand<Executor, Command> _cmd : commands) {
                    Command cmd = (Command) _cmd;
                    int idx = 1;
                    AuditTrailEvent data = cmd.data;
                    if (isOracle) {
                        if (data.getSequenceId() == null) {
                            preparedStmt.setNull(idx++, Types.NUMERIC);
                        } else {
                            preparedStmt.setLong(idx++, data.getSequenceId().longValue());
                        }
                    } else {
                        if (data.getSequenceId() != null) {
                            throw new UnsupportedOperationException("Custom SequenceId currently not supported for this DBMS");
                        }
                    }
                    for (Method m : propertyGetters) {
                        try {
                            Object value = null;
                            if (m.getDeclaringClass().isAssignableFrom(data.getClass())) {
                                value = m.invoke(data, (Object[]) null);
                            }
                            if (value != null) {
                                if (value instanceof Date) {
                                    value = new Timestamp(((Date) value).getTime());
                                }
                                preparedStmt.setObject(idx++, value, guessJdbcType(m));
                            } else {
                                preparedStmt.setNull(idx++, guessJdbcType(m));
                            }
                        } catch (SQLException e) {
                            logger.error("Setting property " + m + " failed", e);
                            throw e;
                        }
                    }
                    preparedStmt.addBatch();
                }
                preparedStmt.executeBatch();
            } catch (SQLException e) {
                logger.error(firstCommand.sqlStmt + " failed", e);
                throw e;
            } finally {
                JdbcUtils.closeStatement(preparedStmt);
            }
        }

    }

    static int guessJdbcType(Method m) {
        Class<?> type = m.getReturnType();
        if (type == String.class)
            return Types.VARCHAR;
        if (type == Integer.class || type == Integer.TYPE)
            return Types.INTEGER;
        if (type == Long.class || type == Long.TYPE)
            return Types.NUMERIC;
        if (type == Float.class || type == Float.TYPE)
            return Types.NUMERIC;
        if (type == Double.class || type == Double.TYPE)
            return Types.NUMERIC;
        if (type == Timestamp.class || type == Date.class || type == java.sql.Date.class)
            return Types.TIMESTAMP;
        throw new UnsupportedOperationException("no mapping for type " + type);
    }

}
