package de.scoopgmbh.copper.monitor.adapter;

import java.rmi.Remote;

public interface SessionFactory extends Remote {
	public CopperDataProvider createSession(String user, String credential);
}
