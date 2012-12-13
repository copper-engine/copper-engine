package de.scoopgmbh.copper.monitor.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

import de.scoopgmbh.copper.monitor.adapter.ServerLogin;



public class ServerMain {
	
	
	private static ServerLogin serverLogin;
	private static ServerLoginImpl serverLoginImpl;
	//Keep a strong reference to the object that implements the java.rmi.Remote interface so that it remains reachable,
	//i.e. ineligible for garbage collection. http://stackoverflow.com/questions/645208/java-rmi-nosuchobjectexception-no-such-object-in-table

	public static void main(String[] args) throws InterruptedException {

		    
		
		try {
//			if (System.getSecurityManager() == null) {
//	            System.setSecurityManager(new SecurityManager());
//	        }
			
			
			System.setProperty("java.rmi.server.randomIDs", "true");
			//http://www.drdobbs.com/jvm/building-secure-java-rmi-servers/184405197
			//Up until now, I have assumed that if a client doesn't go through the login object, it can't access any of the proxy objects.
			//An interesting thing happens when you set the java.rmi.server.randomIDs property to True.
			//Object Id stops being a counter and becomes a securely generated 64-bit random number. 
			//So if someone wants to access an object by using the wire protocol, it must find the correct UID of the object and get the 64-bit number right. 
			//This constitutes a sparse space large enough for preventing most attacks. 
			
			LocateRegistry.createRegistry( Registry.REGISTRY_PORT );
			serverLoginImpl = new ServerLoginImpl();
			serverLogin = (ServerLogin) UnicastRemoteObject.exportObject(serverLoginImpl, 0);
			RemoteServer.setLog(System.out);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(ServerLogin.class.getSimpleName(), serverLogin);
			
//			while(true){
//				Thread.currentThread().wait();
//				//System.out.println("wait");
//			}
			
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
//	   System.out.println("wunderlich");
	}

}
