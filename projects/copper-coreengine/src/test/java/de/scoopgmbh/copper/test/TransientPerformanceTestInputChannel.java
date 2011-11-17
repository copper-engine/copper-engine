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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowFactory;

public class TransientPerformanceTestInputChannel implements Runnable {

	public static final int NUMB = 500000;
	public static final AtomicInteger counter = new AtomicInteger(0);

	public static long min=Long.MAX_VALUE;
	public static long max=Long.MIN_VALUE;
	public static long sum=0;
	public static long statCounter=0;
	public static final Object mutex=new Object();	

	public static void report() {
		System.out.println("counter="+counter);
		System.out.println("min="+min);
		System.out.println("max="+max);
		System.out.println("avg="+(sum/statCounter));
	}	

	public static void addMP(long et) {
		synchronized (mutex) {
			sum += et;
			statCounter++;
			if (et < min) min=et;
			if (et > max) max=et;
		}
	}

	public static void increment() {
		if (counter.incrementAndGet() == NUMB) {
			synchronized (counter) {
				counter.notify();
			}
		}
	}

	public static void wait4finish() {
		synchronized (counter) {
			try {
				counter.wait();
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private ProcessingEngine engine;

	public void setEngine(ProcessingEngine engine) {
		this.engine = engine;
	}


	@Override
	public void run() {
		try {
			Logger.getRootLogger().setLevel(Level.INFO);

			WorkflowFactory<String> wfFactory = engine.createWorkflowFactory("de.scoopgmbh.copper.test.PerformanceTestWF");
			for (int x=0; x<100; x++) {

				long startTS = System.currentTimeMillis();
				counter.set(0);
				for (int i=0; i<NUMB; i++) {
					Workflow<?> wf = wfFactory.newInstance();
					engine.run(wf);
				}
				wait4finish();

				long diff = System.currentTimeMillis() - startTS;
				System.out.println("Elapsed time is "+diff+" msec.");

				long reqPerSec = NUMB*1000L / diff;
				System.out.println("throughput = "+reqPerSec);
				
				Thread.sleep(5000);
			}

			engine.shutdown();

			report();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void startup() {
		new Thread(this).start();
	}

	public void shutdown() {

	}
}
