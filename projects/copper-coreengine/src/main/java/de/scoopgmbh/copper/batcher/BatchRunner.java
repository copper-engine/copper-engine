package de.scoopgmbh.copper.batcher;

import java.util.Collection;

public interface BatchRunner<E extends BatchExecutorBase<E,T>, T extends BatchCommand<E,T>> {
	
	public void run(final Collection<BatchCommand<E,T>> commands, BatchExecutorBase<E,T> base);

}
