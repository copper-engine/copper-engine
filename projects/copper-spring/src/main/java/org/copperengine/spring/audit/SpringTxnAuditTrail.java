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
package org.copperengine.spring.audit;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;

import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.audit.AbstractAuditTrail;
import org.copperengine.core.audit.AuditTrailEvent;
import org.copperengine.core.audit.BatchInsertIntoAutoTrail.Command;
import org.copperengine.core.audit.BatchInsertIntoAutoTrail.Executor;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.batcher.NullCallback;
import org.copperengine.spring.SpringTransaction;
import org.slf4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SpringTxnAuditTrail extends AbstractAuditTrail {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SpringTxnAuditTrail.class);

    private PlatformTransactionManager transactionManager;

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void synchLog(final AuditTrailEvent e) {
        if (isEnabled(e.getLogLevel())) {
            logger.debug("doLog({})", e);
            e.setMessage(messagePostProcessor.serialize(e.getMessage()));
            try {
                new SpringTransaction() {
                    @Override
                    protected void execute(Connection con) throws Exception {
                        @SuppressWarnings("unchecked")
                        BatchCommand<Executor, Command> cmd = createBatchCommand(e, true, NullCallback.instance);
                        Collection<BatchCommand<Executor, Command>> cmdList = Arrays.<BatchCommand<Executor, Command>>asList(cmd);
                        cmd.executor().doExec(cmdList, con);
                    }
                }.run(transactionManager, getDataSource(), createTransactionDefinition());
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new CopperRuntimeException(ex);
            }
        }
    }

    protected TransactionDefinition createTransactionDefinition() {
        return new DefaultTransactionDefinition();
    }

}
