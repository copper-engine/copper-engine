package de.scoopgmbh.copper.persistent;

public interface SavepointAware {

	public void onSave(PersistenceContext pc);
	
}
