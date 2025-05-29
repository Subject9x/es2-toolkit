package org.es2tlk.dts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.es2tlk.ScriptKeys;
import org.es2tlk.dts.model.FaceEntry;
import org.es2tlk.dts.model.Material;
import org.es2tlk.dts.model.Material.Keys;
import org.es2tlk.dts.model.MaterialObj;
import org.es2tlk.dts.model.ObjGroup;
import org.hercworks.core.data.file.dts.TSObject;
import org.hercworks.core.data.file.dyn.DynamixBitmapArray;
import org.hercworks.core.data.file.dyn.DynamixPalette;
import org.hercworks.core.data.file.dyn.DynamixThreeSpaceModel;
import org.hercworks.core.io.transform.common.DynamixBitmapArrayTransformer;
import org.hercworks.core.io.transform.common.DynamixPaletteTransformer;
import org.hercworks.core.io.transform.dbsim.DTSModelTransformer;

public class ExtractDTS {

	
	private static DTSModelTransformer dtsTransformer = new DTSModelTransformer();
	
	public static void main(String[] args) {
		
		System.out.println("1. fill out a copy of dts_out.txt");
		System.out.println("2. enter path and file of unpack.txt to here");
		System.out.print("unpack file= ");
		BufferedReader consoleRead = new BufferedReader(new InputStreamReader(System.in));
		
		String unpackPath = null;
		
		try {
			unpackPath = consoleRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
		System.out.println("you entered=[" + unpackPath + "]");
		
		if(!unpackPath.contains(".txt")) {
			System.out.println("--->Error! missing filename or txt extension of name.");
			System.exit(1);
		}
		
		File unpackFile = new File(unpackPath);
		
		if(!unpackFile.exists()) {
			System.out.println("--->Error! directory doesn't exist.");
			System.exit(1);
		}
		
		String dtsDirPath = null;
		String dplFilePath = null;
		String dbafilePath = null;
		String exportDirPath = null;
		double scalar = 0.1;
		
		boolean index0Alpha = true;	//default to true because DTS need it.
		ArrayList<String> fileNames = new ArrayList<String>();
		
		try {
			Scanner scanner = new Scanner(unpackFile);

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line.contains("//")) {
					continue;
				}
				if(line.length() == 0) {
					continue;
				}
				if(line.contains(ScriptKeys.DTSDir.val())) {
					dtsDirPath = line.substring(line.lastIndexOf('=')+1);	
				}
				else if(line.contains(ScriptKeys.Palette.val())) {
					dplFilePath = line.substring(line.lastIndexOf('=')+1);
				}
				else if(line.contains(ScriptKeys.DBAFILE.val())) {
					dbafilePath = line.substring(line.lastIndexOf('=')+1);
				}
				else if(line.contains(ScriptKeys.ExportDir.val())) {
					exportDirPath = line.substring(line.lastIndexOf('=')+1);
				}
				else if(line.contains("scalar")) {
					scalar = Double.valueOf(line.substring(line.lastIndexOf('=')+1));
				}
				else {
					fileNames.add(line);
				}
				
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
			System.exit(1);
		}	
		
		System.out.println(ScriptKeys.DBMDir.val() + "=" + dtsDirPath);
		if(dplFilePath != null && !dplFilePath.equals("")) {
			System.out.println(ScriptKeys.Palette.val() + "=" + dplFilePath);	
		}
		
		if(dtsDirPath == null) {
			System.out.println("--->ERROR! " + ScriptKeys.DBMDir.val() + " parameter was empty.");
			System.exit(2);
		}
		
		File dtsInputDir = new File(dtsDirPath);
		if(!dtsInputDir.exists()) {
			if(!dtsInputDir.mkdir()) {
				System.out.println("--->ERROR! unable to make output directory at [" + dtsDirPath + "]");
				System.exit(3);
			}
		}
		
		File exportDir = new File(exportDirPath);
		if(!exportDir.exists()) {
			if(!exportDir.mkdir()) {
				System.out.println("--->ERROR! unable to make output directory at [" + exportDirPath + "]");
				System.exit(3);
			}
		}
		
		boolean dplLoaded = false;
		DynamixPalette dpl = null;
		if(dplFilePath != null && dplFilePath.length() != 0) {
			File dplFile = new File(dplFilePath);
			if(!dplFile.exists()) {
				System.out.println("--->Warn! DPL file [" + dplFilePath + "] doesn't exist, skipping.");
			}
			else {
				FileInputStream readDplFile;
				try {
					readDplFile = new  FileInputStream(dplFile);
					DynamixPaletteTransformer dplTransform = new DynamixPaletteTransformer();
					dpl = (DynamixPalette) dplTransform.bytesToObject(readDplFile.readAllBytes());
					readDplFile.close();
					
					if(dpl != null && dpl.getRawBytes().length > 0){
						dplLoaded = true;
					}
				} catch (FileNotFoundException e) {
					dplLoaded = false;
					System.err.println(e.getMessage());
				} catch (IOException e) {
					dplLoaded = false;
					System.err.println(e.getMessage());
				}
			}
		}
		if(dpl == null) {
			dplLoaded = false;
		}
		
		if(!dplLoaded) {
			System.out.println("--->ERROR! problem parsing DPL palette file [" + dplFilePath + "]");
			System.exit(3);
		}
	
		if(fileNames.isEmpty() || fileNames.size() == 0) {
			System.out.println("--->ERROR! no DBM files found in unpack file.");
			System.exit(3);
		}
		
		boolean dbaLoaded = false;
		DynamixBitmapArray dba = null;
		if(dbafilePath != null && dbafilePath.length() != 0) {
			File dbaFile = new File(dbafilePath);
			if(!dbaFile.exists()) {
				System.out.println("--->Warn! DBA file [" + dbafilePath + "] doesn't exist, skipping.");
			}
			else {
				FileInputStream readDBAFile;
				try {
					readDBAFile = new  FileInputStream(dbaFile);
					DynamixBitmapArrayTransformer dbaTransform = new DynamixBitmapArrayTransformer();
					dba = (DynamixBitmapArray) dbaTransform.bytesToObject(readDBAFile.readAllBytes());
					dba.setFileName(dbafilePath.substring(dbafilePath.lastIndexOf("/")+1));
					readDBAFile.close();
					
					if(dba != null && dba.getRawBytes().length > 0){
						dbaLoaded = true;
					}
				} catch (FileNotFoundException e) {
					dplLoaded = false;
					System.err.println(e.getMessage());
				} catch (IOException e) {
					dplLoaded = false;
					System.err.println(e.getMessage());
				}
			}
		}
		if(dba == null) {
			dbaLoaded = false;
		}
		if(!dbaLoaded) {
			System.out.println("--->ERROR! problem parsing DBA palette file [" + dbafilePath + "]");
			System.exit(3);
		}

		for(String dtsFileName : fileNames) {
			processDTSFile(dtsFileName, exportDir, dtsInputDir, dba, scalar);
		}
		
	}
	
	private static void processDTSFile(String fileName, File expDir, File srcDir, DynamixBitmapArray texture, double scalar) {
		
		dtsTransformer.resetIndex();
		
		try(FileInputStream readDTSFile = new FileInputStream(new File(srcDir.getPath() + File.separator + fileName))){
			
			DynamixThreeSpaceModel dts = (DynamixThreeSpaceModel)dtsTransformer.bytesToObject(readDTSFile.readAllBytes());
			dts.setFileName(fileName);
			
			if(dts == null || dts.getRawBytes().length <= 0) {
				return;
			}
			
			if(dts.getMeshes().size() <= 0) {
				return;
			}
			
			DTStoObj toObj = new DTStoObj();
			List<MaterialObj> meshes = toObj.convertDTS_to_OBJ(dts, texture, scalar);
			if(meshes.isEmpty()) {
				return;
			}
			
			for(MaterialObj obj : meshes) {
				writeObjFile(fileName, expDir, obj);
				writeRawJson(dts.getMeshes().iterator().next(), expDir, obj.getFileName());
			}	
			
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			return;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}
	}
	
	private static void writeObjFile(String fileName, File expDir, MaterialObj mesh) {
		
		StringBuilder dat = new StringBuilder();
		
		dat.append("# Converted Dynamix ThreeSpace 2.0 DTS File\n");
		dat.append("# ").append(fileName).append("\n");
		dat.append("\n");
		
		if(mesh.getMaterials().size() != 0) {
			dat.append("mtllib ").append(mesh.getFileName() + ".mtl").append("\n\n");
		}
		
		dat.append("o ").append(mesh.getFileName()).append("\n");
		
		
		for(Vector3D p : mesh.getVertices()) {
			dat.append("v "+p.getX() + " " + p.getY() +" " + p.getZ() + "\n");
		}
		dat.append("# vertices ").append(mesh.getVertices().size()).append("\n\n");

		for(int t=0; t < mesh.getTextureVerts().size(); t++) {
			Vector2D vtex  = mesh.getTextureVerts().get(t);
			dat.append("vt "+vtex.getX() + " " + vtex.getY() +"\n");
		}
		dat.append("# vtextures ").append(mesh.getTextureVerts().size()).append("\n\n");
			
		for(Vector3D norm : mesh.getNormals()) {
			dat.append("vn "+norm.getX() + " " + norm.getY() +" " + norm.getZ() + "\n");
		}
		dat.append("# normals ").append(mesh.getNormals().size()).append("\n\n");
		
		/*
		 * Write faces by group G tag
		 * AND write faces organized by specific material!
		 */
		for(ObjGroup grp : mesh.getGroups()) {
			dat.append("\ng ").append(grp.getName()).append("\n");
			String mtlName = null;
			//write non-textured polygons
			for(FaceEntry face : grp.getFaces()) {
				if(face.getMtlName() == null) {
					dat.append(writeFaceEntry(face, mesh));
				}
			}
			
			for(Material mtl : mesh.getMaterials().values()) {
				for(FaceEntry face : grp.getFaces()) {			
					if(mesh.getMaterialBinding().get(face) == mtl) {
						if(mtlName == null) {
							//check for new material heading or not
							dat.append("usemtl ").append(mtl.getName()).append("\n");
							mtlName = mtl.getName();
						}
						dat.append(writeFaceEntry(face, mesh));
					}
				}
				//reset at end of group
				mtlName = null;
			}
		}
			
		File objFile = new File(expDir.getAbsolutePath() + File.separator + mesh.getFileName() + ".obj");	
		try(FileWriter fileWriter = new FileWriter(objFile)) {
			fileWriter.write(dat.toString());
			fileWriter.close();
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
		}
		
		if(!mesh.getMaterials().isEmpty()) {
			writeMaterialFile(mesh, expDir);
		}
	}
	
	private static void writeMaterialFile(MaterialObj mesh, File exportDir) {
		StringBuilder strMtl = new StringBuilder();
		
		for(String id : mesh.getMaterials().keySet()){
			Material mtl = mesh.getMaterials().get(id);
			for(Keys key : mtl.getAttributes().keySet()) {
				strMtl.append(key.val()).append(" ");
				
				Object attr = mtl.getAttribute(key);
				if(attr instanceof Vector3D) {
					Vector3D val = (Vector3D)attr;
					strMtl.append(val.getX()).append(" ");
					strMtl.append(val.getY()).append(" ");
					strMtl.append(val.getZ());
				}
				else {
					strMtl.append(attr.toString());
				}
				strMtl.append("\n");
			}
			strMtl.append("\n");
		}
		File mtlFile = new File(exportDir.getAbsolutePath() + File.separator + mesh.getFileName() + ".mtl");	
		try(FileWriter fileWriter = new FileWriter(mtlFile)) {
			fileWriter.write(strMtl.toString());
			fileWriter.close();
		} catch (IOException e) {
		    System.err.println(e.getLocalizedMessage());
		}
	}
	
	private static void writeRawJson(TSObject dts, File exportDir, String fileName) {
		
		File jsonFile = new File(exportDir.getAbsolutePath() + File.separator + fileName + ".json");	
		try(FileWriter fileWriter = new FileWriter(jsonFile)) {
			fileWriter.write(dts.toString());
			fileWriter.close();
		} catch (IOException e) {
		    System.err.println(e.getLocalizedMessage());
		}
	}
	
	private static String writeFaceEntry(FaceEntry face, MaterialObj mesh) {
		StringBuilder str = new StringBuilder();

		str.append("f ");
		for(int p=0; p < face.getPointIndex().length; p++) {
			str.append(mesh.getVertices().indexOf(face.getPointIndex()[p]) + 1);
			if(face.getMtlName() != null) {
				str.append("/").append(face.getTextureVerts()[p]);
			}
			else {
				str.append("/");	
			}
			str.append("/").append(mesh.getNormals().indexOf(face.getNormal()) + 1);
			str.append(" ");
		}
		str.append("\n");
		
		return str.toString();
	}
}

