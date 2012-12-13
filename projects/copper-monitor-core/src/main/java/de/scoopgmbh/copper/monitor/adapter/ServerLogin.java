package de.scoopgmbh.copper.monitor.adapter;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerLogin extends Remote {
	public CopperDataProvider login(String user, String credential) throws RemoteException;
}
