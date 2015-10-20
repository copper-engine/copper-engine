package org.copperengine.core.persistent.hybrid;

import org.copperengine.core.persistent.txn.DatabaseTransaction;
import org.copperengine.core.persistent.txn.Transaction;
import org.copperengine.core.persistent.txn.TransactionController;

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
