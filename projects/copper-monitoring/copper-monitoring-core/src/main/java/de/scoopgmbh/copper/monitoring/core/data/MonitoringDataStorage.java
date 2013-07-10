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
package de.scoopgmbh.copper.monitoring.core.data;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;

import com.esotericsoftware.kryo.io.Input;

/**
 *  stores monitoring data in chunked files
 *
 */
public class MonitoringDataStorage {

	static final int FILE_CHUNK_SIZE=4*1024*1024;
	static final int LIMIT_POSITION = 0;
	static final int EARLIEST_POSITION = LIMIT_POSITION+4;
	static final int LATEST_POSITION = EARLIEST_POSITION+8;
	static final int FIRST_RECORD_POSITION = LATEST_POSITION+8;
	static final int HEADER_END = FIRST_RECORD_POSITION;

	final File targetPath;
	final String filenamePrefix;
	      TargetFile currentTarget;
	final ArrayList<TargetFile> writtenFiles = new ArrayList<TargetFile>();
	long lastTimeStamp = 0;
	final Object lock = new Object();
	final ArrayBlockingQueue<TargetFile> buffersToForce = new ArrayBlockingQueue<TargetFile>(16,false);
	boolean closed = false;
	final ForceThread forceThread;
	long totalSize;
	final long maxTotalSize;
	final long discardDataBeforeDateMillis;
	
	class ForceThread extends Thread {
		
		ForceThread() {
			super("File forcer thread for '"+new File(targetPath, filenamePrefix).getAbsolutePath()+"'");
		}
		@Override
		public void run() {
			synchronized (buffersToForce) {
				while (true) {
					TargetFile f;
					try {
						f = buffersToForce.take();
						try {
							f.close();
						} catch (IOException e) {
							//ignore
						}
					} catch (InterruptedException e) {
						if (closed) {
							while ((f = buffersToForce.poll()) != null) {
								try {
									f.close();
								} catch (IOException e1) {
									//ignore
								}
							}
							return;
						}
						throw new RuntimeException("Unexpected interruption", e);
					}
				}
			}
		}
	}
	
	static final class TargetFile {
		File             file;
		RandomAccessFile memoryMappedFile;
		MappedByteBuffer out;
		long             earliestTimestamp = Long.MAX_VALUE;
		long             latestTimestamp = Long.MIN_VALUE;
		int              limit = FIRST_RECORD_POSITION;
		int              fileSize;
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			close();
		}
		public synchronized void close() throws IOException {
			if (memoryMappedFile == null)
				return;
			if (!memoryMappedFile.getChannel().isOpen())
				return;
			out.force();
			try {
				memoryMappedFile.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			memoryMappedFile = null;
			out = null;
		}
	}
	
	static final class DataPointer extends Input implements Comparable<DataPointer> {

		final long  timestamp;
		
		DataPointer(ByteBuffer buf) {
			super(buf.array(), buf.position()+12, buf.getInt(buf.position())+12+buf.position());
			this.timestamp = buf.getLong(buf.position()+4);
			buf.position(limit());
		}

		@Override
		public int compareTo(DataPointer o) {
			//Sort descending! This brings efficiency for popping the earliest from a list 
			if (timestamp < o.timestamp)
				return 1;
			if (timestamp > o.timestamp)
				return -1;
			return 0;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
	
	public MonitoringDataStorage(File targetPath, String filenamePrefix) {
		this(targetPath, filenamePrefix, Long.MAX_VALUE, new Date(0));
	}
	
	public MonitoringDataStorage(File targetPath, String filenamePrefix, long maxSize, Date maxAge) {
		this.targetPath = targetPath;
		this.filenamePrefix = filenamePrefix;
		this.maxTotalSize = maxSize;
		this.discardDataBeforeDateMillis = maxAge.getTime();
		loadFiles();
		(forceThread = new ForceThread()).start();
	}
	
	private void loadFiles()  {
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
			TargetFile tf = new TargetFile();
			tf.file = file;
			RandomAccessFile rf;
			try {
				rf = new RandomAccessFile(tf.file,"r");
				tf.fileSize = (int)file.length();
				totalSize += tf.fileSize;
				MappedByteBuffer bb = rf.getChannel().map(MapMode.READ_ONLY, 0, HEADER_END);
				tf.earliestTimestamp = bb.getLong(EARLIEST_POSITION);
				tf.latestTimestamp = bb.getLong(LATEST_POSITION);
				tf.limit = bb.getInt(LIMIT_POSITION);
				rf.close();
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
				latestFile.memoryMappedFile = new RandomAccessFile(latestFile.file, "rw");
				latestFile.out = latestFile.memoryMappedFile.getChannel().map(MapMode.READ_WRITE, 0, latestFile.file.length());
				latestFile.out.position(latestFile.limit);
				currentTarget = latestFile;
			} catch (IOException ioe) {
				/* should never happen */
				writtenFiles.add(latestFile);				
			}
		}
	}

	void ensureCurrentFile(int additionalBytes) throws IOException {
		if (currentTarget == null) {
			currentTarget = createTargetFile();
		}
		if (currentTarget.out.position() + additionalBytes > currentTarget.fileSize) {
			closeCurrentTarget();
			currentTarget = createTargetFile();
		}
	}



	private void closeCurrentTarget() throws IOException {
		writtenFiles.add(currentTarget);
		try {
			buffersToForce.put(currentTarget);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		currentTarget = null;
	}

	private TargetFile createTargetFile() throws IOException {
		TargetFile newTarget = new TargetFile();
		long currentTimeStamp = System.currentTimeMillis();
		do {
			newTarget.file = new File(targetPath, filenamePrefix+"."+currentTimeStamp);
			if (newTarget.file.exists()) {
				++currentTimeStamp;
				continue;
			}
		} while (false);
		houseKeeping(FILE_CHUNK_SIZE);	
		newTarget.memoryMappedFile = new RandomAccessFile(newTarget.file, "rw");
		newTarget.out = newTarget.memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, FILE_CHUNK_SIZE);
		newTarget.fileSize = FILE_CHUNK_SIZE;
		newTarget.out.putLong(LIMIT_POSITION,newTarget.limit);
		newTarget.out.putLong(EARLIEST_POSITION,newTarget.earliestTimestamp);
		newTarget.out.putLong(LATEST_POSITION,newTarget.latestTimestamp);
		newTarget.out.position(FIRST_RECORD_POSITION);
		totalSize += newTarget.fileSize;
		return newTarget;
	}

	
    private void houseKeeping(int additionalSize) {
    	synchronized (lock) {
	    	Collections.sort(writtenFiles, new Comparator<TargetFile>() {
				@Override
				public int compare(TargetFile o1, TargetFile o2) {
					if (o1.latestTimestamp < o2.latestTimestamp)
						return -1;
					if (o1.latestTimestamp > o2.latestTimestamp)
						return 1;
					return System.identityHashCode(o1)-System.identityHashCode(o2);
				}
			});
	    	int i = 0;
	    	while (totalSize + additionalSize > maxTotalSize && i < writtenFiles.size()) {
	    		int size = writtenFiles.get(i).fileSize;
	    		if (writtenFiles.get(i).file.delete()) {
	    			writtenFiles.remove(i);
	    			totalSize -= size;
	    		}
	    		++i;
	    	}
	    	i = 0;
	    	while (!writtenFiles.isEmpty() && writtenFiles.get(0).latestTimestamp < discardDataBeforeDateMillis && i < writtenFiles.size()) {
	    		int size = writtenFiles.get(0).fileSize;
	    		if (writtenFiles.get(0).file.delete()) {
	    			writtenFiles.remove(0);
	    			totalSize -= size;
	    		}
	    		++i;
	    	}
		}
		
	}

	public void write(byte b[]) throws IOException {
        write(new Date(), b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        write(new Date(), b, off, len);
    }

    public void write(Date referenceDate, byte b[]) throws IOException {
        write(referenceDate, b, 0, b.length);
    }

    public void write(Date referenceDate, byte b[], int off, int len) throws IOException {
    	assert referenceDate != null;
    	assert b != null;
    	long referenceMillis = referenceDate.getTime();
        synchronized (lock) {
        	if (closed)
        		throw new ClosedChannelException();
            ensureCurrentFile(len+12);
            if (currentTarget.earliestTimestamp > referenceMillis) {
            	currentTarget.earliestTimestamp = referenceMillis;
	            currentTarget.out.putLong(EARLIEST_POSITION,referenceMillis);
            }
            if (currentTarget.latestTimestamp < referenceMillis) {
            	currentTarget.latestTimestamp = referenceMillis;
	            currentTarget.out.putLong(LATEST_POSITION,referenceMillis);
            }
            currentTarget.out.putInt(b.length);
            currentTarget.out.putLong(referenceMillis);
            currentTarget.out.put(b);
            currentTarget.limit = currentTarget.out.position();
            currentTarget.out.putInt(LIMIT_POSITION,currentTarget.limit);
        }
    }
    
    
    public void close() throws IOException {
    	synchronized (lock) {
    		if (currentTarget != null)
    			closeCurrentTarget();
    		closed = true;
    	}
    	try {
    		forceThread.interrupt();
			forceThread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
    }
 
    static class OpenedFile {
    	byte[] data;
    	ArrayList<DataPointer> dataPointers;
    	
    	public OpenedFile(TargetFile f, long fromTime, long toTime) throws IOException {
    		RandomAccessFile rf = new RandomAccessFile(f.file, "r");
    		MappedByteBuffer b = rf.getChannel().map(MapMode.READ_ONLY, 0, f.limit);
    		int limit = b.getInt(LIMIT_POSITION);
    		byte[] dat = new byte[limit-FIRST_RECORD_POSITION];
    		b.position(FIRST_RECORD_POSITION);
    		b.get(dat, 0, dat.length);
    		rf.close();
    		readDataPointers(ByteBuffer.wrap(dat), fromTime, toTime);
    	}

		private void readDataPointers(ByteBuffer data, long fromTime, long toTime) {
			dataPointers = new ArrayList<DataPointer>();
			while (data.position() < data.limit()) {
				DataPointer dp = new DataPointer(data); //adjusts buf.position()
				if (dp.timestamp <= toTime && dp.timestamp >= fromTime) {
					dataPointers.add(dp);
				}
			}
			Collections.sort(dataPointers);
		}
		
		public DataPointer pop() {
			if (dataPointers.isEmpty())
				return null;
			return dataPointers.remove(dataPointers.size()-1);
		}
		
		public void push(DataPointer p) {
			dataPointers.add(p);
		}
		
    	
    }

    
    public Iterable<Input> read(Date fromDate, Date toDate) {
    	final ArrayList<TargetFile> filesToRead = new ArrayList<TargetFile>();
    	final long fromTime = fromDate == null?Long.MIN_VALUE:fromDate.getTime();
    	final long toTime = toDate == null?Long.MAX_VALUE:toDate.getTime();
        synchronized (lock) {
        	for (TargetFile target : writtenFiles) {
	    		if ((fromTime <= target.latestTimestamp || toTime >= target.earliestTimestamp) && target.limit > FIRST_RECORD_POSITION)
	    			filesToRead.add(target);
	    	}
        	if (currentTarget != null && (fromTime <= currentTarget.latestTimestamp || toTime >= currentTarget.earliestTimestamp) && currentTarget.limit > FIRST_RECORD_POSITION) {
        		filesToRead.add(currentTarget);
        	}
        }
    	Collections.sort(filesToRead, new Comparator<TargetFile>() {
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
				return System.identityHashCode(o1)-System.identityHashCode(o2);
			}
		});
    	
    	return new Iterable<Input>() {
			
			@Override
			public Iterator<Input> iterator() {
				return new Iterator<Input>() {
					
					@SuppressWarnings("unchecked")
					//Has to be sorted in order of ascending earliestTimestamp. openFiles() depends on that
					ArrayList<TargetFile> files = (ArrayList<TargetFile>)filesToRead.clone();
					ArrayList<OpenedFile> openFiles = new ArrayList<OpenedFile>(); 
					long currentTimestamp;
					DataPointer nextElement;
					
					boolean openFiles() {
						if (files.isEmpty()) {
							return false;
						}
						if (currentTimestamp == 0)
							currentTimestamp = files.get(0).earliestTimestamp;
						ListIterator<TargetFile> it = files.listIterator();
						while (it.hasNext()) {
							TargetFile tf = it.next();
							if (tf.earliestTimestamp > currentTimestamp)
								break;
							it.remove();
							try {
								openFiles.add(new OpenedFile(tf, fromTime, toTime));
								//we have to readjust current time stamp,
								//because the already-opened file may skip some timestamps that are present in the newly-opened file
								//else we could skip opening a file in the middle of these timestamps
								currentTimestamp = Math.min(currentTimestamp, tf.earliestTimestamp);
							} catch (IOException e) {
								//if file cannot be opened, we simple ignore it.
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

					private DataPointer popNext()  {
						DataPointer dp = null;
						OpenedFile dpSource = null;
						ListIterator<OpenedFile> it = openFiles.listIterator();
						while (it.hasNext()) {
							OpenedFile of = it.next();
							DataPointer nextDp = of.pop();
							if (nextDp == null) {
								it.remove();
								continue;
							}
							if (dp == null || nextDp.timestamp < dp.timestamp) {
								if (dp != null) {
									dpSource.push(dp);
								}
								dpSource = of;
								dp = nextDp;
							}
						}
						if (dp != null) {
							currentTimestamp = dp.timestamp;
							if (!files.isEmpty() && files.get(0).earliestTimestamp < currentTimestamp) {
								dpSource.push(dp);
								openFiles();
								return popNext();
							}							
						} else if (!files.isEmpty()) {
							currentTimestamp = files.get(0).earliestTimestamp;
							openFiles();
							return popNext();
						}

						return dp;
					}

					@Override
					public Input next() {
						if (!hasNext())
							throw new NoSuchElementException();
						DataPointer next = nextElement;
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
    		long min=currentTarget.earliestTimestamp;
        	for (TargetFile target : writtenFiles) {
    			min = Math.min(target.earliestTimestamp, min);
	    	}
    		return new Date(min);
        }
	}

	public Date getMaxDate() {
        synchronized (lock) {
    		long max=currentTarget.latestTimestamp;
        	for (TargetFile target : writtenFiles) {
    			max = Math.max(target.latestTimestamp, max);
	    	}
    		return new Date(max);
        }
	}

    
}
