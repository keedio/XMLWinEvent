package org.keedio.watchdir;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * 
 * This thread monitors the directory indicated in the constructor recursively. 
 * At the time believed a new file to all 
 * registered listeners are notified. 
 * <p>
 * Events deleted or modified files are not managed.
 *
 */
public class WatchDirObserver implements Runnable {

	private WatchService watcherSvc;
	private List<WatchDirListener> listeners;
	private static final Logger LOGGER= LoggerFactory
			.getLogger(WatchDirObserver.class);
	private final Map<WatchKey, Path> keys;
	private String blacklist = "";
	private String whitelist = "";
	
    public WatchDirObserver (String dir, String whitelist, String blacklist) {
    	
    	this.blacklist = blacklist;
    	this.whitelist = whitelist;
    	keys = new HashMap<WatchKey, Path>();
    	listeners = new ArrayList<WatchDirListener>();
    	
		try {
			Path directotyToWatch = Paths.get(dir);
	        watcherSvc = FileSystems.getDefault().newWatchService();
	        registerAll(directotyToWatch);

		} catch (IOException e){
			LOGGER.info("No se puede monitorizar el directorio: " + dir, e);
		}
    	
    }

    static <T> WatchEvent<T> castEvent(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
    /**
     * Method used to record listeners. There must be at least one.
     * @param listener	Must implement WhatchDirListerner. See listeners implementations for more information
     */
    public void addWatchDirListener(WatchDirListener listener) {
    	listeners.add(listener);
    }
    
    private void update(WatchDirEvent event) {
    	for (WatchDirListener listener:listeners) {
    		try{
        		listener.process(event);
    		} catch (WatchDirException e) {
    			LOGGER.info("Error procesando el listener", e);
    		}
    	}
    }

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

    /**
     * Register the given directory, and all its sub-directories, with the WatchService.
     * @param  start	The initial path to monitor. 
     * 
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
                    register(dir);
                    return FileVisitResult.CONTINUE;
            }
        });
    }    
    
	private void register(Path dir) throws IOException {

		LOGGER.trace("WatchDir: register");
		
		// Solo nos preocupamos por los ficheros de nueva creacion
		WatchKey key = dir.register(watcherSvc, ENTRY_CREATE);
		Path prev = keys.get(key);

		LOGGER.info("Previous directory: " + prev);
		if (prev == null) {
			LOGGER.info("Registering directory: " + dir);
		} else {
			if (!dir.equals(prev)) {
				LOGGER.info("Updating previous directory: " + "-> " + prev + " to " + dir);
			}
		}

		keys.put(key, dir);

	}

    
    @Override
	public void run() {
    	
    	if (listeners.isEmpty()) {
    		LOGGER.error("No existen listeners. Finalizando");
    	} else {
    		try {
    			boolean fin = false;
    			
    			while (!fin) {
    				// wait for key to be signaled
    				WatchKey key;
    				key = watcherSvc.take();
    				Path dir = keys.get(key);

    				for (WatchEvent<?> event : key.pollEvents()) {
        				WatchEvent<Path> ev = cast(event);
    					Path name = ev.context();
    					Path path = dir.resolve(name);
    					
    					// Si se crea un nuevo directorio es necesario registrarlo de nuevo
    					if (Files.isDirectory(path, NOFOLLOW_LINKS))
    						registerAll(path);
    					else {
    						if (blacklist.isEmpty() && whitelist.isEmpty()){
    							// Si las dos listas estan vacias notificamos
        						update(new WatchDirEvent(path.toString(), event.kind().name()));    							
    						} else {
    							// En caso contrario
        						// Comprobamos si esta en la blacklist
        						if (!whitelist.isEmpty() && match(whitelist, path.toString())){
        							LOGGER.debug("Whitelisted. Go on");
        							update(new WatchDirEvent(path.toString(), event.kind().name()));
        							break;
        						} else if (!blacklist.isEmpty() && !match(blacklist, path.toString())) {
        							LOGGER.debug("Not in blacklisted. Go on");
        							update(new WatchDirEvent(path.toString(), event.kind().name()));
        							break;
        						}
    						}
    					}
    					
    				}

    				// reset key and remove from set if directory no longer
    				// accessible
    				key.reset();

    				Thread.sleep(1000);
    			}
    		} catch (InterruptedException e) {
    			LOGGER.info(e.getMessage(), e);
    		} catch (Exception e) {
    			LOGGER.info(e.getMessage(), e);
    		}
    	}
	}
    
    public static boolean match(String patterns, String string) {
    	
    	String[] splitPat = patterns.split(",");
    	boolean match = false;
    	
    	for (String pattern:splitPat) {
        	Pattern pat = Pattern.compile(pattern + "$");
        	Matcher mat = pat.matcher(string);
        	
        	match = match || mat.find();
        	
        	if (match) break;
    	}
    	
    	
    	return match;
    }

}
