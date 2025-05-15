package org.es2tlk.dts;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.es2tlk.dts.model.FaceEntry;
import org.es2tlk.dts.model.Material;
import org.es2tlk.dts.model.Material.Keys;
import org.es2tlk.dts.model.MaterialObj;
import org.es2tlk.dts.model.ObjGroup;
import org.hercworks.core.data.file.dts.TSGroup;
import org.hercworks.core.data.file.dts.TSObject;
import org.hercworks.core.data.file.dts.TSPoly;
import org.hercworks.core.data.file.dts.TSSurfaceEntry;
import org.hercworks.core.data.file.dts.bsp.TSBSPPart;
import org.hercworks.core.data.file.dts.part.TSCellAnimPart;
import org.hercworks.core.data.file.dts.part.TSPartList;
import org.hercworks.core.data.file.dts.poly.TSShadedPoly;
import org.hercworks.core.data.file.dts.poly.TSSolidPoly;
import org.hercworks.core.data.file.dts.poly.TSTexture4Poly;
import org.hercworks.core.data.file.dyn.DynamixBitmapArray;
import org.hercworks.core.data.file.dyn.DynamixThreeSpaceModel;
import org.hercworks.core.data.struct.Vec3Short;

public final class DTStoObj {

	public DTStoObj() {}
	
	public List<MaterialObj> convertDTS_to_OBJ(DynamixThreeSpaceModel dts, DynamixBitmapArray texture) {
		
		int meshIndex = 0;
		
		List<MaterialObj> meshes = new ArrayList<MaterialObj>();
		
		for(TSObject tso : dts.getMeshes()) {
			List<TSGroup> groups = new ArrayList<TSGroup>();
			
			groups = getTSGroups(tso, groups);
			
			if(groups.isEmpty()) {
				continue;
			}
			MaterialObj m = generateObjMesh(tso, groups, dts.originNameNoExt(), meshIndex, texture);
			
			meshes.add(m);
			meshIndex = meshIndex + 1;
		}
		
		
		return meshes;
	}
	
	
	private MaterialObj generateObjMesh(TSObject tso, List<TSGroup>groups, String fileName, int meshIndex, DynamixBitmapArray texture) {

		StringBuilder name = new StringBuilder();
		
		MaterialObj objMesh = new MaterialObj(name.append(fileName).append("_").append(meshIndex).toString());
		
		int groupIndex = 0;

		int[] vtId = new int[4];
		vtId[0] = objMesh.addTextureCoord(new Vector2D(0.0, 0.0));
		vtId[1] = objMesh.addTextureCoord(new Vector2D(1.0, 0.0));
		vtId[2] = objMesh.addTextureCoord(new Vector2D(1.0, 1.0));
		vtId[3] = objMesh.addTextureCoord(new Vector2D(0.0, 1.0));
		
		for(TSGroup tsg : groups) {
			ObjGroup grp = processTSGroup(groupIndex, tsg, objMesh, texture);
			objMesh.addGroup(grp, groupIndex);
			groupIndex = groupIndex  + 1;
		};
		
		return objMesh;
	}
	
	
	private ObjGroup processTSGroup(int index, TSGroup tsg, MaterialObj trgMesh, DynamixBitmapArray texture) {
				
		ObjGroup groupMesh = new ObjGroup(new String("TSGroup_"+index));
		
		int[] uvQuadIds = new int[] {2,1,4,3};
//		int[] uvQuadIds = new int[] {2,1,4,3};
		int[] uvTriIds = new int[] {3,2,1};
		
		for(TSObject tso : tsg.getItems()) {
			if(!(tso instanceof TSPoly)) {
				continue;
			}
			TSPoly poly = (TSPoly)tso;
			
			int[] pointIndex = new int[poly.getVertexCount()];
			Vector3D[] points = new Vector3D[poly.getVertexCount()];
			for(int i=0; i < poly.getVertexCount(); i++) {
				
//				int verIdx = (int)tsg.getIndexes()[poly.getVertexList() + i];
				int verIdx = (int)tsg.getIndexes()[poly.getVertexList() + poly.getVertexCount() - 1- i];
				
				Vec3Short vert = tsg.getPoints()[verIdx];
				
				double[] vertD = vert.toDouble();
				
				//rotates along X axis
//				Vector3D vertex = new Vector3D(vertD[0], vertD[2], vertD[1]);
				Vector3D vertex = new Vector3D(vertD[0] *-1, vertD[2], vertD[1]);
				
//				Vector3D originOffset = new Vector3D(0, 0, 0);
//				TSObject parent = tsg.getParent();
//				while(parent != null) {
//					if(parent instanceof TSRootObject) {
//						Vec3Short vs = ((TSRootObject)parent).getRootCenter();
//						double[] ofs = vs.toDouble();
//						originOffset = originOffset.add(new Vector3D(ofs[0] *-1, ofs[2], ofs[1]));
//						parent = parent.getParent();
//					}
//				}
//				double[] ctr = tsg.getPoints()[poly.getCenter()].toDouble();
//				originOffset = originOffset.add(new Vector3D(ctr[0] * -1, ctr[2] , ctr[1]));
				
//				vertex = vertex.add(originOffset);
				
				
				trgMesh.addPoint(vertex);
				points[i] = vertex;
			}

			
			Vec3Short n = tsg.getPoints()[poly.getNormal()];
			double[] nd = n.toDouble();
//			Vector3D norm = new Vector3D(nd[0] *-1, nd[2], nd[1]);
			Vector3D norm = getNormal(points);
			trgMesh.addNormal(norm);
			
			FaceEntry face = new FaceEntry(norm, points);
			
			if(pointIndex.length > 2) {
				if(pointIndex.length == 3 || pointIndex.length == 4) {
					if(pointIndex.length == 4) {
						face.setTextureVerts(uvQuadIds);	
					}
					else {
						face.setTextureVerts(uvTriIds);
					}
					addMaterial(tsg, poly, trgMesh, face, texture);
				}
			}
			
			groupMesh.addFaceEntry(face);
		}
		return groupMesh;
	}
	
	private void addMaterial(TSGroup tsg, TSPoly poly, MaterialObj obj,  FaceEntry face, DynamixBitmapArray dba) {
		
		Material m = null;
		String mtlName = null;
		if(poly instanceof TSTexture4Poly) {
			
			int color = tsg.getSurfaces()[((TSTexture4Poly)poly).getColorIndexId() / 4].getSurfaceColor();
			
			mtlName = dba.originNameNoExt() + "_" + color;
			m = obj.getMaterials().get(mtlName);
			if(m == null) {
				m = addNewTextureMaterial(mtlName, obj, dba);
			}
			face.setMtlName(mtlName);
		}
		else if(poly instanceof TSShadedPoly) {
			TSShadedPoly shadePoly = (TSShadedPoly)poly;
			
			int color = tsg.getSurfaces()[shadePoly.getColorIndexId() / 4].getSurfaceColor();
			
			mtlName = "shaded_" + color;
			m = obj.getMaterials().get(mtlName);
			if(m == null) {
				
				m = addNewShadeMaterial(mtlName, obj, color);
			}
			face.setMtlName(mtlName);
		}
		else if(poly instanceof TSSolidPoly) {
			TSSolidPoly solid = (TSSolidPoly)poly;
			
			int color = tsg.getSurfaces()[solid.getColorIndexId() / 4].getSurfaceColor();
			
			mtlName = "solid_" + color;
			m = obj.getMaterials().get(mtlName);
			if(m == null) {
				m = addNewSolidMaterial(mtlName, obj, color);
			}
			face.setMtlName(mtlName);
		}
	}
	
	private Material addNewTextureMaterial(String mtlName, MaterialObj obj, DynamixBitmapArray dba) {
		
		Material mtl = new Material(mtlName);
		LinkedHashMap<Keys, Object> attr = new LinkedHashMap<Material.Keys, Object>();
		
		attr.put(Keys.KEY_NEWMTL, mtlName);
		attr.put(Material.Keys.KEY_MAP_DIFFUSE, mtlName + ".png");
		attr.put(Material.Keys.KEY_DIFFUSE, new Vector3D(1.000, 1.000, 1.000));
		attr.put(Material.Keys.KEY_D_TRANS, Double.valueOf(1.0));
		
		mtl.setAttributes(attr);
		obj.getMaterials().put(mtlName, mtl);
		
		return mtl;
	}
	
	private Material addNewSolidMaterial(String mtlName, MaterialObj obj, int color) {

		Material mtl = new Material(mtlName);
		LinkedHashMap<Keys, Object> attr = new LinkedHashMap<Material.Keys, Object>();
		
		attr.put(Keys.KEY_NEWMTL, mtlName);
		
//		attr.put(Material.Keys.KEY_DIFFUSE, new Vector3D(color[0], color[1], color[2]));
		
		attr.put(Material.Keys.KEY_D_TRANS, Double.valueOf(1.0));
		
		mtl.setAttributes(attr);
		obj.getMaterials().put(mtlName, mtl);
		
		return mtl;
		
	}
	
	private Material addNewShadeMaterial(String mtlName, MaterialObj obj, int color) {

		
		Material mtl = new Material(mtlName);
		LinkedHashMap<Keys, Object> attr = new LinkedHashMap<Material.Keys, Object>();
		
		attr.put(Keys.KEY_NEWMTL, mtlName);
		
		Color c = valueOf(color);
		Color c2 = new Color(color, false);
		
//		attr.put(Material.Keys.KEY_DIFFUSE, new Vector3D(shades[0], shades[1], shades[2]));
		attr.put(Material.Keys.KEY_D_TRANS, Double.valueOf(1.0));
		
		mtl.setAttributes(attr);
		obj.getMaterials().put(mtlName, mtl);
		
		return mtl;
		
	}
	
	private List<TSGroup> getTSGroups(TSObject tso, List<TSGroup> groups) {
		
		if(tso instanceof TSGroup) {
			if(tso.getParent() instanceof TSBSPPart ||
					(tso.getParent() instanceof TSCellAnimPart && !((TSCellAnimPart)tso.getParent()).getParts()[1].equals(tso))){
				//test for 'shaded-only' copies of herc shapes, this appears to map to 'textures on/off' setting in engine.
				groups.add((TSGroup)tso);	
			}
			
			return groups;
		}

		if(tso instanceof TSPartList) {
			for(TSObject branch : ((TSPartList)tso).getParts()) {
				
				groups = getTSGroups(branch, groups);
			}
		}
	
		return groups;
	}
	
	
	private Vector3D getNormal(Vector3D[] points) {
		if(points.length == 1) {
			return points[0].normalize();
		}
		else if(points.length == 2) {
			return points[1].subtract(points[0]);
		}
		else if(points.length == 3) {
			Vector3D v1 = points[1].subtract(points[0]);
			Vector3D v2 = points[2].subtract(points[0]);
			return v1.subtract(v2);
		}
		else {
			Vector3D v1 = new Vector3D(0,0,0);
			for(int p=1; p < points.length - 2; p++) {
				Vector3D a1 = points[p].subtract(points[p-1]);
				Vector3D a2 = points[p+1].subtract(points[p]);
				v1 = v1.add(a1.subtract(a2));
			}
			return v1;
		}
	}
	
	public static Color valueOf(int color) {
	    float r = ((color >> 16) & 0xff) / 127;
	    float g = ((color >>  8) & 0xff) / 127;
	    float b = ((color      ) & 0xff) / 127;
//	    float a = ((color >> 24) & 0xff);
	    return new Color(r, g ,b);
	}
}
