package org.hercworks.exp.dba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.hercworks.core.data.file.dyn.DynamixBitmap;
import org.hercworks.core.data.file.dyn.DynamixBitmapArray;
import org.hercworks.core.data.file.dyn.DynamixPalette;
import org.hercworks.core.io.transform.common.DynamixBitmapArrayTransformer;
import org.hercworks.core.io.transform.common.DynamixBitmapTransformer;
import org.hercworks.core.io.transform.common.DynamixPaletteTransformer;
import org.hercworks.voln.FileType;

public class UnpackDBA {

	private static String keyDBAFile = "dba_file";
	private static String keyDBMDir = "dbm_dir";
	private static String keyPalette = "palette";
	
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
				if(line.contains(keyDBAFile)) {
					dbaFilePath = line.substring(line.lastIndexOf('=')+1);
				}
				if(line.contains(keyDBMDir)) {
					dbmDirPath = line.substring(line.lastIndexOf('=')+1);	
				}
				if(line.contains(keyPalette)) {
					dplFilePath = line.substring(line.lastIndexOf('=')+1);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
			System.exit(1);
		}
		
		System.out.println(keyDBAFile + "=" + dbaFilePath);
		System.out.println(keyDBMDir + "=" + dbmDirPath);
		if(dplFilePath != null && !dplFilePath.equals("")) {
			System.out.println(keyPalette + "=" + dplFilePath);	
		}
		
		if(dbaFilePath == null) {
			System.out.println("--->ERROR! " + keyDBAFile + " parameter was empty.");
			System.exit(2);
		}
		
		if(dbmDirPath == null) {
			System.out.println("--->ERROR! " + keyDBMDir + " parameter was empty.");
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
					DynamixPalette dpl = (DynamixPalette) dplTransform.bytesToObject(readDplFile.readAllBytes());
					readDplFile.close();
					
					if(dpl != null && dpl.getRawBytes().length > 0){
						dplLoaded = true;
					}
				} catch (FileNotFoundException e) {
					dplLoaded = false;
					System.out.println("--->Warn!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		
		String dbaName = dbaFilePath.toLowerCase().substring(dbaFilePath.lastIndexOf('/')+1, dbaFilePath.toLowerCase().lastIndexOf("."+FileType.DBA.val()));
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
				String path = dbmOutputDir + File.separator + dbaName + "_" + frameCount + "." + FileType.DBM.val();
				File dbmFile = new File(path);
				FileOutputStream fileOut = new FileOutputStream(dbmFile);
				
				DynamixBitmapTransformer dbmTransform = new DynamixBitmapTransformer();
				
				fileOut.write(dbmTransform.objectToBytes(dbm));
				fileOut.close();
				System.out.println(path);
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
