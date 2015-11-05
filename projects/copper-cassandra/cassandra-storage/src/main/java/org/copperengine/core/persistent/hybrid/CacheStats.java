package org.copperengine.core.persistent.hybrid;

import java.util.concurrent.atomic.AtomicLong;

public class CacheStats {

    private AtomicLong numberOfReads = new AtomicLong();
    private AtomicLong numberOfCacheHits = new AtomicLong();
    private AtomicLong numberOfCacheMisses = new AtomicLong();

    public void incNumberOfReads(boolean hit) {
        numberOfReads.incrementAndGet();
        if (hit)
            numberOfCacheHits.incrementAndGet();
        else
            numberOfCacheMisses.incrementAndGet();
    }

    public long getNumberOfCacheHits() {
        return numberOfCacheHits.get();
    }

    public long getNumberOfCacheMisses() {
        return numberOfCacheMisses.get();
    }

    public long getNumberOfReads() {
        return numberOfReads.get();
    }

    @Override
    public String toString() {
        return "CacheStats [numberOfReads=" + numberOfReads + ", numberOfCacheHits=" + numberOfCacheHits + ", numberOfCacheMisses=" + numberOfCacheMisses + "]";
    }

}
