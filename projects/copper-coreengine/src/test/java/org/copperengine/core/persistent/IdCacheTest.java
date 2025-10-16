/**
 * Copyright 2002-2017 SCOOP Software GmbH
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
package org.copperengine.core.persistent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdCacheTest {

    @Test
    public void testIdCacheMaxSize() {
        IdCache idCache = new IdCache(3, 10, TimeUnit.SECONDS);
        idCache.put("r1", "c1");
        idCache.put("r2", "c2");
        idCache.put("r3", "c3");
        Assertions.assertTrue(idCache.contains("c1"));
        Assertions.assertTrue(idCache.contains("c2"));
        Assertions.assertTrue(idCache.contains("c3"));
        Assertions.assertTrue(idCache.contains("c1", "c2"));
        Assertions.assertTrue(idCache.contains("c1", "c2", "c3"));
        Assertions.assertFalse(idCache.contains("c4"));
        idCache.put("r4", "c4");
        Assertions.assertFalse(idCache.contains("c1"));
        Assertions.assertTrue(idCache.contains("c2"));
        Assertions.assertTrue(idCache.contains("c3"));
        Assertions.assertFalse(idCache.contains("c1", "c2"));
        Assertions.assertFalse(idCache.contains("c1", "c2", "c3"));
        Assertions.assertTrue(idCache.contains("c4"));
        Assertions.assertTrue(idCache.contains("c2", "c3", "c4"));
    }

    @Test
    public void testIdCacheTTL() throws Exception {
        IdCache idCache = new IdCache(3, 5, TimeUnit.MILLISECONDS);
        idCache.put("r1", "c1");
        idCache.put("r2", "c2");
        idCache.put("r3", "c3");
        Assertions.assertTrue(idCache.contains("c1"));
        Assertions.assertTrue(idCache.contains("c2"));
        Assertions.assertTrue(idCache.contains("c3"));
        Assertions.assertTrue(idCache.contains("c1", "c2"));
        Assertions.assertTrue(idCache.contains("c1", "c2", "c3"));
        Assertions.assertFalse(idCache.contains("c4"));
        Thread.sleep(10);
        Assertions.assertFalse(idCache.contains("c1"));
        Assertions.assertFalse(idCache.contains("c2"));
        Assertions.assertFalse(idCache.contains("c3"));
        Assertions.assertFalse(idCache.contains("c1", "c2"));
        Assertions.assertFalse(idCache.contains("c1", "c2", "c3"));
        Assertions.assertFalse(idCache.contains("c4"));
    }

    @Test
    public void testConcurrency() throws Exception {
        final AtomicLong idFactory = new AtomicLong(System.currentTimeMillis() * 1000L);
        final int number_of_threads = 8;
        final int number_of_iterations = 100000;
        final IdCache idCache = new IdCache(100, 10, TimeUnit.SECONDS);
        final AtomicLong time4all = new AtomicLong();
        final List<Thread> threads = new ArrayList<>();
        final List<Throwable> exceptions = new ArrayList<>();
        for (int i = 0; i < number_of_threads; i++) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        for (int j = 0; j < number_of_iterations; j++) {
                            String rid = Long.toString(idFactory.incrementAndGet());
                            String cid = Long.toString(idFactory.incrementAndGet());
                            long startTS = System.nanoTime();
                            idCache.put(rid, cid);
                            Assertions.assertTrue(idCache.contains(cid));
                            Assertions.assertTrue(idCache.remove(rid));
                            long et = System.nanoTime() - startTS;
                            time4all.addAndGet(et);
                        }
                    }
                    catch (Throwable e) {
                        e.printStackTrace();
                        synchronized (exceptions) {
                            exceptions.add(e);
                        }
                    }
                };
            };
            threads.add(t);
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        Assertions.assertTrue(exceptions.isEmpty());
        System.out.println("time4all=" + (time4all.get() / 1000000L) + " msec");
    }
}
