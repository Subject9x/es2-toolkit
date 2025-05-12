package org.es2tlk.dts.model;

import org.erenyenigul.obj.elements.Point;

public class FaceEntry {

	private String mtlName;
	private Point[] pointIndex;
	private int[] textureVerts;
	private Point normal;

	public FaceEntry() {}
	
	public FaceEntry(Point ...points) {
		this.pointIndex = points;
	}
	
	public FaceEntry(Point normal, Point ...points) {
		this.pointIndex = points;
		this.normal = normal;
	}
	
	public Point[] getPointIndex() {
		return pointIndex;
	}

	public void setPointIndex(Point[] pointIndex) {
		this.pointIndex = pointIndex;
	}

	public Point getNormal() {
		return normal;
	}

	public void setNormals(Point normal) {
		this.normal = normal;
	}

	public int[] getTextureVerts() {
		return textureVerts;
	}

	public void setTextureVerts(int[] textureVerts) {
		this.textureVerts = textureVerts;
	}

	public String getMtlName() {
		return mtlName;
	}

	public void setMtlName(String mtlName) {
		this.mtlName = mtlName;
	}
}
