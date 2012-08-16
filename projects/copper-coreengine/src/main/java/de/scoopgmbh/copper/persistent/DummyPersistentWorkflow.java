package de.scoopgmbh.copper.persistent;

import java.io.Serializable;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.instrument.Transformed;

@Transformed
class DummyPersistentWorkflow extends PersistentWorkflow<Serializable> {

	private static final long serialVersionUID = 7047352707643389609L;
	
	public DummyPersistentWorkflow(String id, String ppoolId, String rowid, int prio) {
		if (id == null) throw new NullPointerException();
		if (ppoolId == null) throw new NullPointerException();
		if (rowid == null) throw new NullPointerException();
		setId(id);
		setProcessorPoolId(ppoolId);
		setPriority(prio);
		this.oldPrio = prio;
		this.oldProcessorPoolId = ppoolId;
		this.rowid = rowid;
	}

	@Override
	public void main() throws InterruptException {
	}

}
