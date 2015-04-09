package org.keedio.watchdir;

public class WatchDirException extends Exception{
	
	public WatchDirException(String message) {
		super(message);
	}
	
	public WatchDirException(String message, Throwable e) {
		super(message, e);
	}
	
}
