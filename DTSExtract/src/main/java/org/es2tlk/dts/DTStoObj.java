package org.es2tlk.dts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.erenyenigul.obj.elements.Point;
import org.erenyenigul.obj.elements.Vector;
import org.es2tlk.dts.model.FaceEntry;
import org.es2tlk.dts.model.Material;
import org.es2tlk.dts.model.Material.Keys;
import org.es2tlk.dts.model.MaterialObj;
import org.es2tlk.dts.model.ObjGroup;
import org.hercworks.core.data.file.dts.TSGroup;
import org.hercworks.core.data.file.dts.TSObject;
import org.hercworks.core.data.file.dts.TSPoly;
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

	private DTStoObj() {}
	
	public static Set<MaterialObj> convertDTS_to_OBJ(DynamixThreeSpaceModel dts, DynamixBitmapArray texture) {
		
		int meshIndex = 0;
		
		Set<MaterialObj> objMeshes = new HashSet<MaterialObj>();
		
		for(TSObject tso : dts.getMeshes()) {
			List<TSGroup> groups = getTSGroups(tso, new ArrayList<TSGroup>());
			
			if(groups.isEmpty()) {
				continue;
			}
			MaterialObj m = generateObjMesh(tso, groups, dts.getFileName(), meshIndex, texture);
			
			if(m != null) {
				objMeshes.add(m);
			}
			else{
				System.out.println("failed to add mesh");
			}
			meshIndex++;
		}
		
		
		return objMeshes;
	}
	
	
	private static MaterialObj generateObjMesh(TSObject tso, List<TSGroup>groups, String fileName, int meshIndex, DynamixBitmapArray texture) {

		StringBuilder name = new StringBuilder();
		
		MaterialObj objMesh = new MaterialObj(name.append(fileName).append("_").append(meshIndex).toString());
		
		int groupIndex = 0;

		int[] vtId = new int[4];
		vtId[0] = objMesh.addTextureCoord(new Vector2D(0.0, 0.0));
		vtId[1] = objMesh.addTextureCoord(new Vector2D(1.0, 0.0));
		vtId[2] = objMesh.addTextureCoord(new Vector2D(1.0, 1.0));
		vtId[3] = objMesh.addTextureCoord(new Vector2D(0.0, 1.0));
		
		
		for(TSGroup g : groups) {
			processTSGroup(groupIndex, g, objMesh, texture);
			groupIndex++;
		};
		
		return objMesh;
	}
	
	
	private static void processTSGroup(int index, TSGroup tsg, MaterialObj trgMesh, DynamixBitmapArray texture) {
		
		List<Material> materials = new ArrayList<Material>();
		
		ObjGroup groupMesh = new ObjGroup("TSGroup_"+index);
		
//		int[] uvQuadIds = new int[] {4,3,2,1};
		int[] uvQuadIds = new int[] {4,3,2,1};
		int[] uvTriIds = new int[] {1,2,3};
		
		int id = 0;
		for(TSObject tso : tsg.getItems()) {
			if(!(tso instanceof TSPoly)) {
				continue;
			}
			TSPoly poly = (TSPoly)tso;
			
			int[] pointIndex = new int[poly.getVertexCount()];
			Point[] points = new Point[poly.getVertexCount()];
			for(int i=0; i < poly.getVertexCount(); i++) {
				
				int verIdx = (int)tsg.getIndexes()[poly.getVertexList() + i];
//				int verIdx = (int)tsg.getIndexes()[poly.getVertexList() + poly.getVertexCount() - 1- i];
				
				Vec3Short vert = tsg.getPoints()[verIdx];
				
				double[] vertD = vert.toDouble();
				
				//rotates along X axis
				Point p = new Point(vertD[0], vertD[2], vertD[1] );
				
				Vector originOffset = new Vector(0, 0, 0);
//				TSObject parent = tsg.getParent();
//				while(parent != null) {
//					Vec3Short vs = new Vec3Short(((TSBasePart)parent).getCenter().getX(), ((TSBasePart)parent).getCenter().getY(), ((TSBasePart)parent).getCenter().getZ());
//					originOffset.add(vec3SToDoubleVec(vs));
//					parent = parent.getParent();
//				}
				
				//FIXME - polys are mirrored left< > right
				Vec3Short centerS = tsg.getPoints()[poly.getCenter()];
				double[] csd = centerS.toDouble();
				originOffset.add(new Vector(new Point(csd[0], csd[1], csd[2])));
				
				p = p.shift(originOffset);
				
				trgMesh.addPoint(p);
				points[i] = p;
			}
			
			if(pointIndex.length > 2) {
				
				Vec3Short n = tsg.getPoints()[poly.getNormal()];
				
				double[] nd = n.toDouble();
				
				Point pointNormal = new Point(nd[0], nd[1], nd[2]);
				
				trgMesh.addNormal(pointNormal);
				FaceEntry face = new FaceEntry(pointNormal, points);
				
				if(pointIndex.length == 4) {
					face.setTextureVerts(uvQuadIds);	
				}
				else {
					face.setTextureVerts(uvTriIds);
				}
				
				groupMesh.addFaceEntry(face);
				addMaterial(tsg, poly, trgMesh, face, texture);
			}
			
			trgMesh.addGroup(groupMesh);
		}
	}
	
	private static Vector vec3SToDoubleVec(Vec3Short vec3s) {
		double[] d = vec3s.toDouble();
		return new Vector(d[0], d[1], d[2]);
	}
	
	private static void addMaterial(TSGroup tsg, TSPoly poly, MaterialObj obj,  FaceEntry face, DynamixBitmapArray dba) {
		
		Material m = null;
		String mtlName = null;
		if(poly instanceof TSTexture4Poly) {
			int color = tsg.getColors()[((TSTexture4Poly)poly).getColorIndexId()];
			mtlName = dba.originNameNoExt() + "_" + color;
			m = obj.getMaterials().get(mtlName);
			if(m == null) {
				m = addNewTextureMaterial(mtlName, obj, dba);
			}
			face.setMtlName(mtlName);
		}
		else if(poly instanceof TSShadedPoly) {
			TSShadedPoly shadePoly = (TSShadedPoly)poly;
			int color = tsg.getColors()[shadePoly.getColorIndexId()];
			mtlName = "shaded_" + color;
			m = obj.getMaterials().get(mtlName);
			if(m == null) {
				
				m = addNewShadeMaterial(mtlName, obj, color);
			}
			face.setMtlName(mtlName);
		}
		else if(poly instanceof TSSolidPoly) {
			TSSolidPoly solid = (TSSolidPoly)poly;
			int color = tsg.getColors()[solid.getColorIndexId()];
			mtlName = "solid_" + color;
			m = obj.getMaterials().get(mtlName);
			if(m == null) {
				m = addNewSolidMaterial(mtlName, obj, color);
			}
			face.setMtlName(mtlName);
		}
	}
	
	private static Material addNewTextureMaterial(String mtlName, MaterialObj obj, DynamixBitmapArray dba) {
		
		Material mtl = new Material();
		LinkedHashMap<Keys, Object> attr = new LinkedHashMap<Material.Keys, Object>();
		
		attr.put(Keys.KEY_NEWMTL, mtlName);
		attr.put(Material.Keys.KEY_MAP_DIFFUSE, mtlName + ".png");
		attr.put(Material.Keys.KEY_DIFFUSE, new Vector3D(1.000, 1.000, 1.000));
		attr.put(Material.Keys.KEY_D_TRANS, Double.valueOf(1.0));
		
		mtl.setAttributes(attr);
		obj.getMaterials().put(mtlName, mtl);
		
		return mtl;
	}
	
	private static Material addNewSolidMaterial(String mtlName, MaterialObj obj, int rgb) {

		Material mtl = new Material();
		LinkedHashMap<Keys, Object> attr = new LinkedHashMap<Material.Keys, Object>();
		
		attr.put(Keys.KEY_NEWMTL, mtlName);
		Color c = new Color(rgb);
		attr.put(Material.Keys.KEY_DIFFUSE, new Vector3D(c.getRed(), c.getGreen(), c.getBlue()));
		attr.put(Material.Keys.KEY_D_TRANS, Double.valueOf(1.0));
		
		mtl.setAttributes(attr);
		obj.getMaterials().put(mtlName, mtl);
		
		return mtl;
		
	}
	
	private static Material addNewShadeMaterial(String mtlName, MaterialObj obj, int rgb) {

		
		Material mtl = new Material();
		LinkedHashMap<Keys, Object> attr = new LinkedHashMap<Material.Keys, Object>();
		
		attr.put(Keys.KEY_NEWMTL, mtlName);
		Color c = new Color(rgb);
		attr.put(Material.Keys.KEY_DIFFUSE, new Vector3D(c.getRed(), c.getGreen(), c.getBlue()));
		attr.put(Material.Keys.KEY_D_TRANS, Double.valueOf(1.0));
		
		mtl.setAttributes(attr);
		obj.getMaterials().put(mtlName, mtl);
		
		return mtl;
		
	}
	
	private static List<TSGroup> getTSGroups(TSObject tso, List<TSGroup> groups) {
		
		if(tso instanceof TSGroup) {
			if(tso.getParent() instanceof TSBSPPart ||
					(tso.getParent() instanceof TSCellAnimPart && ((TSCellAnimPart)tso.getParent()).getParts()[0].equals(tso))){
				groups.add((TSGroup)tso);	
			}
			
			return groups;
		}

		if(tso instanceof TSPartList) {
			for(TSObject branch : ((TSPartList)tso).getParts()) {
				
				groups = DTStoObj.getTSGroups(branch, groups);
			}
		}
	
		return groups;
	}
}
