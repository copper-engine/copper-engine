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
package org.copperengine.monitoring.core.data;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.copperengine.monitoring.core.model.MonitoringData;
import org.copperengine.monitoring.core.model.MonitoringDataStorageContentInfo;
import org.copperengine.monitoring.core.model.MonitoringDataStorageInfo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * stores monitoring data in chunked files
 */
public class MonitoringDataStorage {

    static final int FILE_CHUNK_SIZE = 4 * 1024 * 1024;
    static final int LIMIT_POSITION = 0;
    static final int EARLIEST_POSITION = LIMIT_POSITION + 4;
    static final int LATEST_POSITION = EARLIEST_POSITION + 8;
    static final int FIRST_RECORD_POSITION = LATEST_POSITION + 8;
    static final int HEADER_END = FIRST_RECORD_POSITION;

    final File targetPath;
    final String filenamePrefix;
    TargetFile currentTarget;
    final ArrayList<TargetFile> writtenFiles = new ArrayList<TargetFile>();
    long lastTimeStamp = 0;
    final Object lock = new Object();
    boolean closed = false;
    long totalSize;
    final long maxTotalSize;
    final long discardDataBeforeDateMillis;

    static final class TargetFile {
        private File file;
        private RandomAccessFile memoryMappedFile;
        private MappedByteBuffer out;
        private long earliestTimestamp = Long.MAX_VALUE;
        private long latestTimestamp = Long.MIN_VALUE;
        private int limit = FIRST_RECORD_POSITION;
        private int fileSize;
        private Kryo kryo = SerializeUtil.createKryo();
        private ByteBufferOutputStream bostr;
        private Output output;

        /**
         * @param file
         * @throws IOException
         */
        public TargetFile(File file, boolean readOnly) throws IOException {
            this.file = file;
            if (!readOnly) {
                memoryMappedFile = new RandomAccessFile(file, "rw");
                out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, FILE_CHUNK_SIZE);
                fileSize = FILE_CHUNK_SIZE;
                limit = FIRST_RECORD_POSITION;
                out.putLong(LIMIT_POSITION, limit);
                out.putLong(EARLIEST_POSITION, earliestTimestamp);
                out.putLong(LATEST_POSITION, latestTimestamp);
                out.position(FIRST_RECORD_POSITION);
                bostr = new ByteBufferOutputStream(out);
                output = new Output(bostr);
            } else {
                memoryMappedFile = new RandomAccessFile(file, "r");
                fileSize = (int) file.length();
                MappedByteBuffer bb = memoryMappedFile.getChannel().map(MapMode.READ_ONLY, 0, HEADER_END);
                earliestTimestamp = bb.getLong(EARLIEST_POSITION);
                latestTimestamp = bb.getLong(LATEST_POSITION);
                limit = bb.getInt(LIMIT_POSITION);
                memoryMappedFile.close();
                memoryMappedFile = null;
            }
        }

        public void openForWriting() throws IOException {
            OpenedFile of = new OpenedFile(this, Long.MIN_VALUE, Long.MAX_VALUE, false);
            memoryMappedFile = new RandomAccessFile(file, "rw");
            out = memoryMappedFile.getChannel().map(MapMode.READ_WRITE, 0, FILE_CHUNK_SIZE);
            out.position(limit);
            out.putInt(LIMIT_POSITION, FIRST_RECORD_POSITION);
            bostr = new ByteBufferOutputStream(out);
            output = new Output(bostr);
            kryo = SerializeUtil.createKryo();

            MonitoringData md = null;
            Output o = new Output(nullOut);
            while ((md = of.pop()) != null) {
                kryo.writeClassAndObject(o, md);
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            close();
        }

        public synchronized void close() {
            kryo = null;
            if (output != null) {
                try {
                    output.close();
                } catch (BufferOverflowException be) {
                    /*
                     * we can silently ignore this because we alway flush() the output and this is a remainder of the
                     * exception when a file is fully written
                     */
                }
                output = null;
            }
            if (bostr != null) {
                try {
                    bostr.close();
                } catch (IOException e) {
                    /* ignore */
                }
                bostr = null;
            }
            out = null;
            if (memoryMappedFile == null)
                return;
            if (memoryMappedFile.getChannel().isOpen()) {
                try {
                    memoryMappedFile.close();
                } catch (IOException ex) {
                    /* ignore */
                }
            }
            memoryMappedFile = null;
        }

        public boolean serialize(MonitoringData monitoringData) {
            try {
                try {
                    kryo.writeClassAndObject(output, monitoringData);
                    output.flush();
                    return true;
                } catch (KryoException ky) {
                    // The BufferOverflowException might get wrapped, so we have to handle both.
                    // The exception indicates that we have tried to write beyond the buffer's capacity
                    if (ky.getCause() != null && ky.getCause() instanceof BufferOverflowException) {
                        throw (BufferOverflowException) ky.getCause();
                    }
                    throw ky;
                }
            } catch (BufferOverflowException be) {
                return false;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    public MonitoringDataStorage() throws IOException {
        this(createTempFolder("coppermonitoring"), "data", Long.MAX_VALUE, TimeUnit.DAYS, 366L * 100L /*
                                                                                                       * hundred years
                                                                                                       * should be
                                                                                                       * enough for
                                                                                                       * everyone
                                                                                                       */);
    }

    private static File createTempFolder(String tempDirPrefix) throws IOException {
        File temp = File.createTempFile(tempDirPrefix, "");
        temp.delete();
        temp.mkdir();
        return temp;
    }

    public MonitoringDataStorage(String tempDirPrefix, long maxTotalSize, long daysToKeep) throws IOException {
        this(createTempFolder(tempDirPrefix), "data", maxTotalSize, TimeUnit.DAYS, daysToKeep);
    }

    public MonitoringDataStorage(File targetPath, String filenamePrefix) {
        this(targetPath, filenamePrefix, Long.MAX_VALUE, TimeUnit.DAYS, 366L * 100L /*
                                                                                     * hundred years should be enough
                                                                                     * for everyone
                                                                                     */);
    }

    public MonitoringDataStorage(File targetPath, String filenamePrefix, long maxSize, TimeUnit maxAgeUnit, long duration) {
        this.targetPath = targetPath;
        this.filenamePrefix = filenamePrefix;
        this.maxTotalSize = maxSize;
        this.discardDataBeforeDateMillis = maxAgeUnit.toMillis(duration);
        loadFiles();
        ensureCurrentFile(0);
    }

    private void loadFiles() {
        FileFilter f = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (!pathname.getName().startsWith(filenamePrefix))
                    return false;
                String name = pathname.getName().substring(filenamePrefix.length());
                return name.matches("\\.[0-9]+");
            }
        };
        TargetFile latestFile = null;
        long latestTimestamp = Long.MIN_VALUE;
        for (File file : targetPath.listFiles(f)) {
            try {
                TargetFile tf = new TargetFile(file, true);
                totalSize += tf.fileSize;
                if (tf.latestTimestamp > latestTimestamp) {
                    latestTimestamp = tf.latestTimestamp;
                    if (latestFile != null)
                        writtenFiles.add(latestFile);
                    latestFile = tf;
                } else {
                    writtenFiles.add(tf);
                }
            } catch (IOException e) {
                /* silently discard broken files */
            }
        }
        if (latestFile != null) {
            try {
                latestFile.openForWriting();
                currentTarget = latestFile;
            } catch (IOException ioe) {
                /* should never happen */
                writtenFiles.add(latestFile);
            }
        }
    }

    static OutputStream nullOut = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }
    };

    void ensureCurrentFile(int additionalBytes) {
        if (currentTarget == null) {
            currentTarget = createTargetFile();
        }
        if (currentTarget.out.position() + additionalBytes > currentTarget.fileSize) {
            closeCurrentTarget();
            currentTarget = createTargetFile();
        }
    }

    private void closeCurrentTarget() {
        writtenFiles.add(currentTarget);
        currentTarget.close();
        currentTarget = null;
    }

    private TargetFile createTargetFile() {
        try {
            long currentTimeStamp = System.currentTimeMillis();
            File f = null;
            while (true) {
                f = new File(targetPath, filenamePrefix + "." + currentTimeStamp);
                if (!f.exists()) {
                    houseKeeping(FILE_CHUNK_SIZE);
                    TargetFile newTarget = new TargetFile(f, false);
                    totalSize += newTarget.fileSize;
                    return newTarget;
                }
                ++currentTimeStamp;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void houseKeeping(int additionalSize) {
        long discardBefore = System.currentTimeMillis() - discardDataBeforeDateMillis;
        synchronized (lock) {
            Collections.sort(writtenFiles, new Comparator<TargetFile>() {
                @Override
                public int compare(TargetFile o1, TargetFile o2) {
                    if (o1.latestTimestamp < o2.latestTimestamp)
                        return -1;
                    if (o1.latestTimestamp > o2.latestTimestamp)
                        return 1;
                    return System.identityHashCode(o1) - System.identityHashCode(o2);
                }
            });
            int i = 0;
            while (totalSize + additionalSize > maxTotalSize && i < writtenFiles.size()) {
                int size = writtenFiles.get(i).fileSize;
                if (!writtenFiles.get(i).file.delete())
                    break;
                writtenFiles.remove(i);
                totalSize -= size;
                ++i;
            }
            i = 0;
            while (!writtenFiles.isEmpty() && writtenFiles.get(0).latestTimestamp < discardBefore && i < writtenFiles.size()) {
                int size = writtenFiles.get(0).fileSize;
                if (!writtenFiles.get(0).file.delete())
                    break;
                writtenFiles.remove(0);
                totalSize -= size;
                ++i;
            }
        }

    }

    /**
     * @param monitoringData
     *         {@link MonitoringData#getTimeStamp() must be not null}
     */
    public void write(MonitoringData monitoringData) {
        assert monitoringData.getTimeStamp() != null;
        long referenceMillis = monitoringData.getTimeStamp().getTime();
        synchronized (lock) {
            if (closed) {
                throw new RuntimeException(new ClosedChannelException());
            }
            ensureCurrentFile(0);
            if (!currentTarget.serialize(monitoringData)) {
                closeCurrentTarget();
                write(monitoringData);
                return;
            }

            if (currentTarget.earliestTimestamp > referenceMillis) {
                currentTarget.earliestTimestamp = referenceMillis;
                currentTarget.out.putLong(EARLIEST_POSITION, referenceMillis);
            }
            if (currentTarget.latestTimestamp < referenceMillis) {
                currentTarget.latestTimestamp = referenceMillis;
                currentTarget.out.putLong(LATEST_POSITION, referenceMillis);
            }
            currentTarget.limit = currentTarget.out.position();
            currentTarget.out.putInt(LIMIT_POSITION, currentTarget.limit);
        }
    }

    public void close() throws IOException {
        synchronized (lock) {
            if (currentTarget != null)
                closeCurrentTarget();
            closed = true;
        }
    }

    static class OpenedFile {

        ArrayList<MonitoringData> monitoringDataList;

        public OpenedFile(TargetFile f, long fromTime, long toTime, boolean reverse) throws IOException {
            RandomAccessFile randomAccessFile = new RandomAccessFile(f.file, "r");
            randomAccessFile.seek(FIRST_RECORD_POSITION);
            byte[] data = new byte[f.limit - FIRST_RECORD_POSITION];
            int c = 0;
            while ((c += randomAccessFile.read(data, c, data.length - c)) < data.length)
                ;
            randomAccessFile.close();
            readData(new Input(data), SerializeUtil.createKryo(), fromTime, toTime, reverse);

        }

        private void readData(Input i, Kryo kryo, long fromTime, long toTime, final boolean reverse) {
            monitoringDataList = new ArrayList<MonitoringData>();
            try {
                while (i.available() > 0) {
                    MonitoringData data = (MonitoringData) kryo.readClassAndObject(i);
                    if (data.getTimeStamp().getTime() <= toTime && data.getTimeStamp().getTime() >= fromTime) {
                        monitoringDataList.add(data);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Collections.sort(monitoringDataList, new Comparator<MonitoringData>() {
                @Override
                public int compare(MonitoringData o1, MonitoringData o2) {
                    return o1.getTimeStamp().compareTo(o2.getTimeStamp());
                }
            });
            if (!reverse)
                Collections.reverse(monitoringDataList);
        }

        public MonitoringData pop() {
            if (monitoringDataList.isEmpty())
                return null;
            return monitoringDataList.remove(monitoringDataList.size() - 1);
        }

        public void push(MonitoringData data) {
            monitoringDataList.add(data);
        }

    }

    /**
     * @param fromDate
     *         null = no lower bound
     * @param toDate
     *         null = no upper bound
     */
    public Iterable<MonitoringData> read(Date fromDate, Date toDate) {
        return read(fromDate, toDate, false);
    }

    public Iterable<MonitoringData> readReverse(Date fromDate, Date toDate) {
        return read(fromDate, toDate, true);
    }

    public Iterable<MonitoringData> read(Date fromDate, Date toDate, final boolean reverse) {
        final ArrayList<TargetFile> filesToRead = new ArrayList<TargetFile>();
        final long fromTime = fromDate == null ? Long.MIN_VALUE : fromDate.getTime();
        final long toTime = toDate == null ? Long.MAX_VALUE : toDate.getTime();
        synchronized (lock) {
            for (TargetFile target : writtenFiles) {
                if ((fromTime <= target.latestTimestamp || toTime >= target.earliestTimestamp) && target.limit > FIRST_RECORD_POSITION)
                    filesToRead.add(target);
            }
            if (currentTarget != null && (fromTime <= currentTarget.latestTimestamp || toTime >= currentTarget.earliestTimestamp) && currentTarget.limit > FIRST_RECORD_POSITION) {
                filesToRead.add(currentTarget);
            }
        }
        Comparator<TargetFile> cmp =
                reverse ?
                        new Comparator<TargetFile>() {
                            @Override
                            public int compare(TargetFile o1, TargetFile o2) {
                                if (o1.latestTimestamp < o2.latestTimestamp)
                                    return 1;
                                if (o1.latestTimestamp > o2.latestTimestamp)
                                    return -1;
                                if (o1.earliestTimestamp < o2.earliestTimestamp)
                                    return 1;
                                if (o1.earliestTimestamp > o2.earliestTimestamp)
                                    return -1;
                                return System.identityHashCode(o2) - System.identityHashCode(o1);
                            }
                        } :
                        new Comparator<TargetFile>() {
                            @Override
                            public int compare(TargetFile o1, TargetFile o2) {
                                if (o1.earliestTimestamp < o2.earliestTimestamp)
                                    return -1;
                                if (o1.earliestTimestamp > o2.earliestTimestamp)
                                    return 1;
                                if (o1.latestTimestamp < o2.latestTimestamp)
                                    return -1;
                                if (o1.latestTimestamp > o2.latestTimestamp)
                                    return 1;
                                return System.identityHashCode(o2) - System.identityHashCode(o1);
                            }
                        };

        Collections.sort(filesToRead, cmp);

        return new Iterable<MonitoringData>() {

            @Override
            @SuppressWarnings("unchecked")
            public Iterator<MonitoringData> iterator() {
                return new Iterator<MonitoringData>() {

                    // Has to be sorted in order of ascending earliestTimestamp. openFiles() depends on that
                    @SuppressWarnings("unchecked")
                    ArrayList<TargetFile> files = (ArrayList<TargetFile>) filesToRead.clone();
                    ArrayList<OpenedFile> openFiles = new ArrayList<OpenedFile>();
                    long currentTimestamp;
                    MonitoringData nextElement;

                    boolean openFiles() {
                        if (files.isEmpty()) {
                            return false;
                        }
                        if (currentTimestamp == 0)
                            currentTimestamp = reverse ? files.get(0).latestTimestamp : files.get(0).earliestTimestamp;
                        ListIterator<TargetFile> it = files.listIterator();
                        while (it.hasNext()) {
                            TargetFile tf = it.next();
                            if (reverse) {
                                if (tf.latestTimestamp < currentTimestamp)
                                    break;
                            } else {
                                if (tf.earliestTimestamp > currentTimestamp)
                                    break;
                            }
                            it.remove();
                            try {
                                openFiles.add(new OpenedFile(tf, fromTime, toTime, reverse));
                                // we have to readjust current time stamp,
                                // because the already-opened file may skip some timestamps that are present in the
                                // newly-opened file
                                // else we could skip opening a file in the middle of these timestamps
                                currentTimestamp = reverse ?
                                        Math.max(currentTimestamp, tf.latestTimestamp)
                                        : Math.min(currentTimestamp, tf.earliestTimestamp);
                            } catch (IOException e) {
                                // if file cannot be opened, we simple ignore it.
                            }
                        }
                        return !openFiles.isEmpty();
                    }

                    @Override
                    public boolean hasNext() {
                        if (nextElement != null)
                            return true;
                        openFiles();
                        return (nextElement = popNext()) != null;
                    }

                    private MonitoringData popNext() {
                        MonitoringData monitoringData = null;
                        OpenedFile dpSource = null;
                        ListIterator<OpenedFile> it = openFiles.listIterator();
                        while (it.hasNext()) {
                            OpenedFile of = it.next();
                            MonitoringData nextDp = of.pop();
                            if (nextDp == null) {
                                it.remove();
                                continue;
                            }
                            if (monitoringData == null || ((!reverse && nextDp.getTimeStamp().getTime() < monitoringData.getTimeStamp().getTime()) || (reverse && nextDp.getTimeStamp().getTime() > monitoringData.getTimeStamp().getTime()))) {
                                if (monitoringData != null) {
                                    dpSource.push(monitoringData);
                                }
                                dpSource = of;
                                monitoringData = nextDp;
                            }
                        }
                        if (monitoringData != null) {
                            currentTimestamp = monitoringData.getTimeStamp().getTime();
                            if (!files.isEmpty() && ((!reverse && files.get(0).earliestTimestamp < currentTimestamp) || (reverse && files.get(0).latestTimestamp > currentTimestamp))) {
                                dpSource.push(monitoringData);
                                openFiles();
                                return popNext();
                            }
                        } else if (!files.isEmpty()) {
                            currentTimestamp = reverse ? files.get(0).latestTimestamp : files.get(0).earliestTimestamp;
                            openFiles();
                            return popNext();
                        }

                        return monitoringData;
                    }

                    @Override
                    public MonitoringData next() {
                        if (!hasNext())
                            throw new NoSuchElementException();
                        MonitoringData next = nextElement;
                        nextElement = null;
                        return next;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                };
            }
        };

    }

    public Date getMinDate() {
        synchronized (lock) {
            long min = currentTarget == null ? System.currentTimeMillis() : currentTarget.earliestTimestamp;
            for (TargetFile target : writtenFiles) {
                min = Math.min(target.earliestTimestamp, min);
            }
            return new Date(min);
        }
    }

    public Date getMaxDate() {
        synchronized (lock) {
            long max = currentTarget == null ? System.currentTimeMillis() : currentTarget.latestTimestamp;
            for (TargetFile target : writtenFiles) {
                max = Math.max(target.latestTimestamp, max);
            }
            return new Date(max);
        }
    }

    public MonitoringDataStorageInfo getMonitroingDataStorageInfo() {

        final HashMap<String, MonitoringDataStorageContentInfo> classToInfo = new HashMap<String, MonitoringDataStorageContentInfo>();
        for (MonitoringData data : read(null, null)) {
            String clazz = data.getClass().getName();
            MonitoringDataStorageContentInfo info = classToInfo.get(clazz);
            if (info == null) {
                info = new MonitoringDataStorageContentInfo(clazz, 1l);
            } else {
                info.setCount(info.getCount() + 1);
            }
            classToInfo.put(clazz, info);
        }
        long lSize = 0;
        synchronized (lock) {
            for (TargetFile tf : writtenFiles)
                lSize += tf.limit;
            if (currentTarget != null)
                lSize += currentTarget.limit;
        }
        final double size = (lSize / 1024.0d / 1024.0d);
        return new MonitoringDataStorageInfo(size, targetPath.getAbsolutePath(), new ArrayList<MonitoringDataStorageContentInfo>(classToInfo.values()), getMinDate(), getMaxDate());
    }

}
