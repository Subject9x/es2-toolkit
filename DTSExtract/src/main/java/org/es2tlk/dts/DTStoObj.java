package org.es2tlk.dts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.es2tlk.dts.model.FaceEntry;
import org.es2tlk.dts.model.Material;
import org.es2tlk.dts.model.Material.Keys;
import org.es2tlk.dts.model.MaterialObj;
import org.es2tlk.dts.model.ObjGroup;
import org.hercworks.core.data.file.dts.DefaultShapeColors;
import org.hercworks.core.data.file.dts.TSGroup;
import org.hercworks.core.data.file.dts.TSObject;
import org.hercworks.core.data.file.dts.TSPoly;
import org.hercworks.core.data.file.dts.anim.ANShape;
import org.hercworks.core.data.file.dts.bsp.TSBSPPart;
import org.hercworks.core.data.file.dts.part.TSCellAnimPart;
import org.hercworks.core.data.file.dts.part.TSPartList;
import org.hercworks.core.data.file.dts.poly.TSShadedPoly;
import org.hercworks.core.data.file.dts.poly.TSSolidPoly;
import org.hercworks.core.data.file.dts.poly.TSTexture4Poly;
import org.hercworks.core.data.file.dyn.DynamixBitmapArray;
import org.hercworks.core.data.file.dyn.DynamixThreeSpaceModel;
import org.hercworks.core.data.struct.Vec2Short;
import org.hercworks.core.data.struct.Vec3Short;

public final class DTStoObj {


	int[] uvQuadIds = new int[] {1,2,3,4};
//	int[] uvQuadIds = new int[] {2,1,4,3};
	int[] uvTriIds = new int[] {3,2,1};
	
	double scalarFactor = 1.0;
	boolean applyTransform = true;
	
	//ANShape only
	private Map<Integer, Integer> animListRelationsMap = new HashMap<Integer, Integer>();
	
	public DTStoObj(double scalar, boolean applyTransform) {
		this.scalarFactor = scalar;
		this.applyTransform = applyTransform;
	}
	
	public List<MaterialObj> convertDTS_to_OBJ(DynamixThreeSpaceModel dts, DynamixBitmapArray texture, double scalar) {
		
		int meshIndex = 0;
		scalarFactor = scalar != 0.0 ? scalar : 1.0;	//protect against scale 0
		
		List<MaterialObj> meshes = new ArrayList<MaterialObj>();
		
		for(TSObject tso : dts.getMeshes()) {
			TSObject root = tso;
			
			List<TSGroup> groups = new ArrayList<TSGroup>();
			
			groups = getTSGroups(tso, groups);
			
			if(groups.isEmpty()) {
				continue;
			}
			MaterialObj m = generateObjMesh(root, tso, groups, dts.originNameNoExt(), meshIndex, texture);
			
			meshes.add(m);
			meshIndex = meshIndex + 1;
		}
		
		
		return meshes;
	}
	
	
	private MaterialObj generateObjMesh(TSObject root, TSObject tso, List<TSGroup>groups, String fileName, int meshIndex, DynamixBitmapArray texture) {

		//Build default transforms relations map
		if(root instanceof ANShape) {
			ANShape shape = (ANShape)root;
			for(Vec2Short rel : shape.getAnimationList().getRelations()) {
				animListRelationsMap.put((int)rel.getY(), (int)rel.getX());
			}
		}
		
		StringBuilder name = new StringBuilder();
		
		MaterialObj objMesh = new MaterialObj(name.append(fileName).append("_").append(meshIndex).toString());

		//TSPoly used for 'bones' or something, who knows, this'll probably bite me later.
		addNewPolyMaterial("poly_0", objMesh);
		
		
		int[] vtId = new int[4];
		vtId[0] = objMesh.addTextureCoord(new Vector2D(0.0, 0.0));
		vtId[1] = objMesh.addTextureCoord(new Vector2D(1.0, 0.0));
		vtId[2] = objMesh.addTextureCoord(new Vector2D(1.0, 1.0));
		vtId[3] = objMesh.addTextureCoord(new Vector2D(0.0, 1.0));
		
		for(TSGroup tsg : groups) {
			ObjGroup grp = processTSGroup(root, tsg, objMesh, texture);
			objMesh.addGroup(grp);
		};
		
		return objMesh;
	}
	
	
	private ObjGroup processTSGroup(TSObject root, TSGroup tsg, MaterialObj trgMesh, DynamixBitmapArray texture) {
				
		ObjGroup groupMesh = new ObjGroup(new String("TSGroup_"+tsg.getListIndex()));
		
		for(TSObject tso : tsg.getPolys()) {
			if(!(tso instanceof TSPoly)) {
				continue;
			}
			TSPoly poly = (TSPoly)tso;
			
			int[] pointIndex = new int[poly.getVertexCount()];
			Vector3D[] points = new Vector3D[poly.getVertexCount()];
			
			if(points.length < 5) {
				processRegularFace(root, tsg, poly, groupMesh, trgMesh, points, pointIndex, texture);
			}
			else{
				triangulatePolygon(root, tsg, poly, groupMesh, trgMesh, points, pointIndex, texture);
			}
		}
		return groupMesh;
	}
	

	private Vector3D calcOffset(ANShape root, TSGroup tsg, TSPoly poly) {
		
		Vector3D offset = new Vector3D(0,0,0);
		int transformId = tsg.getTransform();
		
		while(transformId != -1) {
			
			short defTrnId = root.getAnimationList().getDefaultTransforms()[transformId];
			double[] tls = root.getAnimationList().getTransforms()[(int)defTrnId].getTranslation().toDouble();
			Vector3D tlsVec = new Vector3D(tls);
			offset = offset.add(tlsVec);
			transformId = animListRelationsMap.get(transformId); 
		}
		
		return offset;
	}
	
	private void processRegularFace(TSObject root, TSGroup tsg, TSPoly poly, ObjGroup groupMesh, MaterialObj trgMesh, Vector3D[] points, int[] pointIndex, DynamixBitmapArray texture) {
		
		Vector3D offset = null;
		if(root instanceof ANShape && applyTransform) {
			offset = calcOffset((ANShape)root, tsg, poly);
		}
		
		for(int i=0; i < poly.getVertexCount(); i++) {
			
			int verIdx = (int)tsg.getIndexes()[poly.getVertexList() + poly.getVertexCount() - 1- i];

			Vector3D vertex = new Vector3D(tsg.getPoints()[verIdx].toDouble());
			
			if(offset != null) {
				vertex = vertex.add(offset);
			}
			vertex = vertex.scalarMultiply(scalarFactor);
			
			trgMesh.addPoint(vertex);
			points[i] = vertex;
		}
		
		Vec3Short n = tsg.getPoints()[poly.getNormal()];
		double[] nd = n.toDouble();
		Vector3D norm = getNormal(points);
		trgMesh.addNormal(norm);
		
		FaceEntry face = new FaceEntry(norm, points);
		
		if(pointIndex.length > 2 && pointIndex.length < 5) {
			if(pointIndex.length == 4) {
				face.setTextureVerts(uvQuadIds);	
			}
			else {
				face.setTextureVerts(uvTriIds);
			}
			addMaterial(tsg, poly, trgMesh, face, texture);
		}
		groupMesh.addFaceEntry(face);
	
	}
	
	
	private void triangulatePolygon(TSObject root, TSGroup tsg, TSPoly poly, ObjGroup groupMesh, MaterialObj trgMesh, Vector3D[] points, int[] pointIndex, DynamixBitmapArray texture){
		
		Vector3D offset = null;
		if(root instanceof ANShape && applyTransform) {
			offset = calcOffset((ANShape)root, tsg, poly);
		}
		
		int verIdx = (int)tsg.getIndexes()[poly.getVertexList()];
		
		Vector3D start = new Vector3D(tsg.getPoints()[verIdx].toDouble());
		if(offset != null) {
			start = start.add(offset);
			start = start.scalarMultiply(scalarFactor);
		}
		trgMesh.addPoint(start);
		
		for(int v=1; v < poly.getVertexCount() - 1; v++) {
			
			int v1idx = (int)tsg.getIndexes()[poly.getVertexList() + v];
			int v2idx = (int)tsg.getIndexes()[poly.getVertexList() + v + 1];
			
			Vector3D v1 = new Vector3D(tsg.getPoints()[v1idx].toDouble());
			
			Vector3D v2 = new Vector3D(tsg.getPoints()[v2idx].toDouble());
			
			Vector3D[] tri = new Vector3D[3];
			tri[0] = start;
			if(offset != null) {
				v1 = v1.add(offset);
				v2 = v2.add(offset);
			}
			tri[1] = v1.scalarMultiply(scalarFactor);
			tri[2] = v2.scalarMultiply(scalarFactor);
			
			Vector3D norm = getNormal(tri);
			
			trgMesh.addPoint(tri[1]);
			trgMesh.addPoint(tri[2]);
			trgMesh.addNormal(norm);
			
			FaceEntry f = new FaceEntry(norm, tri);
			f.setTextureVerts(uvTriIds);
			addMaterial(tsg, poly, trgMesh, f, texture);
			groupMesh.addFaceEntry(f);
		}
	}
	
	private void addMaterial(TSGroup tsg, TSPoly poly, MaterialObj obj,  FaceEntry face, DynamixBitmapArray dba) {
		
		Material m = null;
		String mtlName = null;
		if(poly instanceof TSTexture4Poly) {
			
			int color = tsg.getSurfaces()[((TSTexture4Poly)poly).getColorIndexId() / 4].getFrontColor();
			
			mtlName = dba.originNameNoExt() + "_" + color;
			m = obj.getMaterials().get(mtlName);
			if(m == null) {
				m = addNewTextureMaterial(mtlName, obj, dba);
			}
			face.setMtlName(mtlName);
		}
		else if(poly instanceof TSShadedPoly) {
			TSShadedPoly shadePoly = (TSShadedPoly)poly;
			
			short colorId = tsg.getSurfaces()[shadePoly.getColorIndexId() / 4].getFrontColor();
			
			DefaultShapeColors color = DefaultShapeColors.color(colorId);
			
			System.out.println(color);
			
			mtlName = "shaded_" + color.toString();
			m = obj.getMaterials().get(mtlName);
			
			if(m == null) {
				m = addNewShadeMaterial(color, obj, mtlName);
			}
			face.setMtlName(mtlName);
		}
		else if(poly instanceof TSSolidPoly) {
			TSSolidPoly solid = (TSSolidPoly)poly;
			
			short colorId = tsg.getSurfaces()[solid.getColorIndexId() / 4].getFrontColor();
			
			DefaultShapeColors color = DefaultShapeColors.color(colorId);
			
			System.out.println(color);
			
			mtlName = "solid_" + color.toString();
			m = obj.getMaterials().get(mtlName);
			
			if(m == null) {
				m = addNewSolidMaterial(color, obj, mtlName);
			}
			face.setMtlName(mtlName);
		}
		else {
			face.setMtlName("poly_0");
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
	
	private Material addNewSolidMaterial(DefaultShapeColors shade, MaterialObj obj, String mtlName) {
		
		Material mtl = new Material(mtlName);
		LinkedHashMap<Keys, Object> attr = new LinkedHashMap<Material.Keys, Object>();
		
		attr.put(Keys.KEY_NEWMTL, mtlName);
		
		attr.put(Material.Keys.KEY_DIFFUSE, new Vector3D(shade.rgb()));
		
		attr.put(Material.Keys.KEY_D_TRANS, Double.valueOf(1.0));
		
		mtl.setAttributes(attr);
		obj.getMaterials().put(mtlName, mtl);
		
		return mtl;
		
	}
	
	private Material addNewShadeMaterial(DefaultShapeColors shade, MaterialObj obj, String mtlName) {
		
		Material mtl = new Material(mtlName);
		LinkedHashMap<Keys, Object> attr = new LinkedHashMap<Material.Keys, Object>();
		
		attr.put(Keys.KEY_NEWMTL, mtlName);
		
		attr.put(Material.Keys.KEY_DIFFUSE, new Vector3D(shade.rgb()));
		
		attr.put(Material.Keys.KEY_D_TRANS, Double.valueOf(1.0));
		
		mtl.setAttributes(attr);
		obj.getMaterials().put(mtlName, mtl);
		
		return mtl;
		
	}
	
	//note - TSPoly seemed to be used for offset / markers metadata and not visible mesh data
	private Material addNewPolyMaterial(String mtlName, MaterialObj obj) {
		
		Material mtl = new Material(mtlName);
		LinkedHashMap<Keys, Object> attr = new LinkedHashMap<Material.Keys, Object>();
		
		attr.put(Keys.KEY_NEWMTL, mtlName);
		attr.put(Material.Keys.KEY_DIFFUSE, new Vector3D(1.0, 0.0, 0.0));
		attr.put(Material.Keys.KEY_D_TRANS, Double.valueOf(0.85));
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
}
