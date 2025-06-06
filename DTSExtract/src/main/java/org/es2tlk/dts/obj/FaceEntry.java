package org.es2tlk.dts.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class FaceEntry {

	private String mtlName;
	private Vector3D[] pointIndex;
	private int[] textureVerts;
	private Vector3D normal;

	public FaceEntry() {}
	
	public FaceEntry(Vector3D ...points) {
		this.pointIndex = points;
	}
	
	public FaceEntry(Vector3D normal, Vector3D ...points) {
		this.pointIndex = points;
		this.normal = normal;
	}
	
	public Vector3D[] getPointIndex() {
		return pointIndex;
	}

	public void setPointIndex(Vector3D[] pointIndex) {
		this.pointIndex = pointIndex;
	}

	public Vector3D getNormal() {
		return normal;
	}

	public void setNormals(Vector3D normal) {
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
