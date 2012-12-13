package de.scoopgmbh.copper.monitor.server;

import java.rmi.RemoteException;

import de.scoopgmbh.copper.monitor.adapter.CopperDataProvider;
import de.scoopgmbh.copper.monitor.adapter.ServerLogin;

public class ServerLoginImpl implements ServerLogin {

	@Override
	public CopperDataProvider login(String user, String credential) throws RemoteException {
		return new RMIForwardCopperDataProvider();
	}

}
