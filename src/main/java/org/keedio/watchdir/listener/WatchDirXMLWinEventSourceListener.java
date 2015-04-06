/***************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ****************************************************************/
package org.keedio.watchdir.listener;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.keedio.watchdir.WatchDirEvent;
import org.keedio.watchdir.WatchDirException;
import org.keedio.watchdir.WatchDirListener;
import org.keedio.watchdir.WatchDirObserver;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;

public class WatchDirXMLWinEventSourceListener extends AbstractSource implements
		Configurable, EventDrivenSource, WatchDirListener {

	private static final String CONFIG_DIRS = "dirs";
	private static final String CONFIG_LISTENERS = "listeners";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WatchDirXMLWinEventSourceListener.class);
	private String confDirs;
	private String[] dirs;
	private WatchDirObserver monitor; 
	
	@Override
	public void configure(Context context) {
		LOGGER.info("Source Configuring..");

		confDirs = context.getString(CONFIG_DIRS).trim();
		Preconditions.checkState(confDirs != null,
				"Configuration must be specified directory(ies).");


		dirs = context.getString(CONFIG_DIRS).split(",");

		Preconditions.checkState(dirs.length > 0, CONFIG_DIRS
				+ " must be specified at least one.");		

	}

	@Override
	public void start() {
		LOGGER.info("Source Starting..");

		try {
			monitor = new WatchDirObserver(dirs);
			monitor.addWatchDirListener(this);
			
			Log.debug("Lanzamos el proceso");
			new Thread(monitor).start();
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}

		super.start();
	}

	@Override
	public void stop() {
		LOGGER.info("Stopping source");
		super.stop();
	}
	
	@Override
	public void process(WatchDirEvent event) throws WatchDirException {

		// Si no esta instanciado el source informamos
		switch(event.getType()) {
		
			case "ENTRY_CREATE":
				entryCreate(event);	
				break;
		}
	}
	
	private void entryCreate(WatchDirEvent event) throws WatchDirException {

		try {
			
			Date inicio = new Date();
			int procesados = 0;
			XMLInputFactory xif = XMLInputFactory.newInstance();
			StreamSource source = new StreamSource(event.getPath());
			XMLEventReader xev = xif.createXMLEventReader(source);
			
			while (xev.hasNext()) {
			    XMLEvent xmlEvent = xev.nextEvent();
			    if (xmlEvent.isStartElement()) {
			        StartElement elem = xmlEvent.asStartElement();
			        String name = elem.getName().getLocalPart();

			        if ("Event".equals(name)) {
			        	//StringBuffer buf = new StringBuffer();
			            String xmlFragment = readElementBody(xev);
			            // lanzamos el evento a la canal flume
			            procesados++;
			            
			    		Event ev = EventBuilder.withBody(String.valueOf(xmlFragment).getBytes());
			    		getChannelProcessor().processEvent(ev);
			            
			            
			        }
			    }			
			}
			
			long intervalo = (new Date().getTime() - inicio.getTime());
			// Se usa el system out para procesar los test de forma correcta
			System.out.println("Se han procesado " + procesados + " elementos en " + intervalo + " milisegundos");
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new WatchDirException("Could not process file " + event.getPath());
		}
	}

	public static String readElementBody(XMLEventReader eventReader)
			throws XMLStreamException {
		StringWriter buf = new StringWriter(1024);

		int depth = 0;
		while (eventReader.hasNext()) {
			// peek event
			XMLEvent xmlEvent = eventReader.peek();

			if (xmlEvent.isStartElement()) {
				++depth;
			}
			else if (xmlEvent.isEndElement()) {
				--depth;

				// reached END_ELEMENT tag?
						// break loop, leave event in stream
				if (depth < 0)
					break;
			}

			// consume event
			xmlEvent = eventReader.nextEvent();

			// print out event
			xmlEvent.writeAsEncodedUnicode(buf);
		}

		return buf.getBuffer().toString();
	}

}
