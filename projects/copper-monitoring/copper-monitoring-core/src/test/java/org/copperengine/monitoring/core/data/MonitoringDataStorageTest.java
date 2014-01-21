/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.copperengine.monitoring.core.model.MonitoringData;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class MonitoringDataStorageTest {

    static final String filename = "test";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testSimpleCase() throws IOException {
        File tmpDir = testFolder.newFolder();
        File f1 = new File(tmpDir, filename + ".1");
        writeFile(f1, new MonitoringDataDummy(new Date(1), "1"), new MonitoringDataDummy(new Date(2), "2"));
        File f2 = new File(tmpDir, filename + ".2");
        writeFile(f2, new MonitoringDataDummy(new Date(3), "3"), new MonitoringDataDummy(new Date(4), "4"));
        File f3 = new File(tmpDir, filename + ".3");
        writeFile(f3, new MonitoringDataDummy(new Date(5), "5"), new MonitoringDataDummy(new Date(6), "6"));
        MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename);

        ArrayList<MonitoringData> read = new ArrayList<MonitoringData>();
        for (MonitoringData in : storage.read(new Date(1), new Date(6))) {
            read.add(in);
        }
        Assert.assertEquals(6, read.size());
        Assert.assertEquals("1", ((MonitoringDataDummy) read.get(0)).value);
        Assert.assertEquals("6", ((MonitoringDataDummy) read.get(5)).value);
    }

    @Test
    public void testSimpleCasePermanentDir() throws IOException {
        String dirPath = testFolder.getRoot().getAbsolutePath() + "/permanent-storage-dir";

        MonitoringDataStorage storage1 = new MonitoringDataStorage(dirPath, 32, 3);
        
        File f1 = new File(dirPath, "data.1");
        writeFile(f1, new MonitoringDataDummy(new Date(1), "1"), new MonitoringDataDummy(new Date(2), "2"));
                
        storage1.read(new Date(0), new Date());
        ArrayList<MonitoringData> read1 = new ArrayList<MonitoringData>();
        for (MonitoringData in : storage1.read(new Date(1), new Date(6))) {
            read1.add(in);
        }
        Assert.assertEquals(0, read1.size());
                
        File f2 = new File(dirPath, "data.2");
        writeFile(f2, new MonitoringDataDummy(new Date(3), "3"), new MonitoringDataDummy(new Date(4), "4"));
        File f3 = new File(dirPath, "data.3");
        writeFile(f3, new MonitoringDataDummy(new Date(5), "5"), new MonitoringDataDummy(new Date(6), "6"));

        MonitoringDataStorage storage2 = new MonitoringDataStorage(dirPath, 32, 3);        
        ArrayList<MonitoringData> read2 = new ArrayList<MonitoringData>();
        for (MonitoringData in : storage2.read(new Date(1), new Date(6))) {
            read2.add(in);
        }
        Assert.assertEquals(6, read2.size());
        Assert.assertEquals("1", ((MonitoringDataDummy) read2.get(0)).value);
        Assert.assertEquals("6", ((MonitoringDataDummy) read2.get(5)).value);
    }

    static class HugeData implements MonitoringData {

        byte[] b;

        public HugeData() {
            b = new byte[1024 * 2048];
            new Random().nextBytes(b);
        }

        @Override
        public Date getTimeStamp() {
            return new Date(1);
        }
    }

    @Test
    public void testHugeData() throws IOException {
        File tmpDir = testFolder.newFolder();
        MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename);
        storage.write(new HugeData());

        ArrayList<MonitoringData> read = new ArrayList<MonitoringData>();
        for (MonitoringData in : storage.read(new Date(1), new Date(6))) {
            read.add(in);
        }
        Assert.assertEquals(1, read.size());
        Assert.assertTrue(read.get(0) instanceof HugeData);
    }

    @Test
    public void testSortedRead() throws IOException {
        File tmpDir = testFolder.newFolder();
        File f1 = new File(tmpDir, filename + ".1");
        writeFile(f1, new MonitoringDataDummy(new Date(1), "1"), new MonitoringDataDummy(new Date(6), "6"));
        File f2 = new File(tmpDir, filename + ".2");
        writeFile(f2, new MonitoringDataDummy(new Date(2), "2"), new MonitoringDataDummy(new Date(5), "5"));
        File f3 = new File(tmpDir, filename + ".3");
        writeFile(f3, new MonitoringDataDummy(new Date(3), "3"), new MonitoringDataDummy(new Date(4), "4"));
        MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename);

        ArrayList<MonitoringData> read = new ArrayList<MonitoringData>();
        for (MonitoringData in : storage.read(new Date(1), new Date(6))) {
            read.add(in);
        }
        Assert.assertEquals(6, read.size());
        Assert.assertEquals("1", ((MonitoringDataDummy) read.get(0)).value);
        Assert.assertEquals("2", ((MonitoringDataDummy) read.get(1)).value);
        Assert.assertEquals("3", ((MonitoringDataDummy) read.get(2)).value);
        Assert.assertEquals("4", ((MonitoringDataDummy) read.get(3)).value);
        Assert.assertEquals("5", ((MonitoringDataDummy) read.get(4)).value);
        Assert.assertEquals("6", ((MonitoringDataDummy) read.get(5)).value);
    }

    @Test
    public void testSkipSome() throws IOException {
        File tmpDir = testFolder.newFolder("montoringdatastoragetest");
        File f1 = new File(tmpDir, filename + ".1");
        writeFile(f1, new MonitoringDataDummy(new Date(1), "1"), new MonitoringDataDummy(new Date(6), "6"));
        File f2 = new File(tmpDir, filename + ".2");
        writeFile(f2, new MonitoringDataDummy(new Date(2), "2"), new MonitoringDataDummy(new Date(5), "5"));
        File f3 = new File(tmpDir, filename + ".3");
        writeFile(f3, new MonitoringDataDummy(new Date(3), "3"), new MonitoringDataDummy(new Date(4), "4"));
        MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename);

        ArrayList<MonitoringData> read = new ArrayList<MonitoringData>();
        for (MonitoringData in : storage.read(new Date(1), new Date(6))) {
            read.add(in);
        }
        Assert.assertEquals(6, read.size());
        Assert.assertEquals("1", ((MonitoringDataDummy) read.get(0)).value);
        Assert.assertEquals("6", ((MonitoringDataDummy) read.get(5)).value);
    }

    @Test
    public void testSortedReadReverse() throws IOException {
        File tmpDir = testFolder.newFolder();
        File f1 = new File(tmpDir, filename + ".1");
        writeFile(f1, new MonitoringDataDummy(new Date(1), "1"), new MonitoringDataDummy(new Date(6), "6"));
        File f2 = new File(tmpDir, filename + ".2");
        writeFile(f2, new MonitoringDataDummy(new Date(2), "2"), new MonitoringDataDummy(new Date(5), "5"));
        File f3 = new File(tmpDir, filename + ".3");
        writeFile(f3, new MonitoringDataDummy(new Date(3), "3"), new MonitoringDataDummy(new Date(4), "4"));
        MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename);

        ArrayList<MonitoringData> read = new ArrayList<MonitoringData>();
        for (MonitoringData in : storage.readReverse(new Date(1), new Date(6))) {
            read.add(in);
        }
        Assert.assertEquals(6, read.size());
        Assert.assertEquals("1", ((MonitoringDataDummy) read.get(5)).value);
        Assert.assertEquals("2", ((MonitoringDataDummy) read.get(4)).value);
        Assert.assertEquals("3", ((MonitoringDataDummy) read.get(3)).value);
        Assert.assertEquals("4", ((MonitoringDataDummy) read.get(2)).value);
        Assert.assertEquals("5", ((MonitoringDataDummy) read.get(1)).value);
        Assert.assertEquals("6", ((MonitoringDataDummy) read.get(0)).value);

    }

    private String getDummyString(long length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append("X");
        }
        return builder.toString();
    }

    @Test
    public void testHouseKeeping() throws Exception {
        File tmpDir = testFolder.newFolder();
        MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename, 9000000, TimeUnit.DAYS, 10);
        storage.write(new MonitoringDataDummy(getDummyString(MonitoringDataStorage.FILE_CHUNK_SIZE - 1000)));
        Thread.sleep(100);
        System.gc();
        Runtime.getRuntime().runFinalization();
        storage.write(new MonitoringDataDummy(getDummyString(MonitoringDataStorage.FILE_CHUNK_SIZE - 1000)));
        Thread.sleep(100);
        System.gc();
        Runtime.getRuntime().runFinalization();
        storage.write(new MonitoringDataDummy(getDummyString(MonitoringDataStorage.FILE_CHUNK_SIZE - 1000)));
        storage.close();
        File[] files = tmpDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(filename);
            }
        });
        Assert.assertEquals(2, files.length);
    }

    @Test
    public void testHousekeepingDate() throws Exception {
        File tmpDir = testFolder.newFolder();
        long now = System.currentTimeMillis();
        MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename, Long.MAX_VALUE, TimeUnit.MINUTES, 1);
        storage.write(new MonitoringDataDummy(new Date(now - 60001), getDummyString(MonitoringDataStorage.FILE_CHUNK_SIZE - 1000)));
        Thread.sleep(100);
        System.gc();
        Runtime.getRuntime().runFinalization();
        storage.write(new MonitoringDataDummy(new Date(now), getDummyString(MonitoringDataStorage.FILE_CHUNK_SIZE - 1000)));
        Thread.sleep(100);
        System.gc();
        Runtime.getRuntime().runFinalization();
        storage.write(new MonitoringDataDummy(new Date(now), getDummyString(MonitoringDataStorage.FILE_CHUNK_SIZE - 1000)));
        Thread.sleep(100);
        System.gc();
        Runtime.getRuntime().runFinalization();
        storage.write(new MonitoringDataDummy(new Date(now), getDummyString(MonitoringDataStorage.FILE_CHUNK_SIZE - 1000)));
        Thread.sleep(100);
        System.gc();
        Runtime.getRuntime().runFinalization();
        storage.write(new MonitoringDataDummy(new Date(now), getDummyString(MonitoringDataStorage.FILE_CHUNK_SIZE - 1000)));
        storage.close();
        File[] files = tmpDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(filename);
            }

        });
        Assert.assertEquals(4, files.length);
    }

    @Test
    public void testFileReuse() throws Exception {
        File tmpDir = testFolder.newFolder();
        long d = System.currentTimeMillis();
        for (int i = 0; i < 127; ++i) {
            MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename);
            storage.write(new MonitoringDataDummy(new Date(d++), getDummyString((byte) i)));
            storage.close();
            Thread.yield();
        }
        File[] files = tmpDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(filename);
            }
        });
        Assert.assertEquals(1, files.length);
        MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename);
        int i = 0;
        for (MonitoringData in : storage.read(null, null)) {
            Assert.assertEquals(i++, ((MonitoringDataDummy) in).value.length());
        }
        Assert.assertEquals(127, i);
        storage.close();

    }

    private void writeFile(File f, MonitoringData... values) throws IOException {
        RandomAccessFile ranAccess = new RandomAccessFile(f, "rw");
        MappedByteBuffer b = ranAccess.getChannel().map(MapMode.READ_WRITE, 0, 1024);
        long earliest = Long.MAX_VALUE;
        long latest = Long.MIN_VALUE;
        for (MonitoringData d : values) {
            earliest = Math.min(d.getTimeStamp().getTime(), earliest);
            latest = Math.max(d.getTimeStamp().getTime(), latest);
        }
        Kryo kryo = SerializeUtil.createKryo();
        b.position(MonitoringDataStorage.FIRST_RECORD_POSITION);
        Output output = new Output(new ByteBufferOutputStream(b));
        for (MonitoringData data : values) {
            kryo.writeClassAndObject(output, data);
        }
        output.close();
        int limit = b.position();

        b.putLong(MonitoringDataStorage.EARLIEST_POSITION, earliest);
        b.putLong(MonitoringDataStorage.LATEST_POSITION, latest);
        b.putInt(MonitoringDataStorage.LIMIT_POSITION, limit);
        ranAccess.close();
    }

    @Test
    public void test_Kryo() throws IOException {
        final File newFile = testFolder.newFile();
        writeFile(newFile, new MonitoringDataDummy(new Date(42), "blabla1"), new MonitoringDataDummy(new Date(43), "blabla2"));

        RandomAccessFile ranAccess = new RandomAccessFile(newFile, "rw");
        MappedByteBuffer b = ranAccess.getChannel().map(MapMode.READ_WRITE, 0, 218);

        long earliestTimestamp = b.getLong(MonitoringDataStorage.EARLIEST_POSITION);
        long latestTimestamp = b.getLong(MonitoringDataStorage.LATEST_POSITION);
        assertEquals(42, earliestTimestamp);
        assertEquals(43, latestTimestamp);

        Kryo kryo = new Kryo();
        Input input = new Input(new ByteBufferInputStream(b));
        input.setPosition(MonitoringDataStorage.FIRST_RECORD_POSITION);
        assertEquals("blabla1", ((MonitoringDataDummy) kryo.readClassAndObject(input)).value);
        assertEquals("blabla2", ((MonitoringDataDummy) kryo.readClassAndObject(input)).value);
        assertNull(kryo.readClassAndObject(input));
        b.putInt(MonitoringDataStorage.LIMIT_POSITION, b.position());
        b.position(0);
        ranAccess.close();
    }

    @Test
    public void test_microbenchmark() throws IOException {
        File tmpDir = testFolder.newFolder();
        MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename);

        ArrayList<Long> delats = new ArrayList<Long>();
        for (int i = 0; i < 40000; i++) {
            long start = System.nanoTime();
            storage.write(new MonitoringDataDummy(new Date(42), "X"));
            delats.add(System.nanoTime() - start);
        }

        // double sum=0;
        // for (Long time : delats) {
        // sum +=time;
        // }
        //
        // System.out.println((sum/delats.size())/(1000*1000));
    }

}
