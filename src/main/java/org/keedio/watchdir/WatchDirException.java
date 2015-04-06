package org.keedio.watchdir;

public class WatchDirException extends Exception{

	private String exception;
	
	public WatchDirException(String message) {
		super(message);
	}
	
	public WatchDirException(String message, Throwable e) {
		super(message, e);
	}
	
}
