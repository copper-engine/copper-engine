package de.scoopgmbh.copper.batcher;

import de.scoopgmbh.copper.Acknowledge;

public class AcknowledgeCallbackWrapper<T extends BatchCommand<?,T>> implements CommandCallback<T> {

	final Acknowledge ack;
	
	public AcknowledgeCallbackWrapper(Acknowledge ack) {
		this.ack = ack;
	}

	@Override
	public void commandCompleted() {
		ack.onSuccess();
	}

	@Override
	public void unhandledException(Exception e) {
		ack.onException(e);
	}
	
}
