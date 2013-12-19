/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package org.copperengine.core.audit;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.copperengine.core.audit.AuditTrail;
import org.copperengine.core.audit.AuditTrailCallback;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class TestBatchingAuditTrail {
	
	

	public static void main(String[] args) throws Exception {
		final ApplicationContext ctx = new FileSystemXmlApplicationContext("src/test/resources/persistent-engine-application-context.xml");
		AuditTrail auditTrail = ctx.getBean(AuditTrail.class);
		for (int i=0; i<100; i++) {
			auditTrail.asynchLog(2, new Date(), "conv12345678901234567890123456789012", "ctx", "proc12345678901234567890123456789012", "corr12345678901234567890123456789012", null, "TEXT", "testMessage");
		}
		Thread.sleep(2000);
		
		test(createTestMessage(100), auditTrail, 20000);
		test(createTestMessage(100), auditTrail, 20000);
		test(createTestMessage(500), auditTrail, 10000);
		test(createTestMessage(1000), auditTrail, 5000);
		test(createTestMessage(4000), auditTrail, 5000);
		test(createTestMessage(8000), auditTrail, 2000);
		test(createTestMessage(16000), auditTrail, 1000);

		System.exit(0);
	}

	private static String createTestMessage(int size) {
		final StringBuilder sb = new StringBuilder(4000);
		for (int i=0; i<(size/10); i++) {
			sb.append("0123456789");
		}
		final String msg = sb.toString();
		return msg;
	}

	private static void test(final String msg, AuditTrail auditTrail, final int max) throws InterruptedException {
		;
		final AtomicInteger x= new AtomicInteger();
		AuditTrailCallback cb = new AuditTrailCallback() {
			@Override
			public void error(Exception e) {
				x.incrementAndGet();
			}
			
			@Override
			public void done() {
				x.incrementAndGet();
			}
		};
		long startTS=System.currentTimeMillis();
		for (int i=0; i<max; i++) {
			auditTrail.asynchLog(2, new Date(), "conv12345678901234567890123456789012", "ctx", "proc12345678901234567890123456789012", "corr12345678901234567890123456789012", null, msg, "TEXT", cb);
		}
		while (x.get() != max) {
			Thread.sleep(5);
		}
		long et = System.currentTimeMillis()-startTS;
		System.out.println("et="+et+" mesc for "+max+" records, recordSize="+msg.length());
	}
}
