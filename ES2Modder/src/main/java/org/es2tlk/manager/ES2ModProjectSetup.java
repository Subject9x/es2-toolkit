package org.es2tlk.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.hercworks.core.io.read.VolFileReader;
import org.hercworks.core.io.transform.ThreeSpaceByteTransformer;
import org.hercworks.voln.DataFile;
import org.hercworks.voln.FileType;
import org.hercworks.voln.VolDir;
import org.hercworks.voln.Voln;

public class ES2ModProjectSetup {

	private static boolean endAsDir = true;
	private static boolean endPathOpen = false;
	
	public static List<FileType> folderDirs = Arrays.asList(
			FileType.DAT,
			FileType.DBA,
			FileType.DBM,
			FileType.DMG,
			FileType.DPL,
			FileType.FM,
			FileType.GAM,
			FileType.GL,
			FileType.PDG
	);
	
	public static void main(String[] args) {
		
		String pathES2Install = null;
		String modProjName = null;
		String modAuthor = null;
		String modPath = null;
		
		Voln shell0 = null;
		Voln simvol0 = null;
		
		System.out.println("This will setup folders and extract files from ES2 vols so that you may begin working on a mod.");
		System.out.println("Mod is installed to your <es2 install>/mods/<your mod>/");
		
		BufferedReader consoleRead = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.print("ES2 install dir=");
		try {
			pathES2Install = consoleRead.readLine();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		File es2InstallDir = new File(pathES2Install);
		
		if(pathES2Install == null || pathES2Install.length() == 0 || !es2InstallDir.exists() || !es2InstallDir.isDirectory()) {
			System.err.println("Empty ES2 directory or not found.");
			System.exit(1);
		}
		
		System.out.print("Mod Project Name=");
		try {
			modProjName = consoleRead.readLine();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		if(modProjName == null || modProjName.length() == 0) {
			System.err.println("Project name cannot be empty.");
			System.exit(1);
		}
		
		System.out.print("Mod Author=");
		try {
			modAuthor = consoleRead.readLine();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		
		File modRoot = new File(generatePath(endAsDir, es2InstallDir.getAbsolutePath(), "mods"));
		if(!modRoot.exists()) {
			if(!modRoot.mkdir()) {
				System.err.println("/mods dir couldn't be created.");
				System.exit(2);
			}
		}
		
		modPath = generatePath(endAsDir, modRoot.getAbsolutePath(), modProjName);
		
		File modDir = new File(modPath);
		if(modDir.exists()) {
			System.err.println("Dir[" + modPath + "] already in use!");
			System.exit(2);
		}
		
		if(!modDir.mkdir()) {
			System.err.println("Dir[" + modPath + "] couldn't be created.");
			System.exit(2);
		}
		System.out.println("---creating mod dir---");
		System.out.println(modPath);
		
		ES2ModInfo projectData = new ES2ModInfo();
		projectData.setProperty(ES2ModInfo.projDir, modPath);
		projectData.setProperty(ES2ModInfo.projName, modProjName);
		projectData.setProperty(ES2ModInfo.projAuthor, modAuthor);
		saveProjectConfig(projectData, modPath);
		
		File refDirs = new File(generatePath(endAsDir, modPath, "REF"));
		if(refDirs.mkdir()) {
			System.out.println("---setting up mod---");
			System.out.println("---> REF folders");
			
			try {
				shell0 = VolFileReader.parseVolFile(generatePath(endPathOpen, es2InstallDir.getAbsolutePath(), "VOL", "SHELL0.VOL"));
				simvol0 = VolFileReader.parseVolFile(generatePath(endPathOpen, es2InstallDir.getAbsolutePath(), "VOL", "SIMVOL0.VOL"));
				
				if(shell0 == null || simvol0 == null) {
					throw new NullPointerException();				
				}
			} catch (Exception e) {
				System.err.println("Error reading SHELL0.VOL and/or SIMVOL0.VOL at [" + es2InstallDir + "/VOL/");
				System.exit(-1);
			}
			
			File srcShell0Dir = new File(generatePath(endAsDir, refDirs.getAbsolutePath(), "SHELL0"));
			srcShell0Dir.mkdir();
			
			File srcSimVol0Dir = new File(generatePath(endAsDir, refDirs.getAbsolutePath(), "SIMVOL0"));
			srcSimVol0Dir.mkdir();
			
			populateExportedVolDirs(shell0, srcShell0Dir.getAbsolutePath());
			populateExportedVolDirs(simvol0, srcSimVol0Dir.getAbsolutePath());
			
		}
		else {
			System.out.println("failed to make /src directory at [" + refDirs +"]");
		}
		
		File srcDirs = new File(generatePath(endAsDir, modPath, "SRC"));
		srcDirs.mkdir();
		for(FileType t : folderDirs) {
			new File(generatePath(endAsDir, srcDirs.getAbsolutePath(), t.val().toLowerCase())).mkdir();
		}
		
		new File(generatePath(endAsDir, modPath, "scripts")).mkdir();
		
	
	}
	
	private static String generatePath(boolean endWithDir, String... segments) {
		
		String path = "";
		
		for(int i=0; i < segments.length; i++) {
			path += segments[i];
			if(i < segments.length - 1) {
				path += File.separator;
			}
			else if(endWithDir){
				path += File.separator;
			}
		}
		
		return path;
	}
	
//	private static void initProjectConfig(ES2ModInfo info, String modRootPath) {
//
//	}
//	
	private static void saveProjectConfig(ES2ModInfo info, String modRootPath){
		
		//generatePath(endPathOpen, modRootPath, "info.txt")
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(generatePath(endPathOpen, modRootPath, "info.txt"))));
			writer.write(ES2ModInfo.projDir + "=" + info.getProperty(ES2ModInfo.projDir)+"\n");
			writer.write(ES2ModInfo.projName + "=" + info.getProperty(ES2ModInfo.projName)+"\n");
			writer.write(ES2ModInfo.projAuthor + "=" + info.getProperty(ES2ModInfo.projAuthor)+"\n");
			writer.close();		
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} 
		
	}
	
	/**
	 * Pull a {@linkplain DataFile} object from a {@linkplain Voln} object and write it out to destination.
	 * @param data
	 * @param dirPath
	 */
	private static void extractVolFile(DataFile data, String dirPath) throws IOException {
		
		File export = new File(generatePath(endPathOpen, dirPath, data.getFileName()));
		
		try(FileOutputStream out = new FileOutputStream(export)){
			out.write(data.getRawBytes());
			out.close();
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			System.err.println("Problem writing " + data.getFileName() + " to file.");
		}
	}
	
	/**
	 * writes sub-directories from a vol file that match the current list of acceptable sub-dirs.
	 * @param vol
	 * @param volExportDir
	 */
	private static void populateExportedVolDirs(Voln vol, String volExportDir) {
		for(VolDir dir : vol.getFolders().values()) {
			if(folderDirs.contains(FileType.typeFromVal(dir.getLabel()))) {
				File srcDir = new File(generatePath(endAsDir, volExportDir, dir.getLabel().toUpperCase()));
				srcDir.mkdir();
				System.out.println("	" + generatePath(endAsDir, Voln.makeFileName(vol.getFileName()), dir.getLabel().toUpperCase()));
				
				if(srcDir.exists()) {
					for(DataFile volFile : dir.getFiles()) {
						try {
							extractVolFile(volFile, srcDir.getAbsolutePath());
						} catch (IOException e) {
							System.err.println(e.getMessage());
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
			FileType.DAT,
			FileType.DBA,
			FileType.DBM,
			FileType.DMG,
			FileType.DPL,
			FileType.FM,
			FileType.GAM,
			FileType.GL,
			FileType.PDG
	 * @param data
	 * @param dirPath
	 */
	private static void writeDynamixFile(DataFile data, String dirPath) {
		
		ThreeSpaceByteTransformer transformer;
		switch(data.getDir()) {
		case DAT:
			
			break;
		case DBA:
		
			break;
		case DBM:
		
			break;
		case DPL:
		
			break;
		case FM:
		
			break;
		case GAM:
		
			break;
		case GL:
		
			break;
		case PDG:
		
			break;
		default:
			break;
		}
	}
}
