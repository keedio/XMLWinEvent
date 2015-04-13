package org.keedio.watchdir;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.flume.Channel;
import org.apache.flume.ChannelSelector;
import org.apache.flume.Context;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.channel.ReplicatingChannelSelector;
import org.apache.flume.conf.Configurables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.keedio.watchdir.WatchDirEvent;
import org.keedio.watchdir.WatchDirObserver;
import org.keedio.watchdir.listener.FakeListener;
import org.keedio.watchdir.listener.WatchDirXMLWinEventSourceListener;
import org.mockito.Mock;
import org.mockito.Spy;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class WatchDirXMLWinEventSourceListenerTest {

	
	WatchDirXMLWinEventSourceListener listener;
	File tstFolder1;
	File tstFolder2;
	File tstFolder3;

	@Mock
	FakeListener mock = mock(FakeListener.class);
	
	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder(new File(System.getProperty("java.io.tmpdir")));
	
	@Before
	public void setUp() throws IOException{
        tstFolder1 = testFolder.newFolder("/tmp1/");
        tstFolder2 = testFolder.newFolder("/tmp2/");
        tstFolder3 = testFolder.newFolder("/tmp3/");
		
		listener = new WatchDirXMLWinEventSourceListener();
		
		Channel channel = new MemoryChannel();
		Context context = new Context();
		context.put("dirs", tstFolder1.getAbsolutePath() + "," + tstFolder2.getAbsolutePath());
		context.put("event.terminator", "|#]");
		context.put("keep-alive", "1");
		context.put("capacity", "100000");
		context.put("transactionCapacity", "100000");

		Configurables.configure(listener, context);
		Configurables.configure(channel, context);
		
		ChannelSelector rcs = new ReplicatingChannelSelector();
		rcs.setChannels(Lists.newArrayList(channel));

		listener.setChannelProcessor(new ChannelProcessor(rcs));
		
		listener.configure(context);
		
		listener.start();;
		
	}
	
	@After
	public void finish() {
		listener.stop();
	}
	
	@Test
	public void testOnGoing() {
		System.out.println();
		Assert.assertTrue("El hilo esta corriendo", "START".equals(listener.getLifecycleState().toString()));
	}
	
	@Test
	public void testFileModified() {
		
		try {
			// Registramos el FakeListener en todos los monitores
			for (WatchDirObserver observer: listener.getMonitor()) {
				observer.addWatchDirListener(mock);
			}
			

            // Creamos el fichero en el directorio 1
        	FileUtils.copyFile(new File("src/test/resources/test.xml"), testFolder.newFile("tmp1/test.xml"));

            Thread.sleep(20000);
            verify(mock, times(1)).process(any(WatchDirEvent.class));


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        }
		
	}
	
	@Test
	public void testFileModifiedNotObserved() {
		
		try {
			// Registramos el FakeListener en todos los monitores
			for (WatchDirObserver observer: listener.getMonitor()) {
				observer.addWatchDirListener(mock);
			}
			

            // Creamos el fichero en el directorio 1
        	FileUtils.copyFile(new File("src/test/resources/test.xml"), testFolder.newFile("tmp3/test.xml"));

            Thread.sleep(20000);
            verify(mock, times(0)).process(any(WatchDirEvent.class));


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        }
		
	}

	@Test
	public void testFileModifiedTwoDirectories() {
		
		try {
			// Registramos el FakeListener en todos los monitores
			for (WatchDirObserver observer: listener.getMonitor()) {
				observer.addWatchDirListener(mock);
			}
			

            // Creamos el fichero en el directorio 1
        	FileUtils.copyFile(new File("src/test/resources/test.xml"), testFolder.newFile("tmp1/test.xml"));
        	FileUtils.copyFile(new File("src/test/resources/test.xml"), testFolder.newFile("tmp2/test.xml"));
        	FileUtils.copyFile(new File("src/test/resources/test.xml"), testFolder.newFile("tmp3/test.xml"));

            Thread.sleep(20000);
            verify(mock, times(2)).process(any(WatchDirEvent.class));


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        }
		
	}

}
