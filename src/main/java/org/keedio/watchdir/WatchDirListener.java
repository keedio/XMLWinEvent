package org.keedio.watchdir;

public interface WatchDirListener {
	
	public void process(WatchDirEvent event) throws WatchDirException;

}
