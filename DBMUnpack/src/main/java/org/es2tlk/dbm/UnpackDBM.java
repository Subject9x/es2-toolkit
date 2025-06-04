package org.es2tlk.dbm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import org.es2tlk.ScriptKeys;
import org.hercworks.core.data.file.dyn.DynamixBitmap;
import org.hercworks.core.data.file.dyn.DynamixPalette;
import org.hercworks.core.io.transform.common.DynamixBitmapTransformer;
import org.hercworks.core.io.transform.common.DynamixPaletteTransformer;
import org.hercworks.core.io.write.DynFileWriter;
import org.hercworks.voln.FileType;

public class UnpackDBM {
	
	public static void main(String[] args) {
		
		boolean dplLoaded = false;
		
		System.out.println("1. fill out a copy of dbm_out.txt");
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
		
		String dbmDirPath = null;
		String dplFilePath = null;
		String pngFilePath = null;
		boolean index0Alpha = false;
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
				if(line.contains(ScriptKeys.DBMDir.val())) {
					dbmDirPath = line.substring(line.lastIndexOf('=')+1);	
				}
				else if(line.contains(ScriptKeys.Palette.val())) {
					dplFilePath = line.substring(line.lastIndexOf('=')+1);
				}
				else if(line.contains(ScriptKeys.PNGDir.val())) {
					pngFilePath = line.substring(line.lastIndexOf('=')+1);
				}
				else if(line.contains(ScriptKeys.Indx0Alpha.val())) {
					index0Alpha = line.substring(line.lastIndexOf('=')+1).toLowerCase().equals("true") ? true : false;
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
		
		System.out.println(ScriptKeys.DBMDir.val() + "=" + dbmDirPath);
		if(dplFilePath != null && !dplFilePath.equals("")) {
			System.out.println(ScriptKeys.Palette.val() + "=" + dplFilePath);	
		}
		
		if(dbmDirPath == null) {
			System.out.println("--->ERROR! " + ScriptKeys.DBMDir.val() + " parameter was empty.");
			System.exit(2);
		}
		
		File dbmInputDir = new File(dbmDirPath);
		if(!dbmInputDir.exists()) {
			if(!dbmInputDir.mkdir()) {
				System.out.println("--->ERROR! unable to make output directory at [" + dbmDirPath + "]");
				System.exit(3);
			}
		}
		
		File pngOutputDir = new File(pngFilePath);
		if(!pngOutputDir.exists()) {
			if(!pngOutputDir.mkdir()) {
				System.out.println("--->ERROR! unable to make output directory at [" + pngFilePath + "]");
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

		DynamixBitmapTransformer dbmTransform = new DynamixBitmapTransformer();
		
		for(String name : fileNames) {
			if(!name.toLowerCase().contains(FileType.DBM.val().toLowerCase())) {
				name = name.toLowerCase() + "." + FileType.DBM.val().toLowerCase();
			}
			
			File dbmInputFile = new File(dbmDirPath + name);
			if(!dbmInputFile.exists() || !dbmInputFile.isFile()) {
				System.out.println("--->ERROR! DBM file not found [" + dbmDirPath + name + "]" );
				continue;
			}
			
			try {
				FileInputStream fizz = new FileInputStream(dbmInputFile);
				dbmTransform.resetIndex();
				DynamixBitmap dbm = (DynamixBitmap)dbmTransform.bytesToObject(fizz.readAllBytes());
				fizz.close();
				
				dbm.setFileName(name);
				dbm.setFileName(dbm.originNameNoExt());
				
				if(dbm == null || dbm.getRawBytes() == null || dbm.getRawBytes().length <= 0) {
					System.out.println("--->ERROR! problem parsing dbm [" + name + "] file.");
				}
				else {
					DynFileWriter.writeDBMToFile(dbm, index0Alpha, dpl, pngFilePath);
				}
			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
				continue;
			} catch (ClassCastException e) {
				System.err.println(e.getMessage());
				continue;
			} catch (IOException e) {
				System.err.println(e.getMessage());
				continue;
			}
			
		}
		
		System.out.println("---Conversion complete---");
	}

}
