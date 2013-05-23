package de.scoopgmbh.copper.persistent;

import java.util.Arrays;

public class DefaultPersisterSimpleCRUDSharedRessources<E,P extends DefaultEntityPersister<E>> extends
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
		return Arrays.<DefaultPersistenceWorker<E, P>>asList(new DefaultPersistenceWorker[]{
			selectWorker, insertWorker, updateWorker, deleteWorker
		});
	}

}
