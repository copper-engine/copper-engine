package de.scoopgmbh.copper.monitoring.core.data;

import java.io.File;
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
		System.out.println(b.position());
		for (Date d : dates) {
			b.putInt(8);
			b.putLong(d.getTime());
			b.putLong(d.getTime());
		}
		b.putInt(MonitoringDataStorage.LIMIT_POSITION, b.position());
//		b.force();
		b.position(0);
		System.out.println(b.getInt()+":"+b.getLong()+":"+b.getLong());
		ranAccess.close();
	}
	
	
}
