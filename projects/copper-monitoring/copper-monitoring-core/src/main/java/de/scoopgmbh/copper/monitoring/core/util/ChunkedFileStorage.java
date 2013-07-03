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
package de.scoopgmbh.copper.monitoring.core.util;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;

public class ChunkedFileStorage {

	static final int FILE_CHUNK_SIZE=4*1024*1024;
	static final int LIMIT_POSITION = 0;
	static final int EARLIEST_POSITION = LIMIT_POSITION+4;
	static final int LATEST_POSITION = EARLIEST_POSITION+8;
	static final int FIRST_RECORD_POSITION = LATEST_POSITION+8;

	final File target;
	      TargetFile currentTarget;
	final ArrayList<TargetFile> writtenFiles = new ArrayList<TargetFile>();
	long lastTimeStamp = 0;
	final Object lock = new Object();
	final ArrayBlockingQueue<TargetFile> buffersToForce = new ArrayBlockingQueue<TargetFile>(16,false);
	boolean closed = false;
	final ForceThread forceThread;
	
	class ForceThread extends Thread {
		
		ForceThread() {
			super("File forcer thread for '"+target.getAbsolutePath()+"'");
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
							while ((f = buffersToForce.poll()) != null)
								f.out.force();
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
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			close();
		}
		public void close() throws IOException {
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
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
	
	public ChunkedFileStorage(File target) {
		this.target = target;
		(forceThread = new ForceThread()).start();
	}
	
	void ensureCurrentFile(int additionalBytes) throws IOException {
		if (currentTarget == null) {
			currentTarget = createTargetFile();
		}
		if (currentTarget.out.position() + additionalBytes > FILE_CHUNK_SIZE) {
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
			newTarget.file = new File(target.getParentFile(), target.getName()+"."+currentTimeStamp);
			if (newTarget.file.exists()) {
				++currentTimeStamp;
				continue;
			}
		} while (false);
			
		newTarget.memoryMappedFile = new RandomAccessFile(newTarget.file, "rw");
		newTarget.out = newTarget.memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, FILE_CHUNK_SIZE);
		newTarget.out.putLong(LIMIT_POSITION,newTarget.limit);
		newTarget.out.putLong(EARLIEST_POSITION,newTarget.earliestTimestamp);
		newTarget.out.putLong(LATEST_POSITION,newTarget.latestTimestamp);
		newTarget.out.position(FIRST_RECORD_POSITION);
		return newTarget;
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

    byte[] intBytes = new byte[4]; 
    byte[] longBytes = new byte[8]; 
    public void write(Date referenceDate, byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
    	long referenceMillis = referenceDate.getTime();
        synchronized (lock) {
        	if (closed)
        		throw new ClosedChannelException();
            ensureCurrentFile(len+4);
            if (currentTarget.earliestTimestamp > referenceMillis) {
            	currentTarget.earliestTimestamp = referenceMillis;
	            currentTarget.out.putLong(EARLIEST_POSITION,referenceMillis);
            }
            if (currentTarget.latestTimestamp < referenceMillis) {
            	currentTarget.latestTimestamp = referenceMillis;
	            currentTarget.out.putLong(LATEST_POSITION,referenceMillis);
            }
            currentTarget.out.putInt(b.length);
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
    
    public Iterable<byte[]> read(Date fromDate, Date toDate) {
    	final ArrayList<TargetFile> filesToRead = new ArrayList<TargetFile>();
    	long fromTime = fromDate == null?Long.MIN_VALUE:fromDate.getTime();
    	long toTime = toDate == null?Long.MAX_VALUE:toDate.getTime();
        synchronized (lock) {
        	for (TargetFile target : writtenFiles) {
	    		if ((fromTime <= target.latestTimestamp || toTime >= target.earliestTimestamp) && target.limit > FIRST_RECORD_POSITION)
	    			filesToRead.add(target);
	    	}
        	if (currentTarget != null && (fromTime <= currentTarget.latestTimestamp || toTime >= currentTarget.earliestTimestamp) && currentTarget.limit > FIRST_RECORD_POSITION)
        		filesToRead.add(currentTarget);
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
    	
    	return new Iterable<byte[]>() {
			
			@Override
			public Iterator<byte[]> iterator() {
				return new Iterator<byte[]>() {
					
					int currentLimit;
					@SuppressWarnings("unchecked")
					ArrayList<TargetFile> files = (ArrayList<TargetFile>)filesToRead.clone();
					RandomAccessFile currentFile;
					MappedByteBuffer byteBuffer;
					
					boolean popFile() {
						currentFile = null;
						if (files.isEmpty()) {
							return false;
						}
						File f = files.remove(0).file;
						try {
							currentFile = new RandomAccessFile(f, "r");
							byteBuffer = currentFile.getChannel().map(MapMode.READ_ONLY, 0, f.length());
							currentLimit = byteBuffer.getInt(0);
							byteBuffer.position(FIRST_RECORD_POSITION);
							return true;
						} catch (IOException fe) {
							return popFile();
						}
					}
					
					boolean ensureNextRecord() {
						if (currentFile != null && byteBuffer.position() >= currentLimit)
							closeCurrentFile();
						if (currentFile == null)
							if (!popFile()) return false;
						return true;
					}

					void closeCurrentFile()  {
						try {
							currentFile.close();
						} catch (IOException ex) {
							//discard
						}
						byteBuffer = null;
						currentFile = null;
					}

					@Override
					public boolean hasNext() {
						return ensureNextRecord();
					}

					@Override
					public byte[] next() {
						if (!ensureNextRecord())
							throw new NoSuchElementException();
						int len = byteBuffer.getInt();
						byte[] ret = new byte[len];
						byteBuffer.get(ret);
						ensureNextRecord();
						return ret;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					@Override
					protected void finalize() throws Throwable {
						super.finalize();
						if (currentFile != null)
							closeCurrentFile();
					}
				};
			}
		};
    	
    }

    
}
