package org.keedio.watchdir;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.WatchEvent;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.keedio.watchdir.listener.FakeListener;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import static org.mockito.Mockito.*;

public class WatchDirTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();
		
	@Before
	public void setUp() {
		
	}
	
	@Test
	public void test() {

		FakeListener listener = mock(FakeListener.class);
		//doReturn(22).when(listener).process(any(WatchEvent.class));
		
		try {
			// Si no existen listeners sale del hilo
			File tstFolder = testFolder.newFolder("/tempFolder/");
			String[] dirs = {tstFolder.getAbsolutePath()};
			WatchDirObserver monitor = new WatchDirObserver(dirs);
			monitor.addWatchDirListener(listener);
			Thread t = new Thread(monitor);
			t.start();
			
			// Creamos el fichero
			testFolder.newFile("tempFolder/dd.dat");
			
			waitFor(20);
			verify(listener, atLeastOnce()).process(any(WatchDirEvent.class));
			
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test2() {

		FakeListener listener = mock(FakeListener.class);
		//doReturn(22).when(listener).process(any(WatchEvent.class));
		
		try {
			// Si no existen listeners sale del hilo
			File tstFolder = testFolder.newFolder("/tempFolder/");
			String[] dirs = {tstFolder.getAbsolutePath()};
			WatchDirObserver monitor = new WatchDirObserver(dirs);
			Thread t = new Thread(monitor);
			t.start();
						
			waitFor(2);
			
			Assert.assertTrue(!t.isAlive());;
			
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test3() {

		FakeListener listener = mock(FakeListener.class);
		//doReturn(22).when(listener).process(any(WatchEvent.class));
		
		try {
			// Si no existen listeners sale del hilo
			File tstFolder1 = testFolder.newFolder("/tempFolder1/");
			File tstFolder2 = testFolder.newFolder("/tempFolder2/");
			String[] dirs = {tstFolder1.getAbsolutePath(), tstFolder2.getAbsolutePath()};
			WatchDirObserver monitor = new WatchDirObserver(dirs);
			monitor.addWatchDirListener(listener);
			Thread t = new Thread(monitor);
			t.start();
			
			// Creamos el fichero en el directorio 1 
			testFolder.newFile("tempFolder2/dd.dat");
			
			waitFor(20);
			verify(listener, times(1)).process(any(WatchDirEvent.class));
			
			// Creamos el fichero en el directorio 2 
			testFolder.newFile("tempFolder1/dd.dat");
			
			waitFor(20);
			verify(listener, times(1)).process(any(WatchDirEvent.class));

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} catch (Throwable e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	private static void waitFor(int seg) throws InterruptedException {
		for (int i=1;i<=seg;i++) {
			System.out.println("... wating " + (seg - i) + " seconds");
			Thread.sleep(1000);
		}
	}
	
}
