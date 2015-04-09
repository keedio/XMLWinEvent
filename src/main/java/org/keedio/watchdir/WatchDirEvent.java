package org.keedio.watchdir;

/**
 * 
 * This event includes the path of the file created.
 *
 */
public class WatchDirEvent {

	private String type;
	private String path;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public WatchDirEvent(String path, String type) {
		this.type = type;
		this.path = path;
	}
	
}
