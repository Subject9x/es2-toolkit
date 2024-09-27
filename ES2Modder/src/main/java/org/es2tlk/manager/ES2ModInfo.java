package org.es2tlk.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ES2ModInfo {

	public static String projName = "name";
	public static String projDir = "dir";
	public static String projAuthor = "author";
	public static String vols = "active";
	
	private HashMap<String, String> properties = new HashMap<String, String>();
	
	private List<String> activeVols = new ArrayList<String>();
	
	public ES2ModInfo() {}

	public String getProperty(String key) {
		return this.properties.get(key);
	}
	
	public void setProperty(String key, String val) {
		this.properties.put(key, val);
	}
	
	public HashMap<String, String> getProperties() {
		return properties;
	}

	public void setProperties(HashMap<String, String> properties) {
		this.properties = properties;
	}
}
