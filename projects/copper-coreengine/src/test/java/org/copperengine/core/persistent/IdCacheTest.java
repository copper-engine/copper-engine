package org.copperengine.core.persistent;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class IdCacheTest {

    @Test
    public void testIdCacheMaxSize() {
        IdCache idCache = new IdCache(3, 10, TimeUnit.SECONDS);
        idCache.put("r1", "c1");
        idCache.put("r2", "c2");
        idCache.put("r3", "c3");
        Assert.assertTrue(idCache.contains("c1"));
        Assert.assertTrue(idCache.contains("c2"));
        Assert.assertTrue(idCache.contains("c3"));
        Assert.assertTrue(idCache.contains("c1", "c2"));
        Assert.assertTrue(idCache.contains("c1", "c2", "c3"));
        Assert.assertFalse(idCache.contains("c4"));
        idCache.put("r4", "c4");
        Assert.assertFalse(idCache.contains("c1"));
        Assert.assertTrue(idCache.contains("c2"));
        Assert.assertTrue(idCache.contains("c3"));
        Assert.assertFalse(idCache.contains("c1", "c2"));
        Assert.assertFalse(idCache.contains("c1", "c2", "c3"));
        Assert.assertTrue(idCache.contains("c4"));
        Assert.assertTrue(idCache.contains("c2", "c3", "c4"));
    }

    @Test
    public void testIdCacheTTL() throws Exception {
        IdCache idCache = new IdCache(3, 5, TimeUnit.MILLISECONDS);
        idCache.put("r1", "c1");
        idCache.put("r2", "c2");
        idCache.put("r3", "c3");
        Assert.assertTrue(idCache.contains("c1"));
        Assert.assertTrue(idCache.contains("c2"));
        Assert.assertTrue(idCache.contains("c3"));
        Assert.assertTrue(idCache.contains("c1", "c2"));
        Assert.assertTrue(idCache.contains("c1", "c2", "c3"));
        Assert.assertFalse(idCache.contains("c4"));
        Thread.sleep(10);
        Assert.assertFalse(idCache.contains("c1"));
        Assert.assertFalse(idCache.contains("c2"));
        Assert.assertFalse(idCache.contains("c3"));
        Assert.assertFalse(idCache.contains("c1", "c2"));
        Assert.assertFalse(idCache.contains("c1", "c2", "c3"));
        Assert.assertFalse(idCache.contains("c4"));
    }

}
