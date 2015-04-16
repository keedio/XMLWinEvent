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

import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
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
import org.keedio.watchdir.WatchDirFileSet;
import org.keedio.watchdir.WatchDirListener;
import org.keedio.watchdir.WatchDirObserver;
import org.keedio.watchdir.metrics.MetricsController;
import org.keedio.watchdir.metrics.MetricsEvent;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * 
 * Implementation of a source of flume that consumes XML files that follow 
 * the standard architecture for monitoring events microsoft (WMI standard). 
 * <p>
 * The different events of the created file (block <Event> </ Event>) are extracted and 
 * sent to the corresponding channel.
 *
 */
public class WatchDirXMLWinEventSourceListener extends AbstractSource implements
		Configurable, EventDrivenSource, WatchDirListener {

	private static final String CONFIG_DIRS = "dirs.";
	private static final String DIR = "dir";
	private static final String WHITELIST = "whitelist";
	private static final String BLACKLIST = "blacklist";
	private static final String TAGNAME = "tag";
	private static final String TAGLEVEL = "taglevel";
	private static final String MAX_WORKERS = "maxworkers";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(WatchDirXMLWinEventSourceListener.class);
	private ExecutorService executor;
	private String confDirs;
	private String[] dirs;
	private Set<WatchDirObserver> monitor; 
	private MetricsController metricsController;
	private Set<WatchDirFileSet> fileSets;
	private int maxWorkers = 10;
	
	public synchronized MetricsController getMetricsController() {
		return metricsController;
	}

	public Set<WatchDirObserver> getMonitor() {
		return monitor;
	}

	public void setMonitor(Set<WatchDirObserver> monitor) {
		this.monitor = monitor;
	}

	@Override
	public void configure(Context context) {
		LOGGER.info("Source Configuring..");

		metricsController = new MetricsController();
		
		Map<String, String> criterias = context.getSubProperties(CONFIG_DIRS);
		Map getCriterias = getMapProperties(criterias);
		
		String globalWhiteList = context.getString(WHITELIST);
		String globalBlackList = context.getString(BLACKLIST);
		maxWorkers = context.getString(MAX_WORKERS)==null?10:Integer.parseInt(context.getString(MAX_WORKERS));
		
		// Creamos los filesets
		fileSets = new HashSet<WatchDirFileSet>();
		Iterator it = getCriterias.keySet().iterator();
		while (it.hasNext()) {
			Map<String, String> aux = (Map<String, String>)getCriterias.get(it.next());
			WatchDirFileSet auxSet = new WatchDirFileSet(aux.get(DIR), aux.get(TAGNAME), Integer.parseInt(aux.get(TAGLEVEL)), globalWhiteList!=null?globalWhiteList:aux.get(WHITELIST), globalBlackList!=null?globalBlackList:aux.get(BLACKLIST));
			
			fileSets.add(auxSet);
		}

		Preconditions.checkState(fileSets.size() > 0, "Bad configuration, review documentation on https://github.com/keedio/XMLWinEvent/blob/master/README.md");	

	}
	
	public static Map<String, Map<String, String>> getMapProperties(Map<String, String> all) {
		
		Map<String, Map<String, String>> map = new HashMap<String, Map<String,String>>();
		Iterator<String> it = all.keySet().iterator();
		
		while(it.hasNext()){
			String key = it.next();
			String[]aux = key.split("\\.");
			String mapKey = aux[0];
			String auxKey = aux[1];
			String auxValue = all.get(key);
			
			if (!map.containsKey(mapKey)) {
				HashMap<String, String> auxMap = new HashMap<String, String>();
				auxMap.put(auxKey, auxValue);
				
				map.put(mapKey, auxMap);
			} else {
				map.get(mapKey).put(auxKey, auxValue);
			}
					
		}
		
		return map;
	}

	@Override
	public void start() {
		LOGGER.info("Source Starting..");
		executor = Executors.newFixedThreadPool(maxWorkers);
		monitor = new HashSet<WatchDirObserver>();
		
		try {
			Iterator<WatchDirFileSet> it = fileSets.iterator();
			
			while(it.hasNext()) {
//				WatchDirObserver aux = new WatchDirObserver(dirs[i], whitelist, blacklist);
				WatchDirObserver aux = new WatchDirObserver(it.next());
				aux.addWatchDirListener(this);

				Log.debug("Lanzamos el proceso");
				new Thread(aux).start();

				monitor.add(aux);
			}
			metricsController.start();
			
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}

		super.start();
	}

	@Override
	public void stop() {
		LOGGER.info("Stopping source");
		executor.shutdown();
		metricsController.stop();
		super.stop();
	}
	
	@Override
	public void process(WatchDirEvent event) throws WatchDirException {

		// Si no esta instanciado el source informamos
		switch(event.getType()) {
		
			case "ENTRY_CREATE":
				// Notificamos nuevo fichero creado
				metricsController.manage(new MetricsEvent(MetricsEvent.NEW_FILE));
				executor.submit(new WatchDirXMLWinWorker(this, event));
				break;
			default:
				LOGGER.info("El evento " + event.getPath() + " no se trata.");
				break;
		}
	}
	
}
