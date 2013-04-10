/*
 * Copyright 2002-2012 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.monitor.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.Test;

import de.scoopgmbh.copper.monitor.adapter.ServerLogin;


public class ServerMainTest {

	@Test
	public void test_connect() throws Exception{
		new Thread(){
			@Override
			public void run() {
				try {
					ServerMain.main(new String[]{});
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
