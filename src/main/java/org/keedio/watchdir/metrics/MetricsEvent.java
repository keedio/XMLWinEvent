package org.keedio.watchdir.metrics;

public class MetricsEvent {
	
	public final static int NEW_EVENT = 1;
	public final static int NEW_FILE = 2;
	public final static int MEAN_FILE_PROCESS = 3;
	public final static int TOTAL_FILE_EVENTS = 4;
	
	private int code;
	private long value = -1;
	
	public MetricsEvent(int code) {
		this.code = code;
	}
	
	public MetricsEvent(int code, long value) {
		this.code = code;
		this.value = value;
	}

	public int getCode() {
		return code;
	}
	
	public long getValue() {
		return this.value;
	}

}
