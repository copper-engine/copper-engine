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
package org.copperengine.spring;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

public abstract class SpringTransaction {

    protected abstract void execute(Connection con) throws Exception;

    public void run(PlatformTransactionManager transactionManager, DataSource dataSource, TransactionDefinition def) throws Exception {
        TransactionStatus txnStatus = transactionManager.getTransaction(def);
        try {
            Connection con = DataSourceUtils.getConnection(dataSource);
            try {
                execute(con);
            } finally {
                DataSourceUtils.releaseConnection(con, dataSource);
            }
        } catch (Exception e) {
            transactionManager.rollback(txnStatus);
            throw e;
        }
        transactionManager.commit(txnStatus);
    }
}