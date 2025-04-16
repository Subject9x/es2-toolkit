package org.es2tlk.dba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.es2tlk.ScriptKeys;
import org.es2tlk.dbm.ConvertToDBM;
import org.hercworks.core.data.file.dyn.DynamixBitmap;
import org.hercworks.core.data.file.dyn.DynamixBitmapArray;
import org.hercworks.core.data.file.dyn.DynamixPalette;
import org.hercworks.core.io.transform.common.DynamixBitmapArrayTransformer;
import org.hercworks.core.io.transform.common.DynamixBitmapTransformer;
import org.hercworks.core.io.transform.common.DynamixPaletteTransformer;
import org.hercworks.voln.FileType;

import at.favre.lib.bytes.Bytes;

public class CompileDBA {
	
	public static void main(String[] args) {
		
		System.out.println("1. Copy assembleDBA.txt to the directory with .DBM files.");
		System.out.println("2. Fill out fields.");
		System.out.print("3. Please enter path and name of assembly file=");
		BufferedReader consoleRead = new BufferedReader(new InputStreamReader(System.in));
		
		String assemblyPath = null;
		
		try {
			assemblyPath = consoleRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
		System.out.println("you entered=[" + assemblyPath + "]");
		
		if(!assemblyPath.contains(".txt")) {
			System.out.println("--->Error! missing filename or txt extension of name.");
			System.exit(1);
		}
		
		File assemblyFile = new File(assemblyPath);
		
		if(!assemblyFile.exists()) {
			System.out.println("--->Error! directory doesn't exist.");
			System.exit(1);
		}
		
		String dbmDir = null;
		String dbaDir = null;
		String dbaFileName = null;
		String dplFileName = null;
		boolean index0Alpha = false;
		List<String> fileEntries = new ArrayList<String>();
		
		try {
			Scanner scanner = new Scanner(assemblyFile);

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line.contains("//")) {
					continue;
				}
				if(line.length() == 0) {
					continue;
				}
				if(line.contains(ScriptKeys.DBMDir.val())) {
					dbmDir = line.substring(line.lastIndexOf('=')+1);
				}
				if(line.contains(ScriptKeys.DBADirExport.val())) {
					dbaDir = line.substring(line.lastIndexOf('=')+1);	
				}
				if(line.contains(ScriptKeys.DBAName.val())) {
					dbaFileName = line.substring(line.lastIndexOf('=')+1);
				}
				if(line.contains(ScriptKeys.Palette.val())) {
					dplFileName = line.substring(line.lastIndexOf('=')+1);
				}
				if(line.contains(ScriptKeys.Indx0Alpha.val())) {
					index0Alpha = line.substring(line.lastIndexOf('=')+1).toLowerCase().equals("true") ? true : false;
				}
				if(line.toLowerCase().contains(".dbm") || line.toLowerCase().contains(".png")) {
					fileEntries.add(line.substring(0, line.lastIndexOf('.')+4));
				}	
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
		
		System.out.println(ScriptKeys.DBMDir.val() + "= " + dbmDir);
		System.out.println(ScriptKeys.DBADirExport.val() + "= " + dbaDir);
		System.out.println(ScriptKeys.DBAName.val() + "= " + dbaFileName);
		if(dplFileName != null) {
			System.out.println(ScriptKeys.Palette.val() + "= " + dplFileName);
		}
		System.out.println("files=");
		
		if(fileEntries.isEmpty()) {
			System.out.println("--->Error! missing or malformed entries..");
			System.exit(3);
		}
		
		int pngPaletteErr = 0;	//if any .PNG are in the list and script is MISSING index0Alpha and palette keys, exit out!
		for(String f : fileEntries) {
			if(f.toLowerCase().contains(".png")) {
				if(dplFileName == null) {
					pngPaletteErr = 1;
				}
				else {
					pngPaletteErr = -1;
				}
				break;
			}
		}
		if(pngPaletteErr == 1) {
			System.err.println("--->ERROR! list contins .PNG files but is missing a .DPL file and path!");
			System.exit(2);
		}
		
		
		DynamixPalette dpl = null;
		//png files detected, and at least a DPL dir was given.
		if(pngPaletteErr == -1) {
			File dplFile = new File(dplFileName);
			if(!dplFile.exists() || !dplFile.isFile()) {
				System.err.println("--->ERROR! list contins .PNG files but is missing a .DPL file and path!");
				System.exit(2);
			}
			
			FileInputStream dplFileLoad;
			try {
				dplFileLoad = new FileInputStream(dplFile);
				DynamixPaletteTransformer dplTransform = new DynamixPaletteTransformer();
				dpl = (DynamixPalette) dplTransform.bytesToObject(dplFileLoad.readAllBytes());
				
				dplFileLoad.close();
				
				if(dpl == null) {
					throw new IOException("error loading .DPL file");
				}
				
				
			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
				System.exit(2);
			} catch (ClassCastException e) {
				System.err.println(e.getMessage());
				System.exit(2);
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.exit(2);
			}
		}
			
		DynamixBitmapArray dba = new DynamixBitmapArray();
		DynamixBitmap[] images = new DynamixBitmap[fileEntries.size()];
		
		int fileSize = 4;	//rows and cols are part of the recorded file size
		try {
			
			int rowCount = 0;	//so far columns is unused.

			DynamixBitmapTransformer transform = new DynamixBitmapTransformer();
			for (String s : fileEntries) {
				File dbmFile = new File(dbmDir + "/" + s);
				if (dbmFile.exists()) {
					DynamixBitmap dbm = null;
					transform.resetIndex();
					if(s.toLowerCase().contains(".png")) {
						dbm = ConvertToDBM.convertImage(dbmFile, dpl, index0Alpha);
						dbm.setRawBytes(transform.objectToBytes(dbm));
						dbm.setImageDataLen(dbm.getImageData().length());
						dbm.setFileSize(Bytes.from(dbm.getRawBytes().length));
					}
					else {
						FileInputStream fizz = new FileInputStream(dbmFile);
						dbm = (DynamixBitmap) transform.bytesToObject(fizz.readAllBytes());
						fizz.close();
					}
					
					if(dbm != null) {
						images[rowCount] = dbm;

						fileSize += dbm.getRawBytes().length;
						fileSize++;	// spacer byte between entries
						rowCount++;

						System.out.println("processed---> " + s);
					}
					else{
						System.out.println("--->ERROR! processing " + s);
						System.exit(3);
					}
				}
			}
			
			
			dba.setExt(FileType.DBA);
			dba.setArrayRow((short) rowCount);
			dba.setArrayCols((short) 0);
			dba.setFileSize(Bytes.from(fileSize).byteOrder(ByteOrder.LITTLE_ENDIAN));
			dba.setImages(images);

			System.out.println("---------------DBA Output---------------");
			System.out.println("File Name=" + dbaFileName);
			System.out.println("Rows=" + dba.getArrayRow());
			System.out.println("Cols=" + dba.getArrayCols());
			System.out.println("Total Images=" + dba.getImages().length);
			System.out.println("\n\nwriting out.--->");
			
			DynamixBitmapArrayTransformer transformDBA = new DynamixBitmapArrayTransformer();
			byte[] output = transformDBA.objectToBytes(dba);
			
			if(dbaDir.charAt(dbaDir.length()-1) != '/') {
				dbaDir += "/";
			}
			
			String outFileName = dbaFileName;
			if(!outFileName.toLowerCase().contains(FileType.DBA.val().toLowerCase())) {
				outFileName = dbaFileName.toUpperCase() + "." + FileType.DBA.val().toUpperCase();
			}
			File writeOut = new File(dbaDir + outFileName);
			FileOutputStream fozz = new FileOutputStream(writeOut);
			
			fozz.write(output);
			fozz.close();
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
}
