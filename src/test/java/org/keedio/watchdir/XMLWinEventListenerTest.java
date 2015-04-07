package org.keedio.watchdir;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.WatchEvent;

import org.apache.flume.Event;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.source.AbstractSource;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.keedio.watchdir.listener.FakeListener;
import org.keedio.watchdir.listener.XMLWinEventListener;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.*;

public class XMLWinEventListenerTest {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
	    System.setOut(null);
	    System.setErr(null);
	}
	

	@Test
	public void test1() {
		// Test parseo de fichero creado correctamente
		WatchDirEvent eventFake = mock(WatchDirEvent.class);
		doReturn("src/test/resources/test.xml").when(eventFake).getPath();
		doReturn("ENTRY_CREATE").when(eventFake).getType();
		
		ChannelProcessor processor = mock(ChannelProcessor.class);
		doNothing().when(processor).processEvent(any(Event.class));
		
		AbstractSource source = mock(AbstractSource.class);
		when(source.getChannelProcessor()).thenReturn(processor);
		
		
		
		try {
			// Si no existen listeners sale del hilo
			XMLWinEventListener sample = new XMLWinEventListener();
			sample.process(eventFake);
			
			// Comprobamos 35661 eventos
			Assert.assertTrue(outContent.toString().contains("35661"));
			
		} catch (WatchDirException e) { 
			Assert.fail();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} 
	}

	@Test
	public void test2() {
		// Test parseo de fichero creado correctamente
		WatchDirEvent eventFake = mock(WatchDirEvent.class);
		doReturn("src/test/resources/test2.xml").when(eventFake).getPath();
		doReturn("ENTRY_CREATE").when(eventFake).getType();
		
		ChannelProcessor processor = mock(ChannelProcessor.class);
		doNothing().when(processor).processEvent(any(Event.class));
		
		AbstractSource source = mock(AbstractSource.class);
		when(source.getChannelProcessor()).thenReturn(processor);
		
		
		
		try {
			// Si no existen listeners sale del hilo
			XMLWinEventListener sample = new XMLWinEventListener();
			sample.process(eventFake);
			
			// Si llegamos aqui el test esta mal
			Assert.fail();
			
		} catch (WatchDirException e) { 
			// El fichero esta mal formado
			Assert.assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} 
	}

}
