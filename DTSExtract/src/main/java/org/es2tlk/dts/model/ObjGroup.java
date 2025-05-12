package org.es2tlk.dts.model;

import java.util.ArrayList;
import java.util.List;

/***
 * I appreciate the Wavefront.Java library that I ended up using, but its quite
 * barebones.
 */
public class ObjGroup {

	private String name;
	
	private List<FaceEntry> faces;
	
	public ObjGroup(String name) {
		this.name = name;

		this.faces = new ArrayList<FaceEntry>();
	}
	
	public void addFaceEntry(FaceEntry f) {
		faces.add(f);
	}
	
	public String getName() {
		return name;
	}
	
	public List<FaceEntry> getFaces(){
		return this.faces;
	}

	
}
