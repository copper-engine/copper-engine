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


import java.util.Date;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.batcher.Batcher;
import org.copperengine.core.batcher.CommandCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fast db based audit trail implementation. It is possible to extend the COPPER audit trail with custom attributes.
 * See JUnitTest {@code BatchingAuditTrailTest.testCustomTable()} for an example.
 *
 * @author austermann
 */
public class BatchingAuditTrail extends AbstractAuditTrail {

    private static final Logger logger = LoggerFactory.getLogger(BatchingAuditTrail.class);
    private Batcher batcher;

    /**
     * returns immediately after queueing the log message
     *
     * @param logLevel
     *            the level on that the audit trail event is recorded (might be used for filtering)
     * @param occurrence
     *            timestamp of the audit trail event
     * @param conversationId
     *            conversation id embraces all audit trail events for one business process (might be the same for a
     *            whole business transaction over a range of involved systems)
     * @param context
     *            the context of the audit trail event (e.g. a camel route, a workflow task, ...)
     * @param instanceId
     *            workflow id for a single workflow
     * @param correlationId
     *            correlates a request response pair (e.g. workflow calls another workflow, workflow calls a camel
     *            route, ...)
     * @param transactionId
     *            Same ID vor several conversations, that belongs to the same transaction. Example: ExecuteOrder
     *            (conversation 1), ChangeOrder (conversation 2) and CancelOrder (conversation 3) that all belongs to
     *            transaction 77. When transaction 77 can be deleted, all conversations for this transaction can be
     *            deleted.
     * @param _message
     *            a message describing the audit trail event
     * @param messageType
     *            type of the message, e.g. XML, used for message rendering in the COPPER monitor
     */
    public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String _message, String messageType) {
        this.asynchLog(new AuditTrailEvent(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, _message, messageType, null));
    }

    /**
     * returns immediately after queueing the log message
     *
     * @param logLevel
     *            the level on that the audit trail event is recorded (might be used for filtering)
     * @param occurrence
     *            timestamp of the audit trail event
     * @param conversationId
     *            conversation id embraces all audit trail events for one business process (might be the same for a
     *            whole business transaction over a range of involved systems)
     * @param context
     *            the context of the audit trail event (e.g. a camel route, a workflow task, ...)
     * @param instanceId
     *            workflow id for a single workflow
     * @param correlationId
     *            correlates a request response pair (e.g. workflow calls another workflow, workflow calls a camel
     *            route, ...)
     * @param transactionId
     *            Same ID vor several conversations, that belongs to the same transaction. Example: ExecuteOrder
     *            (conversation 1), ChangeOrder (conversation 2) and CancelOrder (conversation 3) that all belongs to
     *            transaction 77. When transaction 77 can be deleted, all conversations for this transaction can be
     *            deleted.
     * @param _message
     *            a message describing the audit trail event
     * @param messageType
     *            type of the message, e.g. XML, used for message rendering in the COPPER monitor
     * @param cb
     *            callback called when logging succeeded or failed.
     */
    public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String _message, String messageType, final AuditTrailCallback cb) {
        this.asynchLog(new AuditTrailEvent(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, _message, messageType, null), cb);
    }

    /**
     * returns immediately after queueing the log message
     * @param e
     *            the AuditTrailEvent to be logged
     */
    public void asynchLog(AuditTrailEvent e) {
        doLog(e, new Acknowledge.BestEffortAcknowledge(), false);
    }


    /**
     * returns immediately after queueing the log message
     * @param e
     *            the AuditTrailEvent to be logged
     * @param cb
     *            callback called when logging succeeded or failed.
     */
    public void asynchLog(final AuditTrailEvent e, final AuditTrailCallback cb) {
        CommandCallback<BatchInsertIntoAutoTrail.Command> callback = new CommandCallback<BatchInsertIntoAutoTrail.Command>() {
            @Override
            public void commandCompleted() {
                cb.done();
            }

            @Override
            public void unhandledException(Exception e) {
                cb.error(e);
            }
        };
        doLog(e, false, callback);
    }

    protected boolean doLog(AuditTrailEvent e, final Acknowledge ack, boolean immediate) {
        CommandCallback<BatchInsertIntoAutoTrail.Command> callback = new CommandCallback<BatchInsertIntoAutoTrail.Command>() {
            @Override
            public void commandCompleted() {
                ack.onSuccess();
            }

            @Override
            public void unhandledException(Exception e) {
                ack.onException(e);
            }
        };
        return doLog(e, immediate, callback);
    }

    protected boolean doLog(AuditTrailEvent e, boolean immediate, CommandCallback<BatchInsertIntoAutoTrail.Command> callback) {
        if (isEnabled(e.logLevel)) {
            logger.debug("doLog({})", e);
            e.setMessage(messagePostProcessor.serialize(e.message));
            batcher.submitBatchCommand(createBatchCommand(e, immediate, callback));
            return true;
        }
        return false;
    }

    @Override
    public void synchLog(final AuditTrailEvent event) {
        Acknowledge.DefaultAcknowledge ack = new Acknowledge.DefaultAcknowledge();
        if (doLog(event, ack, true)) {
            ack.waitForAcknowledge();
        }
    }


    public void setBatcher(Batcher batcher) {
        this.batcher = batcher;
    }
}
