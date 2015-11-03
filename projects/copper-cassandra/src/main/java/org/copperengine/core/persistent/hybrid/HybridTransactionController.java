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
package org.copperengine.core.persistent.hybrid;

import org.copperengine.core.persistent.txn.DatabaseTransaction;
import org.copperengine.core.persistent.txn.Transaction;
import org.copperengine.core.persistent.txn.TransactionController;

/**
 * empty implementation of the {@link TransactionController} interface, as the HybridDBStorage does NOT support
 * transactions.
 * 
 * @author austermann
 *
 */
public class HybridTransactionController implements TransactionController {

    @Override
    public <T> T run(DatabaseTransaction<T> txn) throws Exception {
        return txn.run(null);
    }

    @Override
    public <T> T run(Transaction<T> txn) throws Exception {
        return txn.run();
    }

}
