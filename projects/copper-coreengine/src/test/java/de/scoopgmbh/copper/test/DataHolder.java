package de.scoopgmbh.copper.test;

import java.util.HashMap;
import java.util.Map;

public class DataHolder {
	
	private final Map<String, Object> map = new HashMap<String, Object>();
	
	public void clear(String id) {
		synchronized (map) {
			map.remove(id);
		}
	}
	
	public void put(String id, Object data) {
		synchronized (map) {
			map.put(id,data);
		}
	}
	
	public Object get(String id) {
		synchronized (map) {
			return map.get(id);
		}
	}
}
