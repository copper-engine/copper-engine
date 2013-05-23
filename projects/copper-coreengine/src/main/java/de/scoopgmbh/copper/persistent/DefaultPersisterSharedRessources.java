package de.scoopgmbh.copper.persistent;


public abstract class DefaultPersisterSharedRessources<E, P extends DefaultEntityPersister<E>> {
	
	public abstract Iterable<DefaultPersistenceWorker<E,P>> getWorkers();

}
