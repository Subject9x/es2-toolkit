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

import org.hercworks.core.data.file.dyn.DynamixBitmap;
import org.hercworks.core.data.file.dyn.DynamixBitmapArray;
import org.hercworks.core.io.transform.common.DynamixBitmapArrayTransformer;
import org.hercworks.core.io.transform.common.DynamixBitmapTransformer;
import org.hercworks.voln.FileType;

import at.favre.lib.bytes.Bytes;

public class CompileDBA {

	private static String keyDirDBM = "dbm_dir";
	private static String keyDirDBA = "dba_out";
	private static String keyMode = "mode";
	private static String keyDBAName = "dba_name";
	
	public static void main(String[] args) {
		
		System.out.println("1. Copy assemble.txt to the directory with .DBM files.");
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
		String mode = "DBM"; //default
		String dbaFileName = null;
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
				if(line.contains(keyDirDBM)) {
					dbmDir = line.substring(line.lastIndexOf('=')+1);
				}
				if(line.contains(keyDirDBA)) {
					dbaDir = line.substring(line.lastIndexOf('=')+1);	
				}
				if(line.contains(keyMode)) {
					mode = line.substring(line.lastIndexOf('=')+1);
				}
				if(line.contains(keyDBAName)) {
					dbaFileName = line.substring(line.lastIndexOf('=')+1);
				}
				if(line.toLowerCase().contains("."+mode.toLowerCase())) {
					fileEntries.add(line);
				}	
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
		
		System.out.println("dbm_dir=" + dbmDir);
		System.out.println("dba_out=" + dbaDir);
		System.out.println("mode=" + mode);
		System.out.println("files=");
		
		if(fileEntries.isEmpty()) {
			System.out.println("--->Error! missing or malformed entries..");
			System.exit(3);
		}
		
		DynamixBitmapArray dba = new DynamixBitmapArray();
		DynamixBitmap[] images = new DynamixBitmap[fileEntries.size()];
		
		int fileSize = 4;	//rows and cols are part of the recorded file size
		int rowCount = 0;	//so far columns is unused.
		DynamixBitmapTransformer transform = new DynamixBitmapTransformer();
		try {
			for (String s : fileEntries) {
				File dbmFile = new File(dbmDir + "/" + s);
				if (dbmFile.exists()) {
					//System.out.print("loading dbm=" + dbmFile.getPath());
					FileInputStream fizz = new FileInputStream(dbmFile);
					
					DynamixBitmap dbm = (DynamixBitmap) transform.bytesToObject(fizz.readAllBytes());
					images[rowCount] = dbm;

					fileSize += dbm.getRawBytes().length;
					fileSize++;	// spacer byte between entries
					rowCount++;

					fizz.close();
					transform.resetIndex();
					System.out.println("processed---> " + s);

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
			
			File writeOut = new File(dbaDir + dbaFileName + "." + FileType.DBA.val());
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
