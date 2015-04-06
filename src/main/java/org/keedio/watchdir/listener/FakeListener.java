package org.keedio.watchdir.listener;

import java.nio.file.WatchEvent;

import org.keedio.watchdir.WatchDirEvent;
import org.keedio.watchdir.WatchDirException;
import org.keedio.watchdir.WatchDirListener;

public class FakeListener implements WatchDirListener {

	@Override
	public void process(WatchDirEvent event) throws WatchDirException {
		// TODO Auto-generated method stub
		System.out.println("Got event: " + event.getPath() + " " + event.getType());
	}

}
