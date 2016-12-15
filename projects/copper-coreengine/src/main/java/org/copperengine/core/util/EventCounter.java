package org.copperengine.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventCounter {

    private static final Logger logger = LoggerFactory.getLogger(EventCounter.class);

    final ConcurrentHashMap<Long, AtomicLong> eventCounters = new ConcurrentHashMap<>();
    final AtomicLong lastTS = new AtomicLong(System.currentTimeMillis());
    final int maxSize;

    public EventCounter(int maxSize) {
        if (maxSize <= 0)
            throw new IllegalArgumentException();
        this.maxSize = maxSize;
    }

    public void countEvent() {
        countEvent(System.currentTimeMillis());
    }

    public long getNumberOfEvents(int historyMinutes) {
        return getNumberOfEvents(historyMinutes, System.currentTimeMillis());
    }

    void countEvent(final long now) {
        try {
            checkClockReset(now);
            final long currentMinute = now - (now % 60000L);
            AtomicLong x = eventCounters.get(currentMinute);
            boolean checkSize = false;
            while (x == null) {
                x = eventCounters.putIfAbsent(currentMinute, new AtomicLong(0));
                x = eventCounters.get(currentMinute);
                checkSize = true;
            }

            if (checkSize) {
                if (eventCounters.size() > maxSize) {
                    List<Long> keys = new ArrayList<>(eventCounters.keySet());
                    if (keys.size() > maxSize) {
                        Collections.sort(keys);
                        int countElements2remove = keys.size() - maxSize;
                        for (int i=0; i<countElements2remove; i++) {
                            eventCounters.remove(keys.get(i));
                        }
                    }
                }
            }
            x.incrementAndGet();
        }
        catch(Exception e) {
            logger.error("Unexpected exception!",e);
        }
    }

    void checkClockReset(final long now) {
        if (lastTS.get() > now) {
            // clock has been reset - we start with an empty map, because everything else is useless
            eventCounters.clear();
            lastTS.set(now);
        }
        if (lastTS.get() < now) {
            lastTS.set(now);
        }
    }

    long getNumberOfEvents(final int historyMinutes, final long now) {
        if (historyMinutes <= 0 || now < 0) {
            throw new IllegalArgumentException();
        }
        final long currentMinute = now - (now % 60000L);
        long counter = 0;
        for (int i=0; i<historyMinutes; i++) {
            long key = currentMinute - (long)i * 60000L;
            AtomicLong x = eventCounters.get(key);
            counter += (x == null ? 0 : x.get());
        }
        return counter;
    }

}
