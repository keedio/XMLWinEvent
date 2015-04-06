package org.keedio.watchdir.listener;

import java.io.StringWriter;
import java.util.Date;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import org.apache.flume.source.AbstractSource;
import org.keedio.watchdir.WatchDirEvent;
import org.keedio.watchdir.WatchDirException;
import org.keedio.watchdir.WatchDirListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLWinEventListener implements WatchDirListener{
	
	private AbstractSource flumeSource = null;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(XMLWinEventListener.class);

	public AbstractSource getFlumeSource() {
		return flumeSource;
	}

	public void setFlumeSource(AbstractSource flumeSource) {
		this.flumeSource = flumeSource;
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

	@Override
	public void process(WatchDirEvent event) throws WatchDirException {
		
		// Si no esta instanciado el source informamos
		switch(event.getType()) {
		
			case "ENTRY_CREATE":
				entryCreate(event);	
				break;
		}
			
	}

	private String[] entryCreate(WatchDirEvent event) throws WatchDirException {
		String all[] = {};

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
			            all[procesados++] = xmlFragment;
			            
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
		
		return all;
	}

}
