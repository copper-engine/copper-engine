/*
 * Copyright 2002-2011 SCOOP Software GmbH
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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowFactory;
import de.scoopgmbh.copper.persistent.PersistentScottyEngine;

public class PersistentPerformanceTestInputChannel implements Runnable {
	
	private static final Logger logger = Logger.getLogger(PersistentPerformanceTestInputChannel.class);
	
	private PersistentScottyEngine engine;
	
	public void setEngine(PersistentScottyEngine engine) {
		this.engine = engine;
	}

	@Override
	public void run() {
		// generate some data to work on
		// the workflow process instance will obtain this data
		final int SIZE = 64;
		StringBuilder dataSB = new StringBuilder(SIZE);
		for (int i=0; i<SIZE; i++) {
			int pos = (int)(Math.random()*70.0);
			dataSB.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890!§$%&/()=?".substring(pos,pos+1));
		}
		final String data = dataSB.toString(); 
		System.out.println(data);
		
		try {
			// reduce logging data
			Logger.getRootLogger().setLevel(Level.INFO);

			// create the workflow 'PersistentSpock2GTestWF'
			WorkflowFactory<String> wfFactory = engine.createWorkflowFactory("de.scoopgmbh.copper.test.PersistentSpock2GTestWF");
			
			// warm up the engine by processing 2 workflow calls 
			logger.info("Warming up...");
			List<Workflow<?>> x = new ArrayList<Workflow<?>>(100);
			for (int i=0; i<2; i++) {
				Workflow<String> wf = wfFactory.newInstance();
				wf.setData(data);
				x.add(wf);
			}
			engine.run(x);
			Thread.sleep(5000);
			
			// load the engine with workflows
			final int max=20;
			final int batchSize=500;
			logger.info("Test starts now");
			final long startTS = System.currentTimeMillis();
			for (int k=0; k<max; k++) {
				List<Workflow<?>> list = new ArrayList<Workflow<?>>(100);
				for (int i=0; i<batchSize; i++) {
					Workflow<String> wf = wfFactory.newInstance();
					wf.setData(data);
					list.add(wf);
				}
				engine.run(list);
			}
			logger.info("all "+(max*batchSize)+" request(s) created");
			
			// wait for workflow responses
			Counter.doWait(max*batchSize-50);
			long et = System.currentTimeMillis() - startTS;
			logger.info("Done Waiting - et = "+(et/1000L));
			
			Thread.sleep(15000);			
			//logger.info(engine.getDbStorage().getStatistics());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void startup() {
		new Thread(this).start();
	}
	
	public void shutdown() {
		
	}
}
