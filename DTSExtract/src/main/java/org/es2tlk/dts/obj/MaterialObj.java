package org.es2tlk.dts.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.erenyenigul.obj.elements.Face;

/**
 * Attempt at an OBJ model object as defined by the Wavefront.java library....
 * but with support for a corresponding .MTL material file with UV, colors etc.
 * 
 * 
 */
public class MaterialObj {

	private String fileName;
	
	int vidx = 0;
	private List<Vector3D> vertices = new ArrayList<Vector3D>();
	
	int nidx = 0;
	private List<Vector3D> normals = new ArrayList<Vector3D>();
	
	int texidx = 0;
	private List<Vector2D> textureVerts = new ArrayList<Vector2D>();
	
	private List<ObjGroup> groups = new ArrayList<ObjGroup>();
	
	private Map<String, Material> materials = new HashMap<String, Material>();
	
	private Map<FaceEntry, Material> materialBinding = new HashMap<FaceEntry, Material>();
	
	public MaterialObj() {}
	
	public MaterialObj(String meshName) {
		this.fileName = meshName;
	}

	
	public int addPoint(Vector3D p) {
		boolean isNew = true;
		int index = 0;
		for(Vector3D v : getVertices()) {
			if(v.getX() == p.getX() &&
					v.getY() == p.getY() &&
					v.getZ() == p.getZ()) {
				
				isNew = false;
				index = getVertices().indexOf(v);
			}
		}
		if(isNew) {
			getVertices().add(p);
			vidx+=1;
			index = vidx;
		}
		return index; 
	}
	
	public int addNormal(Vector3D n) {
		
		boolean isNew = true;
		int index = 0;
		for(Vector3D v : getNormals()) {
			if(v.getX() == n.getX() &&
					v.getY() == n.getY() &&
					v.getZ() == n.getZ()) {
				
				isNew = false;
				index = getNormals().indexOf(v);
			}
		}
		if(isNew) {
			getNormals().add(n);
			nidx += 1;
			index = nidx;
		}
		
		return index;
	}
	
	public int addTextureCoord(Vector2D vt) {
		getTextureVerts().add(vt);
		texidx += 1;
		return texidx;
	}
	
	public void addGroup(ObjGroup grp) {
		getGroups().add(grp);
		for(FaceEntry f : grp.getFaces()) {
			if(f.getMtlName() != null && !f.getMtlName().isEmpty()) {
				this.materialBinding.put(f, getMaterials().get(f.getMtlName()));
			}
		}
	}
	
	
	public List<Vector3D> getVertices(){
		return vertices;
	}

	public List<Vector3D> getNormals() {
		return normals;
	}

	public void setNormals(List<Vector3D> normals) {
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

	public Map<FaceEntry, Material> getMaterialBinding() {
		return materialBinding;
	}

	public void setMaterialBinding(Map<FaceEntry, Material> materialBinding) {
		this.materialBinding = materialBinding;
	}

	public void exportObjWithMtl() {
		
	}
}
