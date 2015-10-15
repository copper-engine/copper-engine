/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class ConcurrentBatchedWorker {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentBatchedWorker.class);

    private Thread worker;
    private Queue<List<PersistentWorkflow<?>>> queue = new LinkedList<List<PersistentWorkflow<?>>>();
    int flushSize = 50;
    private boolean shutdown = false;
    private List<PersistentWorkflow<?>> currentList = new ArrayList<PersistentWorkflow<?>>(flushSize);
    private static final List<PersistentWorkflow<?>> TOKEN = Collections.unmodifiableList(new ArrayList<PersistentWorkflow<?>>());

    public void setFlushSize(int flushSize) {
        this.flushSize = flushSize;
    }

    public void start() {
        if (worker != null)
            throw new IllegalStateException();
        worker = new Thread("") {
            @Override
            public void run() {
                doRun();
            }
        };
        worker.start();
        logger.info("Started!");
    }

    public void shutdown() {
        if (!shutdown) {
            shutdown = true;
            worker.interrupt();
        }
        logger.info("Stopped!");
    }

    public void beginTxn() {

    }

    public void endTxn() {
        logger.trace("endTxn...");
        if (currentList.size() > 0) {
            synchronized (queue) {
                queue.add(currentList);
                queue.notify();
            }
            currentList = new ArrayList<PersistentWorkflow<?>>(flushSize);
        }
        synchronized (queue) {
            queue.add(TOKEN);
            queue.notify();
            while (queue.size() > 0) {
                try {
                    queue.wait(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        logger.trace("endTxn done");
    }

    public void enqueue(PersistentWorkflow<?> wf) {
        if (wf == null)
            throw new NullPointerException();
        currentList.add(wf);
        if (currentList.size() >= flushSize) {
            synchronized (queue) {
                queue.add(currentList);
                queue.notify();
            }
            currentList = new ArrayList<PersistentWorkflow<?>>(flushSize);
        }
    }

    private void doRun() {
        while (!shutdown) {
            try {
                List<PersistentWorkflow<?>> list = null;
                synchronized (queue) {
                    list = queue.poll();
                    while (list == null) {
                        queue.wait();
                        list = queue.poll();
                    }
                    if (list == TOKEN) {
                        logger.trace("Found token");
                        queue.notify();
                    }
                }
                if (list != TOKEN) {
                    if (logger.isTraceEnabled())
                        logger.trace("Calling process for list with " + list.size() + " element(s");
                    process(list);
                }
            } catch (InterruptedException e) {
                // ignore
            } catch (Exception e) {
                logger.error("iteration failed", e);
            }
        }
    }

    abstract void process(List<PersistentWorkflow<?>> list);

}
