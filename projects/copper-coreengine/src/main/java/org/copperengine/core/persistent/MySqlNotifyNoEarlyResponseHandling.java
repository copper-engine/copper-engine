/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.core.persistent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.Response;
import org.copperengine.core.batcher.AbstractBatchCommand;
import org.copperengine.core.batcher.AcknowledgeCallbackWrapper;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.batcher.BatchExecutor;
import org.copperengine.core.db.utility.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MySqlNotifyNoEarlyResponseHandling {

    private static final Logger logger = LoggerFactory.getLogger(MySqlNotifyNoEarlyResponseHandling.class);

    static final String SQL =
            "INSERT INTO cop_response (CORRELATION_ID, RESPONSE_TS, RESPONSE, RESPONSE_TIMEOUT, RESPONSE_META_DATA, RESPONSE_ID) " +
                    "SELECT D.* FROM " +
                    "(select correlation_id from cop_wait where correlation_id = ?) W, " +
                    "(select ? as correlation_id, ? as response_ts, ? as response, ? as response_timeout, ? as response_meta_data, ? as RESPONSE_ID) D " +
                    "WHERE D.correlation_id = W.correlation_id";


    static final class Command extends AbstractBatchCommand<Executor, Command> {

        final Response<?> response;
        final Serializer serializer;
        final long defaultStaleResponseRemovalTimeout;

        public Command(Response<?> response, Serializer serializer, long defaultStaleResponseRemovalTimeout, final long targetTime, Acknowledge ack) {
            super(new AcknowledgeCallbackWrapper<Command>(ack), targetTime);
            this.response = response;
            this.serializer = serializer;
            this.defaultStaleResponseRemovalTimeout = defaultStaleResponseRemovalTimeout;
        }

        @Override
        public Executor executor() {
            return Executor.INSTANCE;
        }
    }


    static final class Executor extends BatchExecutor<Executor, Command> {

        private static final Executor INSTANCE = new Executor();

        @Override
        public int maximumBatchSize() {
            return 100;
        }

        @Override
        public int preferredBatchSize() {
            return 50;
        }

        @Override
        public void doExec(final Collection<BatchCommand<Executor, Command>> commands, final Connection con) throws Exception {
            if (commands.isEmpty())
                return;
            final PreparedStatement stmt = con.prepareStatement(SQL);
            try {
                final Timestamp now = new Timestamp(System.currentTimeMillis());
                for (BatchCommand<Executor, Command> _cmd : commands) {
                    Command cmd = (Command) _cmd;
                    stmt.setString(1, cmd.response.getCorrelationId());
                    stmt.setString(2, cmd.response.getCorrelationId());
                    stmt.setTimestamp(3, now);
                    stmt.setString(4, cmd.serializer.serializeResponse(cmd.response));
                    stmt.setTimestamp(5, TimeoutProcessor.processTimout(cmd.response.getInternalProcessingTimeout(), cmd.defaultStaleResponseRemovalTimeout));
                    stmt.setString(6, cmd.response.getMetaData());
                    stmt.setString(7, cmd.response.getResponseId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } catch (SQLException e) {
                logger.error("doExec failed", e);
                logger.error("NextException=", e.getNextException());
                throw e;
            } catch (Exception e) {
                logger.error("doExec failed", e);
                throw e;
            } finally {
                JdbcUtils.closeStatement(stmt);
            }
        }
    }

}
