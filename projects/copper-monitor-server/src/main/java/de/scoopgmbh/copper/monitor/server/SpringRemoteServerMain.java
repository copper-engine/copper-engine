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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;




public class SpringRemoteServerMain {
	
	public void start(){
		ApplicationContext context = new ClassPathXmlApplicationContext(
	            "/de/scoopgmbh/copper/monitor/server/Context.xml");
//		HttpInvokerServiceExporter rmiServiceExporter = new HttpInvokerServiceExporter();
//		rmiServiceExporter.setService(new ServerLoginImpl());
//		rmiServiceExporter.setServiceInterface(ServerLogin.class);
//		rmiServiceExporter.
//		rmiServiceExporter.prepare();
	}
	
	public static void main(String[] args) throws InterruptedException {
		SpringRemoteServerMain springRemoteServerMain = new SpringRemoteServerMain();
		springRemoteServerMain.start();
		Thread.sleep(Long.MAX_VALUE);
	}

}
