package org.es2tlk.dts.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Surface {

	private int index;
	
	private List<Vector2D> textureVerts;
	
	private List<FaceEntry> faces;
	
	public Surface() {
		textureVerts = new ArrayList<Vector2D>();
		faces = new ArrayList<FaceEntry>();
	}

	public void addFace(FaceEntry f) {
		this.faces.add(f);
	}
	
	public void addFaces(FaceEntry ...faces) {
		this.faces.addAll(Arrays.asList(faces));
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<Vector2D> getTextureVerts() {
		return textureVerts;
	}

	public void setTextureVerts(List<Vector2D> textureVerts) {
		this.textureVerts = textureVerts;
	}

	public List<FaceEntry> getFaces() {
		return faces;
	}

	public void setFaces(List<FaceEntry> faces) {
		this.faces = faces;
	}
	
}
