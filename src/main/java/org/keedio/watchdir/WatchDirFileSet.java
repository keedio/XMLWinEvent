package org.keedio.watchdir;

/**
 * Thos class include characteristics of the file to be monitorized:
 * Path, path of the directory 
 * Tag name, name of tag expected to be in the xml files
 * Tag level, level of tag expected to be in the xml files
 * Whitelist, files to monitorize
 * Blacklist, excluded files
 * 
 * @author rolmo
 *
 */
public class WatchDirFileSet {
	
	private String path;
	private String tagName;
	private int tagLevel;
	private String whitelist;
	private String blacklist;

	public WatchDirFileSet(String path, String tagName, int tagLevel,
			String whitelist, String blacklist) {
		super();
		this.path = path==null?"":path;
		this.tagName = tagName==null?"":tagName;
		this.tagLevel = tagLevel;
		this.whitelist = whitelist==null?"":whitelist;
		this.blacklist = blacklist==null?"":blacklist;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public int getTagLevel() {
		return tagLevel;
	}
	public void setTagLevel(int tagLevel) {
		this.tagLevel = tagLevel;
	}
	public String getWhitelist() {
		return whitelist;
	}
	public void setWhitelist(String whitelist) {
		this.whitelist = whitelist;
	}
	public String getBlacklist() {
		return blacklist;
	}
	public void setBlacklist(String blacklist) {
		this.blacklist = blacklist;
	}
	
	

}
