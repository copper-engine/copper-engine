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
package org.copperengine.core.persistent.txn;

/**
 * COPPER supports custom Transaction Management by using a built-in or custom Transaction Controller.
 * COPPER comes with a simple build in Transaction Mgmt (see {@link CopperTransactionController} and
 * a transaction controller that uses Springs Transaction Managament (see {@code SpringTransactionController} in project
 * copper-spring).
 *
 * @author austermann
 */
public interface TransactionController {

    /**
     * Runs a database transaction, i.e. a database connection is aquired and the provided DatabaseTransaction object is
     * executed in the scope of a transaction.
     * @param <T> return type which is passed back by the succeeded transaction. Might be null if transaction runs without giving a result.
     * @param txn a database transaction object which will be run by the transaction controller.
     * @return the result of the transaction if any.
     * @throws Exception if something goes wrong within the given transaction which is not caught by itself or if something
     *    goes wrong with the transaction management like getConnection throws an SQLException.
     */
    public <T> T run(final DatabaseTransaction<T> txn) throws Exception;

    /**
     * Runs a transaction, the provided DatabaseTransaction object is executed in the scope of a transaction.
     * A database, JMS, or any other connection may be aquired later on in this transaction, but to do so is in the
     * scope of the
     * Transaction object. The TransactionController is just defining the start and end of the transaction.
     * @param <T> return type which is passed back by the succeeded transaction. Might be null if transaction runs without giving a result.
     * @param txn a database transaction object which will be run by the transaction controller
     * @return the result of the transaction if any.
     * @throws Exception if something goes wrong within the given transaction which is not caught by itself or if something
     *    goes wrong with the transaction management like getConnection throws an SQLException.
     */
    public <T> T run(final Transaction<T> txn) throws Exception;

}
