/*
 * Copyright 2002-2013 SCOOP Software GmbH
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

import java.util.Arrays;

public class DefaultPersisterSimpleCRUDSharedRessources<E, P extends DefaultEntityPersister<E>> extends
        DefaultPersisterSharedRessources<E, P> {

    final DefaultPersistenceWorker<E, P> selectWorker;
    final DefaultPersistenceWorker<E, P> insertWorker;
    final DefaultPersistenceWorker<E, P> updateWorker;
    final DefaultPersistenceWorker<E, P> deleteWorker;

    public DefaultPersisterSimpleCRUDSharedRessources(DefaultPersistenceWorker<E, P> selectWorker,
            DefaultPersistenceWorker<E, P> insertWorker,
            DefaultPersistenceWorker<E, P> updateWorker,
            DefaultPersistenceWorker<E, P> deleteWorker) {
        this.selectWorker = selectWorker;
        this.insertWorker = insertWorker;
        this.updateWorker = updateWorker;
        this.deleteWorker = deleteWorker;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<DefaultPersistenceWorker<E, P>> getWorkers() {
        return Arrays.<DefaultPersistenceWorker<E, P>> asList(new DefaultPersistenceWorker[] {
                selectWorker, insertWorker, updateWorker, deleteWorker
        });
    }

}
