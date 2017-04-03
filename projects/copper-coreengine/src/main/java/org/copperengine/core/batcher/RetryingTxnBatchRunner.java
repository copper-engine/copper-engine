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
package org.copperengine.core.batcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.copperengine.core.db.utility.RetryingTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryingTxnBatchRunner<E extends BatchExecutorBase<E, T>, T extends BatchCommand<E, T>> implements BatchRunner<E, T> {

    private static final Logger logger = LoggerFactory.getLogger(RetryingTxnBatchRunner.class);

    private DataSource dataSource;

    public RetryingTxnBatchRunner() {
    }

    /**
     * @param dataSource on which the RetryingTxnBatchRunner operates on.
     * @since 3.1
     * */
    public RetryingTxnBatchRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void run(final Collection<BatchCommand<E, T>> commands, final BatchExecutorBase<E, T> base) {
        if (commands.isEmpty())
            return;

        try {
            if (dataSource == null) {
                base.doExec(commands, null);
            } else {
                new RetryingTransaction<Void>(dataSource) {
                    @Override
                    protected Void execute() throws Exception {
                        base.doExec(commands, getConnection());
                        return null;
                    }
                }.run();
            }
            for (BatchCommand<?, ?> cmd : commands) {
                cmd.callback().commandCompleted();
            }
        } catch (Exception e) {
            if (commands.size() == 1) {
                BatchCommand<?, ?> cmd = commands.iterator().next();
                cmd.callback().unhandledException(e);
            } else {
                logger.warn("batch execution failed - trying execution of separate commands ", e);
                for (BatchCommand<E, T> cmd : commands) {
                    List<BatchCommand<E, T>> l = new ArrayList<BatchCommand<E, T>>();
                    l.add(cmd);
                    run(l, base);
                }
            }
        }
    }

}
