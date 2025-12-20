package org.es2tlk.dba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.es2tlk.ScriptKeys;
import org.hercworks.core.data.file.dyn.DynamixBitmap;
import org.hercworks.core.data.file.dyn.DynamixBitmapArray;
import org.hercworks.core.data.file.dyn.DynamixPalette;
import org.hercworks.core.io.transform.common.DynamixBitmapArrayTransformer;
import org.hercworks.core.io.transform.common.DynamixBitmapTransformer;
import org.hercworks.core.io.transform.common.DynamixPaletteTransformer;
import org.hercworks.core.io.write.DynFileWriter;
import org.hercworks.voln.FileType;

public class UnpackDBA {
	
	public static void main(String[] args) {
		
		boolean dplLoaded = false;
		
		System.out.println("1. fill out a copy of unpack.txt");
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
		
		String dbaFilePath = null;
		String dbmDirPath = null;
		String dplFilePath = null;
		boolean index0Alpha = false;
		boolean heightmap = false;
		
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
				if(line.contains(ScriptKeys.DBAFILE.val())) {
					dbaFilePath = line.substring(line.lastIndexOf('=')+1);
				}
				if(line.contains(ScriptKeys.DBMDir.val())) {
					dbmDirPath = line.substring(line.lastIndexOf('=')+1);	
				}
				if(line.contains(ScriptKeys.Palette.val())) {
					dplFilePath = line.substring(line.lastIndexOf('=')+1);
				}
				if(line.contains(ScriptKeys.Indx0Alpha.val())) {
					index0Alpha = line.substring(line.lastIndexOf('=')+1).toLowerCase().equals("true") ? true : false;
				}
				if(line.contains(ScriptKeys.Heightmap.val())) {
					heightmap =  line.substring(line.lastIndexOf('=')+1).toLowerCase().equals("true") ? true : false;
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
			System.exit(1);
		}
		
		System.out.println(ScriptKeys.DBAFILE.val() + "=" + dbaFilePath);
		System.out.println(ScriptKeys.DBMDir.val() + "=" + dbmDirPath);
		if(dplFilePath != null && !dplFilePath.equals("")) {
			System.out.println(ScriptKeys.Palette.val() + "=" + dplFilePath);	
		}
		
		if(dbaFilePath == null) {
			System.out.println("--->ERROR! " + ScriptKeys.DBAFILE.val() + " parameter was empty.");
			System.exit(2);
		}
		
		if(dbmDirPath == null) {
			System.out.println("--->ERROR! " + ScriptKeys.DBMDir.val() + " parameter was empty.");
			System.exit(2);
		}
		
		File dbaFile = new File(dbaFilePath);
		if(!dbaFile.exists()) {
			System.out.println("--->ERROR! file/directory not found [" + dbaFilePath + "]");
			System.exit(1);
		}
		
		File dbmOutputDir = new File(dbmDirPath);
		if(!dbmOutputDir.exists()) {
			if(!dbmOutputDir.mkdir()) {
				System.out.println("--->ERROR! unable to make output directory at [" + dbmDirPath + "]");
				System.exit(3);
			}
		}
		
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
					System.out.println("--->Warn! ");
				} catch (IOException e) {
					dplLoaded = false;
					e.printStackTrace();
				}
			}
		}
		if(dpl == null) {
			dplLoaded = false;
		}
		
		FileType dbaExt = FileType.DBA;
		if(dbaFilePath.toLowerCase().contains(FileType.DB0.val())) {
			dbaExt = FileType.DB0;
		}
		else if(dbaFilePath.toLowerCase().contains(FileType.DB1.val())) {
			dbaExt = FileType.DB1;
		}
		else if(dbaFilePath.toLowerCase().contains(FileType.DB2.val())) {
			dbaExt = FileType.DB2;
		}
		else if(dbaFilePath.toLowerCase().contains(FileType.HB0.val())) {
			dbaExt = FileType.HB0;
		}
		else if(dbaFilePath.toLowerCase().contains(FileType.HB1.val())) {
			dbaExt = FileType.HB1;
		}
		else if(dbaFilePath.toLowerCase().contains(FileType.HB2.val())) {
			dbaExt = FileType.HB2;
		}
		
		String dbaName = dbaFilePath.toLowerCase().substring(dbaFilePath.lastIndexOf('/')+1, dbaFilePath.toLowerCase().lastIndexOf("."+dbaExt.val()));
		int frameCount = 0;

		System.out.println("---Begin unpack---");
		try {
			FileInputStream fizz = new FileInputStream(new File(dbaFilePath));
			DynamixBitmapArrayTransformer dbaTransform = new DynamixBitmapArrayTransformer();
			DynamixBitmapArray dba = (DynamixBitmapArray) dbaTransform.bytesToObject(fizz.readAllBytes());
			
			fizz.close();
			
			if(dba == null || dba.getRawBytes() == null || dba.getRawBytes().length == 0) {
				System.out.println("--->ERROR! problem parsing .DBA file.");
				System.exit(4);
			}
			
			for(DynamixBitmap dbm : dba.getImages()) {
				dbm.setFileName(dbaName + "_" + frameCount);
				
				String path = dbmOutputDir + File.separator;
				String dbmPath = path + dbm.getFileName().toUpperCase() + "." + FileType.DBM.val().toUpperCase();
				
				File dbmFile = new File(dbmPath);
				FileOutputStream fileOut = new FileOutputStream(dbmFile);
				
				DynamixBitmapTransformer dbmTransform = new DynamixBitmapTransformer();
				
				fileOut.write(dbmTransform.objectToBytes(dbm));
				fileOut.close();
				System.out.println(path);
				
				if(heightmap) {
					DynFileWriter.writeDBMToHeightmap(dbm, dplFilePath);
				}
				else {
					if(dplLoaded) {
						DynFileWriter.writeDBMToFile(dbm, index0Alpha, dpl, path);
					}
					else {
						DynFileWriter.writeDBMToFileNoPalette(dbm, path);
					}
				}
				
				frameCount++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
		System.out.println("---Unpack complete---");
	}
}
