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
package de.scoopgmbh.copper.test;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowFactory;

public class PersistentTestInputChannel implements Runnable {

	private static final Logger logger = Logger.getLogger(PersistentTestInputChannel.class);
	private ProcessingEngine engine;

	public void setEngine(ProcessingEngine engine) {
		this.engine = engine;
	}

	public void run() {
		try {
			final int SIZE = 20*10;
			StringBuilder dataSB = new StringBuilder(SIZE);
			for (int i=0; i<SIZE; i++) {
				int pos = (int)(Math.random()*70.0);
				dataSB.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890!§$%&/()=?".substring(pos,pos+1));
			}
			final String data = dataSB.toString(); 
			
			WorkflowFactory<String> wfFactory = engine.createWorkflowFactory("de.scoopgmbh.copper.test.PersistentSpock2GTestWF");

			for (int i=0; i<200; i++) {
				Workflow<String> wf = wfFactory.newInstance();
				wf.setData(data);
				engine.run(wf);
			}
//			Thread.sleep(15000);
//			engine.shutdown();

		}
		catch(Exception e) {
			logger.error("run failed",e);
		}
	}

	public void startup() {
		new Thread(this).start();
	}
	
	public void shutdown() {
		
	}
}
