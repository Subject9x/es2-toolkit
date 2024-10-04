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

import org.hercworks.core.data.file.dat.shell.ArmHerc;
import org.hercworks.core.data.file.dat.shell.ArmWeap;
import org.hercworks.core.data.file.dat.shell.CareerMissions;
import org.hercworks.core.data.file.dat.shell.HardpointOverlayConfig;
import org.hercworks.core.data.file.dat.shell.HercInf;
import org.hercworks.core.data.file.dat.shell.Hercs;
import org.hercworks.core.data.file.dat.shell.InitHerc;
import org.hercworks.core.data.file.dat.shell.RprHerc;
import org.hercworks.core.data.file.dat.shell.WeaponsDat;
import org.hercworks.core.data.file.dat.sim.BeamData;
import org.hercworks.core.data.file.dat.sim.HercSimDat;
import org.hercworks.core.data.file.dat.sim.MissileDatFile;
import org.hercworks.core.data.file.dat.sim.ProjectileData;
import org.hercworks.core.data.file.dbsim.FlightModel;
import org.hercworks.core.data.file.dbsim.GunLayout;
import org.hercworks.core.data.file.dbsim.PaperDollGraphic;
import org.hercworks.core.data.struct.herc.HercLUT;
import org.hercworks.core.io.read.VolFileReader;
import org.hercworks.core.io.transform.ThreeSpaceByteTransformer;
import org.hercworks.core.io.transform.dbsim.BeamDatFileTransformer;
import org.hercworks.core.io.transform.dbsim.FlightModelTransformer;
import org.hercworks.core.io.transform.dbsim.GunLayoutTransformer;
import org.hercworks.core.io.transform.dbsim.HercSimDataTransformer;
import org.hercworks.core.io.transform.dbsim.MissileDatFileTransformer;
import org.hercworks.core.io.transform.dbsim.PaperDiagramGraphTransformer;
import org.hercworks.core.io.transform.dbsim.ProjectileDataTransformer;
import org.hercworks.core.io.transform.shell.ArmHercTransformer;
import org.hercworks.core.io.transform.shell.ArmWeapTransformer;
import org.hercworks.core.io.transform.shell.CareerDataTransformer;
import org.hercworks.core.io.transform.shell.HardpointOverlayTransformer;
import org.hercworks.core.io.transform.shell.HercInfoTransformer;
import org.hercworks.core.io.transform.shell.HercsStartTransformer;
import org.hercworks.core.io.transform.shell.InitHercTransformer;
import org.hercworks.core.io.transform.shell.RprHercTransform;
import org.hercworks.core.io.transform.shell.WeaponsDatTransformer;
import org.hercworks.transfer.dto.file.TransferObject;
import org.hercworks.transfer.dto.file.shell.ArmHercDTO;
import org.hercworks.transfer.dto.file.shell.ArmWeapDTO;
import org.hercworks.transfer.dto.file.shell.CareerMissionsDTO;
import org.hercworks.transfer.dto.file.shell.HardpointOverlayDTO;
import org.hercworks.transfer.dto.file.shell.HercInfDTO;
import org.hercworks.transfer.dto.file.shell.InitHercDTO;
import org.hercworks.transfer.dto.file.shell.RepairHercDTO;
import org.hercworks.transfer.dto.file.shell.StartHercsDTO;
import org.hercworks.transfer.dto.file.shell.WeaponsDatDTO;
import org.hercworks.transfer.dto.file.sim.BeamDatDTO;
import org.hercworks.transfer.dto.file.sim.FlightModelDTO;
import org.hercworks.transfer.dto.file.sim.GunLayoutDTO;
import org.hercworks.transfer.dto.file.sim.HercSimDatDTO;
import org.hercworks.transfer.dto.file.sim.MissileDatDTO;
import org.hercworks.transfer.dto.file.sim.PaperDollDTO;
import org.hercworks.transfer.dto.file.sim.ProjectileDataDTO;
import org.hercworks.transfer.svc.impl.dbsim.BeamDatDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.FlightModelDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.GunLayoutDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.HercSimDataDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.PaperDollDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.ProjectileDatDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.ArmHercDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.ArmWeapDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.CareerMissionsDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.HardpointOverlayDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.HercInfoDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.InitHercDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.RepairHercDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.StartingHercsDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.WeaponsDatShellDTOServiceImpl;
import org.hercworks.voln.DataFile;
import org.hercworks.voln.FileType;
import org.hercworks.voln.VolDir;
import org.hercworks.voln.Voln;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ES2ModProjectSetup {

	private static ObjectMapper objectMapper;
	
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
	
	public static void setupNewProject(String[] args) {
		
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
			
			objectMapper = new ObjectMapper();
			
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
		
		populateSrcDirs(shell0, srcDirs.getAbsolutePath());
		populateSrcDirs(simvol0, srcDirs.getAbsolutePath());
		
		
		File scriptPath = new File(generatePath(endAsDir, modPath, "scripts"));
		scriptPath.mkdir();
		
		
		
		
		
		System.out.println("Mod setup complete!");
		System.exit(10);
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
	
	private static void populateSrcDirs(Voln vol, String srcExportDir) {
		for(VolDir dir : vol.getFolders().values()) {
			if(folderDirs.contains(FileType.typeFromVal(dir.getLabel()))) {
				File srcDir = new File(generatePath(endAsDir, srcExportDir, dir.getLabel().toUpperCase()));
				srcDir.mkdir();
				System.out.println("	" + generatePath(endAsDir, Voln.makeFileName(vol.getFileName()), dir.getLabel().toUpperCase()));
				
				if(srcDir.exists()) {
					for(DataFile volFile : dir.getFiles()) {
						try {
							volFile.setDir(FileType.typeFromVal(dir.getLabel()));
							writeDynamixFile(volFile, srcDir.getAbsolutePath());
						} catch (IOException e) {
							System.out.println(e.getMessage());
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
			FileType.DAT,
			FileType.DMG,
			FileType.DPL,
			FileType.FM,
			FileType.GAM,
			FileType.GL,
			FileType.PDG
	 * @param data
	 * @param dirPath
	 * @throws IOException 
	 * @throws DatabindException 
	 * @throws StreamWriteException 
	 */
	private static void writeDynamixFile(DataFile data, String dirPath) throws StreamWriteException, DatabindException, IOException {
		
		ThreeSpaceByteTransformer transformer;
		TransferObject dtoExport = null;
		File write = null;
		String fileName = data.originNameNoExt();
		
		switch(data.getDir()) {
		case DAT:
			if(data.getFileName().toLowerCase().contains("beam")) {
				transformer = new BeamDatFileTransformer();
				BeamData beam = (BeamData)transformer.bytesToObject(data.getRawBytes());
				dtoExport = (BeamDatDTO)(new BeamDatDTOServiceImpl().convertToDTO(beam));
			}
			else if(data.getFileName().toLowerCase().contains("proj")) {
				transformer = new ProjectileDataTransformer();
				ProjectileData proj = (ProjectileData)transformer.bytesToObject(data.getRawBytes());
				dtoExport = (ProjectileDataDTO)(new ProjectileDatDTOServiceImpl().convertToDTO(proj));
			}
			else if(HercLUT.getByAbbrev(data.originNameNoExt()) != null) {
				transformer = new HercSimDataTransformer();
				HercSimDat simDat = (HercSimDat)transformer.bytesToObject(data.getRawBytes());
				dtoExport = (HercSimDatDTO)(new HercSimDataDTOServiceImpl().convertToDTO(simDat));
			}
			
			else if(data.getFileName().toLowerCase().contains("BULLETS") 
					|| data.getFileName().toLowerCase().contains("ROCKETS")) {
				transformer = new MissileDatFileTransformer();
				MissileDatFile missiles = (MissileDatFile)transformer.bytesToObject(data.getRawBytes());
				missiles.setFileName(data.originNameNoExt());
				dtoExport = (MissileDatDTO)(new HercSimDataDTOServiceImpl().convertToDTO(missiles));
			}
			
			break;
			
		case FM:
			transformer = new FlightModelTransformer();
			FlightModel fm = (FlightModel)transformer.bytesToObject(data.getRawBytes());
			dtoExport = (FlightModelDTO)(new FlightModelDTOServiceImpl().convertToDTO(fm));
			break;
			
		case GAM:
			if(data.getFileName().toLowerCase().contains("ini_")) {
				transformer = new InitHercTransformer();
				InitHerc rprHerc = (InitHerc)transformer.bytesToObject(data.getRawBytes());
				dtoExport = (InitHercDTO)(new InitHercDTOServiceImpl().convertToDTO(rprHerc));
			}
			else if(data.getFileName().toLowerCase().contains("_hots")) {
				transformer = new HardpointOverlayTransformer();
				HardpointOverlayConfig overlay = (HardpointOverlayConfig)transformer.bytesToObject(data.getRawBytes());
				overlay.setFileName(data.getFileName().toUpperCase());
				dtoExport = (HardpointOverlayDTO)(new HardpointOverlayDTOServiceImpl().convertToDTO(overlay));
			}
			else if(data.getFileName().toLowerCase().contains("rpr_")) {
				transformer = new RprHercTransform();
				RprHerc rprHerc = (RprHerc)transformer.bytesToObject(data.getRawBytes());
				dtoExport = (RepairHercDTO)(new RepairHercDTOServiceImpl().convertToDTO(rprHerc));
			}
			else if(data.getFileName().toLowerCase().contains("arm_weap")) {
				transformer = new ArmWeapTransformer();
				ArmWeap armWeap = (ArmWeap)transformer.bytesToObject(data.getRawBytes());
				dtoExport = (ArmWeapDTO)(new ArmWeapDTOServiceImpl().convertToDTO(armWeap));
			}
			else if(data.getFileName().toLowerCase().contains("arm_")) {
				transformer = new ArmHercTransformer();
				ArmHerc armHerc = (ArmHerc)transformer.bytesToObject(data.getRawBytes());
				dtoExport = (ArmHercDTO)(new ArmHercDTOServiceImpl().convertToDTO(armHerc));
			}
			else if(data.getFileName().toLowerCase().contains("herc_inf")) {
				transformer = new HercInfoTransformer();
				HercInf hercInfo = (HercInf)transformer.bytesToObject(data.getRawBytes());
				dtoExport = (HercInfDTO)(new HercInfoDTOServiceImpl().convertToDTO(hercInfo));
			} 
			else if(data.getFileName().toLowerCase().contains("hercs")) {
				transformer = new HercsStartTransformer();
				Hercs hercs = (Hercs)transformer.bytesToObject(data.getRawBytes());
				dtoExport = (StartHercsDTO)(new StartingHercsDTOServiceImpl().convertToDTO(hercs));
			} 
			else if(data.getFileName().toLowerCase().contains("weapons")) {
				transformer = new WeaponsDatTransformer();
				WeaponsDat weaponsDat = (WeaponsDat)transformer.bytesToObject(data.getRawBytes());
				dtoExport = (WeaponsDatDTO)(new WeaponsDatShellDTOServiceImpl().convertToDTO(weaponsDat));
			} 
			else if(data.getFileName().toLowerCase().contains("career")) {
				transformer = new CareerDataTransformer();
				CareerMissions career = (CareerMissions)transformer.bytesToObject(data.getRawBytes());
				dtoExport = (CareerMissionsDTO)(new CareerMissionsDTOServiceImpl().convertToDTO(career));
			} 
			break;
			
		case GL:
			transformer = new GunLayoutTransformer();
			GunLayout gunLayout = (GunLayout)transformer.bytesToObject(data.getRawBytes());
			dtoExport = (GunLayoutDTO)(new GunLayoutDTOServiceImpl().convertToDTO(gunLayout));
			break;
			
		case PDG:
			transformer = new PaperDiagramGraphTransformer();
			PaperDollGraphic pdg = (PaperDollGraphic)transformer.bytesToObject(data.getRawBytes());
			dtoExport = (PaperDollDTO)(new PaperDollDTOServiceImpl().convertToDTO(pdg));
			break;
		default:
			break;
		}
		
		if(dtoExport != null) {
			dtoExport.setFileName(fileName);
			dtoExport.setFileExt(data.getExt().val());
			dtoExport.setDir(data.getDir().val());
			write = new File(generatePath(endPathOpen, dirPath, fileName + ".json"));
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(write, dtoExport);
			System.out.println("		"+fileName+".json");
		}
	}
}
