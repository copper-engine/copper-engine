package de.scoopgmbh.copper.monitoring.core.data;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.esotericsoftware.kryo.io.Input;

public class MonitoringDataStorageTest {

	static final String filename = "test"; 
	
	@Test
	public void testSimpleCase() throws IOException {
		File tmpDir = File.createTempFile("montoringdatastoragetest", ".tmp");
		tmpDir.delete();
		tmpDir.mkdirs();
		File f1 = new File(tmpDir, filename+".1");
		f1.deleteOnExit();
		writeFile(f1, new Date(1), new Date(2));
		File f2 = new File(tmpDir, filename+".2");
		f2.deleteOnExit();
		writeFile(f2, new Date(3), new Date(4));
		File f3 = new File(tmpDir, filename+".3");
		f3.deleteOnExit();
		writeFile(f3, new Date(5), new Date(6));
		MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename);
		int i = 1;
		for (Input in : storage.read(new Date(1), new Date(6))) {
			byte[] data = new byte[8];
			in.read(data);
			for (int j = 0; j < 7; ++j) {
				Assert.assertEquals(data[j], 0);
			}
			Assert.assertEquals(i, data[7]);
			i += 1;
		}
		Assert.assertEquals(7, i);
		
	}

	@Test
	public void testSortedRead() throws IOException {
		File tmpDir = File.createTempFile("montoringdatastoragetest", ".tmp");
		tmpDir.delete();
		tmpDir.mkdirs();
		File f1 = new File(tmpDir, filename+".1");
		f1.deleteOnExit();
		writeFile(f1, new Date(1), new Date(6));
		File f2 = new File(tmpDir, filename+".2");
		f2.deleteOnExit();
		writeFile(f2, new Date(2), new Date(5));
		File f3 = new File(tmpDir, filename+".3");
		f3.deleteOnExit();
		writeFile(f3, new Date(3), new Date(4));
		MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename);
		int i = 1;
		for (Input in : storage.read(new Date(1), new Date(6))) {
			byte[] data = new byte[8];
			in.read(data);
			for (int j = 0; j < 7; ++j) {
				Assert.assertEquals(data[j], 0);
			}
			Assert.assertEquals(i, data[7]);
			i += 1;
		}
		Assert.assertEquals(7, i);
		
	}


	@Test
	public void testHouseKeeping() throws Exception {
		File tmpDir = File.createTempFile("montoringdatastoragetest", ".tmp");
		tmpDir.delete();
		tmpDir.mkdirs();
		MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename,9000000,new Date(0));
		storage.write(new byte[MonitoringDataStorage.FILE_CHUNK_SIZE-1000]);
		Thread.sleep(100);
		System.gc();
		Runtime.getRuntime().runFinalization();
		storage.write(new byte[MonitoringDataStorage.FILE_CHUNK_SIZE-1000]);
		Thread.sleep(100);
		System.gc();
		Runtime.getRuntime().runFinalization();
		storage.write(new byte[MonitoringDataStorage.FILE_CHUNK_SIZE-1000]);
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
		File tmpDir = File.createTempFile("montoringdatastoragetest", ".tmp");
		tmpDir.delete();
		tmpDir.mkdirs();
		MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename,Long.MAX_VALUE,new Date(10));
		storage.write(new Date(9), new byte[MonitoringDataStorage.FILE_CHUNK_SIZE-1000]);
		Thread.sleep(100);
		System.gc();
		Runtime.getRuntime().runFinalization();
		storage.write(new Date(11), new byte[MonitoringDataStorage.FILE_CHUNK_SIZE-1000]);
		Thread.sleep(100);
		System.gc();
		Runtime.getRuntime().runFinalization();
		storage.write(new Date(12), new byte[MonitoringDataStorage.FILE_CHUNK_SIZE-1000]);
		Thread.sleep(100);
		System.gc();
		Runtime.getRuntime().runFinalization();
		storage.write(new Date(13), new byte[MonitoringDataStorage.FILE_CHUNK_SIZE-1000]);
		Thread.sleep(100);
		System.gc();
		Runtime.getRuntime().runFinalization();
		storage.write(new Date(14), new byte[MonitoringDataStorage.FILE_CHUNK_SIZE-1000]);
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
		File tmpDir = File.createTempFile("montoringdatastoragetest", ".tmp");
		tmpDir.delete();
		tmpDir.mkdirs();
		long d = System.currentTimeMillis();
		for (int i = 0; i < 127; ++i) {
			MonitoringDataStorage storage = new MonitoringDataStorage(tmpDir, filename);
			storage.write(new Date(d++),new byte[]{(byte)i});
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
		for (Input in : storage.read(null,null)) {
			Assert.assertEquals(i++, in.readByte());
		}
		Assert.assertEquals(127, i);
		storage.close();
		
	}

    private void writeFile(File f, Date ... dates) throws IOException {
		RandomAccessFile ranAccess = new RandomAccessFile(f,"rw");
		MappedByteBuffer b = ranAccess.getChannel().map(MapMode.READ_WRITE, 0, 1024);
		long earliest = Long.MAX_VALUE;
		long latest = Long.MIN_VALUE;
		for (Date d : dates) {
			earliest = Math.min(d.getTime(), earliest);
			latest = Math.max(d.getTime(), latest);
		}
		b.putLong(MonitoringDataStorage.EARLIEST_POSITION, earliest);
		b.putLong(MonitoringDataStorage.LATEST_POSITION, latest);
		b.position(MonitoringDataStorage.FIRST_RECORD_POSITION);
		for (Date d : dates) {
			b.putInt(8);
			b.putLong(d.getTime());
			b.putLong(d.getTime());
		}
		b.putInt(MonitoringDataStorage.LIMIT_POSITION, b.position());
//		b.force();
		b.position(0);
		ranAccess.close();
	}
	
	
}
