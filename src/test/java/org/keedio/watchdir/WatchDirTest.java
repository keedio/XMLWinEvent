package org.keedio.watchdir;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.WatchEvent;

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
			
			Thread.sleep(20000);
			verify(listener, atLeastOnce()).process(any(WatchDirEvent.class));
			
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
