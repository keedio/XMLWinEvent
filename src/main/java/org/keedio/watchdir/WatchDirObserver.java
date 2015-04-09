package org.keedio.watchdir;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keedio.watchdir.listener.FakeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;


public class WatchDirObserver implements Runnable {

	private WatchService watcherSvc;
	private List<WatchDirListener> listeners;
	private static final Logger LOGGER= LoggerFactory
			.getLogger(WatchDirObserver.class);
	private final Map<WatchKey, Path> keys;
	
    static <T> WatchEvent<T> castEvent(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
    public void addWatchDirListener(WatchDirListener listener) {
    	listeners.add(listener);
    }
    
    public void update(WatchDirEvent event) {
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

    public WatchDirObserver (String dir) {
    	
    	keys = new HashMap<WatchKey, Path>();
    	listeners = new ArrayList<WatchDirListener>();
    	
		try {
			Path _directotyToWatch = Paths.get(dir);
	        watcherSvc = FileSystems.getDefault().newWatchService();
	        registerAll(_directotyToWatch);

		} catch (IOException e){
			LOGGER.info("No se puede monitorizar el directorio: " + dir, e);
		}
    	
    }

    /**
     * Register the given directory, and all its sub-directories, with the WatchService.
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
    					else
    						// En caso contrario notificamos el cambio sobre el fichero
    						update(new WatchDirEvent(path.toString(), event.kind().name()));
    					
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

}
