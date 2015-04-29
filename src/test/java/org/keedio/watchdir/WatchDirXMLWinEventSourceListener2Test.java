package org.keedio.watchdir;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.flume.Channel;
import org.apache.flume.ChannelSelector;
import org.apache.flume.Context;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.channel.ReplicatingChannelSelector;
import org.apache.flume.conf.Configurables;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.keedio.watchdir.WatchDirEvent;
import org.keedio.watchdir.WatchDirObserver;
import org.keedio.watchdir.listener.FakeListener;
import org.keedio.watchdir.listener.WatchDirXMLWinEventSourceListener;
import org.mockito.Mock;
import org.mockito.Spy;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import static org.hamcrest.core.StringContains.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class WatchDirXMLWinEventSourceListener2Test {

	
	WatchDirXMLWinEventSourceListener listener;
	File tstFolder1;
	File tstFolder2;
	File tstFolder3;

	@Mock
	FakeListener mock = mock(FakeListener.class);
	
	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder(new File(System.getProperty("java.io.tmpdir")));

	@Rule
    public ExpectedException thrown= ExpectedException.none();
	
	@Before
	public void setUp() throws IOException{
        tstFolder1 = testFolder.newFolder("/tmp1/");
        tstFolder2 = testFolder.newFolder("/tmp2/");
        tstFolder3 = testFolder.newFolder("/tmp3/");
		
		// Creamos el fichero en el directorio 1
		FileUtils.copyFile(new File("src/test/resources/test.xml"), testFolder.newFile("tmp1/test.xml"));
		FileUtils.copyFile(new File("src/test/resources/test.xml"), testFolder.newFile("tmp1/test2.xml"));
		FileUtils.copyFile(new File("src/test/resources/test.xml"), testFolder.newFile("tmp3/test.xml"));

		listener = new WatchDirXMLWinEventSourceListener();
		
		Channel channel = new MemoryChannel();
		Context context = new Context();
		context.put("dirs.1.dir", tstFolder1.getAbsolutePath());
		context.put("dirs.1.tag", "event");
		context.put("dirs.1.taglevel", "2");
		context.put("dirs.2.dir", tstFolder2.getAbsolutePath());
		context.put("dirs.2.tag", "event");
		context.put("dirs.2.taglevel", "2");
		context.put("event.terminator", "|#]");
		context.put("keep-alive", "1");
		context.put("capacity", "100000");
		context.put("transactionCapacity", "100000");
		context.put("blacklist", "");
		context.put("whitelist", "");
		context.put("readonstartup", "true");
		context.put("suffix", ".completed");

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
	public void testExistingFiles() throws Exception {
		
			// Registramos el FakeListener en todos los monitores
			for (WatchDirObserver observer: listener.getMonitor()) {
				observer.addWatchDirListener(mock);
			}
			
			Thread.sleep(2000);
			            
            // Los ficheros .finished han tenido que ser generados.
            thrown.expectMessage(containsString("already exists in the test folder"));
            testFolder.newFile("tmp1/test.xml.completed").exists();
            testFolder.newFile("tmp1/test2.xml.completed").exists();


	}

	@Test
	public void testMappingConvertion() {
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("1.1", "1.1");
		map.put("1.2", "1.2");
		map.put("1.3", "1.3");
		map.put("1.4", "1.4");
		map.put("2.1", "2.1");
		map.put("2.2", "2.2");
		map.put("2.3", "2.3");
		map.put("2.4", "2.4");
		
		Map test = WatchDirXMLWinEventSourceListener.getMapProperties(map);
		
		Assert.assertTrue("La tabla tiene dos elementos", test.size() == 2);
		String str = ((Map<String, String>)test.get("2")).get("3");
		Assert.assertTrue("El elemento 2, 3", "2.3".equals(str));
		
	}
	

}
