package org.es2tlk.dts.obj;

import java.util.LinkedHashMap;

public class Material {

	public static enum Keys{
		KEY_NEWMTL("newmtl"),
		KEY_AMBIENT("Ka"),
		KEY_DIFFUSE("Kd"),
		KEY_SPECULAR("Ks"),
		KEY_D_TRANS("d"),
		KEY_ILLUM("illum"),
		KEY_MAP_AMBIENT("map_Ka"),
		KEY_MAP_DIFFUSE("map_Kd"),
		KEY_MAP_SPECULAR("map_Ks"),
		KEY_MAP_BUMP("map_bump"),
		KEY_DISP("disp"),
		;
		
		private String val;
		private Keys(String val) {
			this.val = val;
		}
		
		public String val() {
			return this.val;
		}
	}
	
	private String name;
	
	private LinkedHashMap<Keys, Object> attributes;
	
	public Material() {}
	
	public Material(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LinkedHashMap<Keys, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(LinkedHashMap<Keys, Object> attributes) {
		this.attributes = attributes;
	}
	
	public Object getAttribute(Keys key) {
		return attributes.get(key);
	}
	
	
}
