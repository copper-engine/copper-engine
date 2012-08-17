/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.persistent.txn;

/**
 * COPPER supports custom Transaction Management by using a built-in or custom Transaction Controller.
 * COPPER comes with a simple build in Transaction Mgmt (see {@link CopperTransactionController} and
 * a transaction controller that uses Springs Transaction Managament (see {@link SpringTransactionController}).
 * 
 * @author austermann
 *
 */
public interface TransactionController {
	
	/**
	 * Runs a database transaction, i.e. a database connection is aquired and the provided DatabaseTransaction object is executed 
	 * in the scope of a transaction.
	 */
	public <T> T run(final DatabaseTransaction<T> txn) throws Exception;
	
	/**
	 * Runs a transaction, the provided DatabaseTransaction object is executed in the scope of a transaction.
	 * A database, JMS, or any other connection may be aquired later on in this transaction, but to do so is in the scope of the 
	 * Transaction object. The TransactionController is just defining the start and end of the transaction. 
	 */
	public <T> T run(final Transaction<T> txn) throws Exception;
	
}
