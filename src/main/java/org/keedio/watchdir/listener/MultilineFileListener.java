package org.keedio.watchdir.listener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.text.StrBuilder;
import org.keedio.watchdir.WatchDirEvent;
import org.keedio.watchdir.WatchDirException;
import org.keedio.watchdir.WatchDirListener;

public class MultilineFileListener implements WatchDirListener{

	private Map<String, Long> filesOffset;
	
	
	public MultilineFileListener() {
		
		// TODO-->Buscamos el fichero serailizado con la tabla hash
		
		// TODO --> y si no existe lo creamos por primera vez
		filesOffset = new HashMap<String, Long>();
	}
	
	@Override
	public void process(WatchDirEvent event) throws WatchDirException {

		switch(event.getType()) {
		
			case "ENTRY_MODIFY":
				entryModified(event);
				break;
			case "ENTRY_CREATE":
				entryCreate(event);
				break;
			case "ENTRY_DELETE":
				entryDeleted(event);
				break;
		}
		
	}

	private void entryDeleted(WatchDirEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void entryModified(WatchDirEvent event) {
		// Leemos las lineas y se las escribimos
		String lines = readLines(event.getPath());
		
		System.out.println("Nuevas lineas:" + lines);
		
		// TODO --> Y serializamos de nuevo la tabla hash
	}

	private void entryCreate(WatchDirEvent event) {
		// Creamos el indice a 0
		filesOffset.put(event.getPath(), Long.valueOf(0));
		
		// Leemos las lineas y se las escribimos
		String lines = readLines(event.getPath());
		
		System.out.println("Nuevas lineas:" + lines);
		
	}

	private String readLines(String path) {
		
		try {
			BufferedReader buf = new BufferedReader(new FileReader(path));
			if (filesOffset.get(path) != null)
				buf.skip(filesOffset.get(path));
			else
				filesOffset.put(path, Long.valueOf(0));
			
			StringBuffer strBuf = new StringBuffer();
			String line;
			long aux = 0;
			
			// Leemos hasta el final
			while ((line = buf.readLine()) != null) {
				strBuf.append(line);
				aux += line.length();
			}
			
			// Modificamos el offset
			filesOffset.put(path, filesOffset.get(path) + Long.valueOf(aux));
			
			return strBuf.toString();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}

}
