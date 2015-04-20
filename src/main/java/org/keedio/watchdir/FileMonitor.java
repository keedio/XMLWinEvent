package org.keedio.watchdir;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileMonitor implements Runnable {

	private ConcurrentLinkedQueue<WatchDirEvent> queue;
	private WatchDirObserver observer;
	private static final Logger LOGGER= LoggerFactory
			.getLogger(FileMonitor.class);
	
	public FileMonitor(WatchDirObserver observer) {
		queue = new ConcurrentLinkedQueue<WatchDirEvent>();
		this.observer = observer;
	}

	
	public boolean addItem(WatchDirEvent event) {
		return queue.add(event);
	}
	
	@Override
	public void run() {
		
		while (true) {
			Iterator<WatchDirEvent> it = queue.iterator();
			while (it.hasNext()) {
				WatchDirEvent event = it.next();
				String path = event.getPath();
				File file = new File(path);
				
				if (isFileClosed(file)) {
					// Eliminamos el item de la cola y lanzamos la notificación
					queue.remove(path);
					observer.update(event);
				}
			}
		}
		
	}
	
	public static boolean isFileClosed(File file) {
		Process plsof;
		BufferedReader reader;
		
		try {
	        plsof = new ProcessBuilder(new String[]{"lsof", "|", "grep", file.getAbsolutePath()}).start();
	        reader = new BufferedReader(new InputStreamReader(plsof.getInputStream()));
	        String line;
	        while((line=reader.readLine())!=null) {
	            if(line.contains(file.getAbsolutePath())) {                            
	                reader.close();
	                plsof.destroy();
	                return false;
	            }
	        }
	    } catch(Exception ex) {
	    	LOGGER.error("Problemas en el comando lsof", ex);
	    }
	    return true;
	}

}
