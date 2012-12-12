package de.scoopgmbh.copper.monitor.server;

import java.rmi.registry.Registry;



public class ServerMain {
	
	
	public static void main(String[] args) {

		    
		new RMIForwardCopperDataProvider(Registry.REGISTRY_PORT);
	   
	}

}
