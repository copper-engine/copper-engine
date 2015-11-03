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
package org.copperengine.core.persistent.hybrid;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the {@link TimeoutManager} interface.
 *
 * @author austermann
 */
public final class DefaultTimeoutManager extends Thread implements TimeoutManager {

    private final static Logger logger = LoggerFactory.getLogger(TimeoutManager.class);
    private final static long SLOT_INTERVAL = 25;

    private final Map<Long, TimeoutSlot> slots = new TreeMap<Long, TimeoutSlot>();
    private long nextWakeupTime;
    private boolean shutdown = false;
    private volatile boolean started = false;

    public DefaultTimeoutManager() {
        super("copper.Timeoutmanager");
    }

    static long processSlot(Date timeoutTS) {
        return processSlot(timeoutTS.getTime());
    }

    static long processSlot(long timeoutTS) {
        return ((timeoutTS / SLOT_INTERVAL) + 1) * SLOT_INTERVAL;
    }

    public synchronized TimeoutManager startup() {
        this.setDaemon(true);
        this.start();
        started = true;
        return this;
    }

    public synchronized TimeoutManager shutdown() {
        if (shutdown)
            return this;
        shutdown = true;
        synchronized (slots) {
            slots.notify();
        }
        return this;
    }

    public void run() {
        logger.info("started");
        while (!shutdown) {
            try {
                List<Runnable> expired = new ArrayList<Runnable>(32);
                synchronized (slots) {
                    if (shutdown)
                        break;

                    if (logger.isDebugEnabled())
                        logger.debug("Activated at: " + System.currentTimeMillis());
                    for (Iterator<Map.Entry<Long, TimeoutSlot>> i = slots.entrySet().iterator(); i.hasNext();) {
                        Map.Entry<Long, TimeoutSlot> entry = i.next();
                        long timeoutTime = entry.getKey();
                        if (timeoutTime <= System.currentTimeMillis()) {
                            i.remove();
                            if (logger.isDebugEnabled())
                                logger.debug("Expired slot found at: " + timeoutTime);
                            expired.addAll(entry.getValue().getWfId2RunnableMap().values());
                        } else {
                            break;
                        }
                    }
                }
                for (Runnable r : expired) {
                    try {
                        r.run();
                    } catch (Exception e) {
                        logger.error("run failed", e);
                    }
                }

                synchronized (slots) {
                    if (shutdown)
                        break;

                    Iterator<Entry<Long, TimeoutSlot>> i = slots.entrySet().iterator();
                    if (!i.hasNext()) {
                        logger.debug("There are currently no timeout slots - waiting indefinitely...");
                        nextWakeupTime = 0;
                        slots.wait();
                    } else {
                        nextWakeupTime = i.next().getValue().getTimeoutTS();
                        long delay = nextWakeupTime - System.currentTimeMillis();
                        if (delay > 0) {
                            logger.debug("Sleeping for: " + delay + "msec.");
                            slots.wait(delay);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Unexpected exception:", e);
            }
        }
        logger.info("stopped");
    }

    @Override
    public void registerTimeout(final Date _timeoutTS, final String workflowId, final Runnable onTimeout) {
        logger.debug("registerTimeout({}, {})", _timeoutTS, workflowId);

        if (_timeoutTS == null)
            return;

        if (!started)
            throw new IllegalStateException("timeout manager not yet started!");

        final Long timeoutTS = Long.valueOf(processSlot(_timeoutTS));
        if (logger.isDebugEnabled()) {
            long currentTime = System.currentTimeMillis();
            logger.debug("currentTime=" + currentTime);
            logger.debug("timeoutTS=" + timeoutTS);
            logger.debug("nextWakeupTime=" + nextWakeupTime);
        }
        synchronized (slots) {
            TimeoutSlot timeoutSlot = (TimeoutSlot) slots.get(timeoutTS);
            if (timeoutSlot == null) {
                timeoutSlot = new TimeoutSlot(timeoutTS.longValue());
                slots.put(timeoutTS, timeoutSlot);
                if (nextWakeupTime > timeoutTS.longValue() || nextWakeupTime == 0L)
                    slots.notify();
            }
            timeoutSlot.getWfId2RunnableMap().put(workflowId, onTimeout);
        }
    }

    @Override
    public void unregisterTimeout(final Date _timeoutTS, final String workflowId) {
        logger.debug("unregisterTimeout({}, {})", _timeoutTS, workflowId);

        if (_timeoutTS == null)
            return;

        if (!started)
            throw new IllegalStateException("timeout manager not yet started!");

        final Long timeoutTS = Long.valueOf(processSlot(_timeoutTS));
        synchronized (slots) {
            TimeoutSlot timeoutSlot = (TimeoutSlot) slots.get(timeoutTS);
            if (timeoutSlot != null) {
                timeoutSlot.getWfId2RunnableMap().remove(workflowId);
                if (timeoutSlot.getWfId2RunnableMap().isEmpty()) {
                    slots.remove(timeoutTS);
                }
            }
        }
    }

}
