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
package org.copperengine.core.persistent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collection;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.Response;
import org.copperengine.core.batcher.AbstractBatchCommand;
import org.copperengine.core.batcher.AcknowledgeCallbackWrapper;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.batcher.BatchExecutor;

class OracleNotify {

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
            final Timestamp now = new Timestamp(System.currentTimeMillis());
            try (final PreparedStatement stmt = con.prepareStatement("INSERT INTO COP_RESPONSE (CORRELATION_ID, RESPONSE_TS, RESPONSE, LONG_RESPONSE, RESPONSE_META_DATA, RESPONSE_TIMEOUT, RESPONSE_ID) VALUES (?,?,?,?,?,?,?)")) {
                for (BatchCommand<Executor, Command> _cmd : commands) {
                    Command cmd = (Command) _cmd;
                    stmt.setString(1, cmd.response.getCorrelationId());
                    stmt.setTimestamp(2, now);
                    String payload = cmd.serializer.serializeResponse(cmd.response);
                    stmt.setString(3, payload.length() > 4000 ? null : payload);
                    stmt.setString(4, payload.length() > 4000 ? payload : null);
                    stmt.setString(5, cmd.response.getMetaData());
                    stmt.setTimestamp(6, TimeoutProcessor.processTimout(cmd.response.getInternalProcessingTimeout(), cmd.defaultStaleResponseRemovalTimeout));
                    stmt.setString(7, cmd.response.getResponseId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }

    }

}
