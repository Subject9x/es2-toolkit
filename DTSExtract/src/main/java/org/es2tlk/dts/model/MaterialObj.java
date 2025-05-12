package org.es2tlk.dts.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.erenyenigul.obj.elements.Face;
import org.erenyenigul.obj.elements.Point;

/**
 * Attempt at an OBJ model object as defined by the Wavefront.java library....
 * but with support for a corresponding .MTL material file with UV, colors etc.
 * 
 * 
 */
public class MaterialObj {

	private String fileName;
	
	int vidx = 0;
	private List<Point> vertices;
	
	int nidx = 0;
	private List<Point> normals;
	
	int texidx = 0;
	private List<Vector2D> textureVerts;
	
	private List<ObjGroup> groups;
	
	private Map<String, Material> materials;
	
	private Map<Face, Material> materialBinding;
	
	public MaterialObj() {}
	
	public MaterialObj(String meshName) {
		this.fileName = meshName;
		this.vertices = new ArrayList<Point>();
		
		this.normals = new ArrayList<Point>();
		
		this.textureVerts = new ArrayList<Vector2D>();
		this.groups = new ArrayList<ObjGroup>();
		this.materials = new HashMap<String, Material>();
		materialBinding = new HashMap<Face, Material>();
	}

	
	public int addPoint(Point p) {
		vertices.add(p);
		vidx+=1;
		return vidx; 
	}
	
	public int addNormal(Point n) {
		normals.add(n);
		nidx += 1;
		return nidx;
	}
	
	public int addTextureCoord(Vector2D vt) {
		textureVerts.add(vt);
		texidx += 1;
		return texidx;
	}
	
	public void addGroup(ObjGroup grp) {
		this.groups.add(grp);
	}
	
	public List<Point> getVertices(){
		return vertices;
	}

	public List<Point> getNormals() {
		return normals;
	}

	public void setNormals(List<Point> normals) {
		this.normals = normals;
	}
	
	public List<Vector2D> getTextureVerts() {
		return textureVerts;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<ObjGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<ObjGroup> groups) {
		this.groups = groups;
	}

	public Map<String, Material> getMaterials() {
		return materials;
	}

	public void setMaterials(Map<String, Material> materials) {
		this.materials = materials;
	}

	public Map<Face, Material> getMaterialBinding() {
		return materialBinding;
	}

	public void setMaterialBinding(Map<Face, Material> materialBinding) {
		this.materialBinding = materialBinding;
	}

	public void exportObjWithMtl() {
		
	}
}
