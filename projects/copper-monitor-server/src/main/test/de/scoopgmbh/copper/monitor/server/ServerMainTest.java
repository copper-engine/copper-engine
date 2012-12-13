package de.scoopgmbh.copper.monitor.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.Test;

import de.scoopgmbh.copper.monitor.adapter.ServerLogin;


public class ServerMainTest {

	@Test
	public void test_connect() throws Exception{
		new Thread(){
			public void run() {
				try {
					ServerMain.main(null);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			};
		}.start();
		Thread.sleep(1000);
		
		Registry registry = LocateRegistry.getRegistry("localhost",Registry.REGISTRY_PORT);
		ServerLogin serverLogin = (ServerLogin) registry.lookup(ServerLogin.class.getSimpleName());
		serverLogin.login("", "");

	}
}
