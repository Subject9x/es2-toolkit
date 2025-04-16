package org.es2tlk.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.es2tlk.manager.io.ES2CompileScript;
import org.es2tlk.manager.io.ModFileJsonProcessor;
import org.hercworks.core.data.file.dat.shell.ArmHerc;
import org.hercworks.core.data.file.dat.shell.ArmWeap;
import org.hercworks.core.data.file.dat.shell.CareerMissions;
import org.hercworks.core.data.file.dat.shell.HardpointOverlayConfig;
import org.hercworks.core.data.file.dat.shell.HercInf;
import org.hercworks.core.data.file.dat.shell.Hercs;
import org.hercworks.core.data.file.dat.shell.InitHerc;
import org.hercworks.core.data.file.dat.shell.RprHerc;
import org.hercworks.core.data.file.dat.shell.TrainingHercs;
import org.hercworks.core.data.file.dat.shell.WeaponsDat;
import org.hercworks.core.data.file.dat.sim.BeamData;
import org.hercworks.core.data.file.dat.sim.DebrisHerc;
import org.hercworks.core.data.file.dat.sim.HercSimDat;
import org.hercworks.core.data.file.dat.sim.MissileDatFile;
import org.hercworks.core.data.file.dat.sim.ProjectileData;
import org.hercworks.core.io.transform.dbsim.BeamDatFileTransformer;
import org.hercworks.core.io.transform.dbsim.DebrisHercTransformer;
import org.hercworks.core.io.transform.dbsim.FlightModelTransformer;
import org.hercworks.core.io.transform.dbsim.GunLayoutTransformer;
import org.hercworks.core.io.transform.dbsim.HercDamageFileTransformer;
import org.hercworks.core.io.transform.dbsim.HercSimDataTransformer;
import org.hercworks.core.io.transform.dbsim.MissileDatFileTransformer;
import org.hercworks.core.io.transform.dbsim.PaperDiagramGraphTransformer;
import org.hercworks.core.io.transform.dbsim.ProjectileDataTransformer;
import org.hercworks.core.io.transform.dbsim.WeaponPDGTransformer;
import org.hercworks.core.io.transform.shell.ArmHercTransformer;
import org.hercworks.core.io.transform.shell.ArmWeapTransformer;
import org.hercworks.core.io.transform.shell.CareerDataTransformer;
import org.hercworks.core.io.transform.shell.HardpointOverlayTransformer;
import org.hercworks.core.io.transform.shell.HercInfoTransformer;
import org.hercworks.core.io.transform.shell.HercsStartTransformer;
import org.hercworks.core.io.transform.shell.InitHercTransformer;
import org.hercworks.core.io.transform.shell.RprHercTransform;
import org.hercworks.core.io.transform.shell.TrainingHercsTransform;
import org.hercworks.core.io.transform.shell.WeaponsDatTransformer;
import org.hercworks.transfer.dto.file.shell.ArmHercDTO;
import org.hercworks.transfer.dto.file.shell.ArmWeapDTO;
import org.hercworks.transfer.dto.file.shell.CareerMissionsDTO;
import org.hercworks.transfer.dto.file.shell.HardpointOverlayDTO;
import org.hercworks.transfer.dto.file.shell.HercInfDTO;
import org.hercworks.transfer.dto.file.shell.InitHercDTO;
import org.hercworks.transfer.dto.file.shell.RepairHercDTO;
import org.hercworks.transfer.dto.file.shell.StartHercsDTO;
import org.hercworks.transfer.dto.file.shell.TrainingHercsDTO;
import org.hercworks.transfer.dto.file.shell.WeaponsDatDTO;
import org.hercworks.transfer.dto.file.sim.BeamDatDTO;
import org.hercworks.transfer.dto.file.sim.DebrisHercDTO;
import org.hercworks.transfer.dto.file.sim.FlightModelDTO;
import org.hercworks.transfer.dto.file.sim.GunLayoutDTO;
import org.hercworks.transfer.dto.file.sim.HercDmgDTO;
import org.hercworks.transfer.dto.file.sim.HercSimDatDTO;
import org.hercworks.transfer.dto.file.sim.MissileDatDTO;
import org.hercworks.transfer.dto.file.sim.PaperDollDTO;
import org.hercworks.transfer.dto.file.sim.ProjectileDataDTO;
import org.hercworks.transfer.dto.file.sim.WpnPDGDTO;
import org.hercworks.transfer.svc.impl.dbsim.BeamDatDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.DebrisHercDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.FlightModelDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.GunLayoutDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.HercSimDataDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.HercSimDmgDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.MissileDatDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.PaperDollDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.ProjectileDatDTOServiceImpl;
import org.hercworks.transfer.svc.impl.dbsim.WeapnPDGDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.ArmHercDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.ArmWeapDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.CareerMissionsDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.HardpointOverlayDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.HercInfoDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.InitHercDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.RepairHercDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.StartingHercsDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.TrainingHercsDTOServiceImpl;
import org.hercworks.transfer.svc.impl.shell.WeaponsDatShellDTOServiceImpl;
import org.hercworks.voln.DataFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sub-class responsible for 'compiling' a mod's files and then exporting them to
 * the target install directory.
 */
public class ES2CompileMod {

	private static String argExport = "-e";
	private static String argVerbose = "-x";
	private static String argMod = "-dir";
	
	private static String argKeyScript = ".txt";
	
	private static String classDefRegex = "\"classDef\":\"[a-zA-Z]+\"";
	
	private boolean willInstall = false;
	private boolean verbose = false;
	
	private String modPath = "";
	private File modDir;
	
	private List<String> scripts;
	
	private ObjectMapper objectMapper;
	
	public void compileMod(String[] args) {
		
		scripts = new ArrayList<String>();
		
		for(int a = 0; a < args.length; a++) {
			String arg = args[a];
			if(arg.equalsIgnoreCase(argExport)) {
				System.out.println(argExport + " = will install mod to root es2/ directory.");
				willInstall = true;
			}
			if(arg.equalsIgnoreCase(argVerbose)) {
				System.out.println(argVerbose + " = verbose logging output.");
				verbose = true;
			}
			if(arg.toLowerCase().contains(argMod)) {
				if(a + 1 < args.length) {
					modPath = args[a+1];
					System.out.println("dir= " + modPath);
				}
			}
			if(arg.toLowerCase().contains(argKeyScript)) {
				System.out.println("+ add script: " + arg);
				scripts.add(arg);
			}
		}
		
		if(modPath == null || modPath.equals("")) {
			System.err.println("---> ERROR! missing [-dir] arg immediately followed by FULL path to mod!");
			System.exit(1);
		}
		
		if(scripts.isEmpty() || scripts.size() == 0) {
			System.err.println("---> ERROR! no compile scripts to run!");
			System.exit(1);
		}
		
		modDir = new File(modPath);
		
		if(!modDir.exists()) {
			System.err.println("---> ERROR! mod dir [" + modDir.getAbsolutePath() + "] not found!");
			System.exit(1);
		}
		
		objectMapper = new ObjectMapper();
		
		for(String script : scripts) {
			processCompileScript(script);
		}

	}
	
	private void processCompileScript(String scriptName) {
		
		File scriptFile = new File(String.join(File.separator, modDir.getAbsolutePath(), scriptName));
		
		if(!scriptFile.exists()){
			System.err.println("---> ERROR! script file [" + scriptFile.getAbsolutePath() + "] not found!");
			return;
		}
		
		ES2CompileScript compileScript = new ES2CompileScript();
		compileScript.setName(scriptName);
		compileScript.setPath(scriptFile.getAbsolutePath());
		compileScript.setDir(modDir.getAbsolutePath());
		
		File exportDir = new File(String.join(File.separator, compileScript.getDir(),"export"));
		if(!exportDir.exists()) {
			exportDir.mkdir();
		}
		
		List<String> compileList = new ArrayList<String>();
		
		try {
			Scanner lineReader = new Scanner(scriptFile);
			while(lineReader.hasNextLine()) {
				String line = lineReader.nextLine();
				if(line.contains("//")) {
					continue;
				}
				else if(line.length() == 0) {
					continue;
				}
				else if(line.contains(ES2CompileScript.keyModule)) {
					compileScript.setTargetExe(line.substring(line.indexOf(ES2CompileScript.keyModule) 
							+ ES2CompileScript.keyModule.length()));
				}
				else if(line.contains(".json")){
					compileList.add(line);
				}
			}
			lineReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		if(compileScript.getTargetExe() == null || compileScript.getTargetExe().equalsIgnoreCase("")) {
			System.out.println("---> ERROR: " + scriptName + " is missing [module=] key!");
			return;
		}
		
		if(compileList.isEmpty()) {
			System.out.println("---> WARN: no files found for compilation.");
			return;
		}
		
		compileScript.setCompileList(compileList);
		compileScript.setCompiledResources(new ArrayList<DataFile>());
		
		if(compileScript.getTargetExe().equalsIgnoreCase("vshell")) {
			
			compileShellResource(compileScript);
			
		}
		else{
			compileSimResource(compileScript);
		}
		
		System.out.println("----------------- Script Complete");

	}
	
	private void compileShellResource(ES2CompileScript script) {
		ModFileJsonProcessor processor = new ModFileJsonProcessor();
		processor.init(verbose, null, objectMapper);
		
		for(String entry : script.getCompileList()) {
			File srcFile = new File(String.join(File.separator, script.getDir(), entry));
			if(srcFile.exists()) {
				String data = readMultilineJsonFile(srcFile);
				String classDef = readClassDefInJson(data);
				
				if(classDef == null) {
					System.err.print("Error!\n     missing class_def field for [" + entry +"], skipping compile");
					continue;
				}
				
				try {
					@SuppressWarnings("unchecked")
					Class<? extends DataFile> type = (Class<? extends DataFile>) Class.forName("org.hercworks.core.data.file.dat.shell." + classDef);
					
					DataFile compiledDataFileObject = null;
					//System.out.println(data); //XXX - developer debug only
					
					if(type == ArmHerc.class) {
						compiledDataFileObject =  processor.importJson(data, new ArmHercTransformer(), type, new ArmHercDTOServiceImpl(), ArmHercDTO.class);
					}
					else if(type == ArmWeap.class) {
						compiledDataFileObject =  processor.importJson(data, new ArmWeapTransformer(), type, new ArmWeapDTOServiceImpl(), ArmWeapDTO.class);
					}
					else if(type == CareerMissions.class) {
						compiledDataFileObject =  processor.importJson(data, new CareerDataTransformer(), type, new CareerMissionsDTOServiceImpl(), CareerMissionsDTO.class);
					}
					else if(type == HardpointOverlayConfig.class) {
						compiledDataFileObject =  processor.importJson(data, new HardpointOverlayTransformer(), type, new HardpointOverlayDTOServiceImpl(), HardpointOverlayDTO.class);
					}
					else if(type == HercInf.class) {
						compiledDataFileObject =  processor.importJson(data, new HercInfoTransformer(), type, new HercInfoDTOServiceImpl(), HercInfDTO.class);
					}
					else if(type == Hercs.class) {
						compiledDataFileObject =  processor.importJson(data, new HercsStartTransformer(), type, new StartingHercsDTOServiceImpl(), StartHercsDTO.class);
					}
					else if(type == InitHerc.class) {
						compiledDataFileObject =  processor.importJson(data, new InitHercTransformer(), type, new InitHercDTOServiceImpl(), InitHercDTO.class);
					}
					else if(type == RprHerc.class) {
						compiledDataFileObject =  processor.importJson(data, new RprHercTransform(), type, new RepairHercDTOServiceImpl(), RepairHercDTO.class);
					}
					else if(type == TrainingHercs.class) {
						compiledDataFileObject =  processor.importJson(data, new TrainingHercsTransform(), type, new TrainingHercsDTOServiceImpl(), TrainingHercsDTO.class);
					}
					else if(type == WeaponsDat.class) {
						compiledDataFileObject =  processor.importJson(data, new WeaponsDatTransformer(), type, new WeaponsDatShellDTOServiceImpl(), WeaponsDatDTO.class);
					}
					
					if(compiledDataFileObject != null) {
						script.getCompiledResources().add(compiledDataFileObject);
						File destDir = new File(String.join(File.separator, script.getDir(), "export",  compiledDataFileObject.getDir().val()));
						if(!destDir.exists()) {
							if(!destDir.mkdir()) {
								System.err.print("--->ERROR! failed to make export directory [" + destDir.getAbsolutePath() + "]");
								continue;
							}
						}
						File exportedFile = new File(String.join(File.separator, destDir.getAbsolutePath(), compiledDataFileObject.getFileName() 
								+ "." + compiledDataFileObject.getExt().val()));
						
						FileUtils.writeByteArrayToFile(exportedFile, compiledDataFileObject.getRawBytes());
						
						System.out.println("---> compiled: " + entry);
					}
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					continue;
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				
			}
			else {
				System.out.println("---> skipping " + entry + ", bad path [" + srcFile.getAbsolutePath() + "].");
			}	
		}
	}
	
	@SuppressWarnings("unchecked")
	private void compileSimResource(ES2CompileScript script) {
		ModFileJsonProcessor processor = new ModFileJsonProcessor();
		processor.init(verbose, null, objectMapper);
		
		for(String entry : script.getCompileList()) {
			File srcFile = new File(String.join(File.separator, script.getDir(), entry));
			if(srcFile.exists()) {
				String data = readMultilineJsonFile(srcFile);
				String classDef = readClassDefInJson(data);
				
				if(classDef == null) {
					System.err.print("Error!\n     missing class_def field for [" + entry +"], skipping compile");
					continue;
				}
				try {
					String packagePath = "org.hercworks.core.data.file.";
					Class<? extends DataFile> type;
					DataFile compiledDataFileObject = null;
					
					if(classDef.equalsIgnoreCase("FlightModel")) {
						type = (Class<? extends DataFile>) Class.forName("org.hercworks.core.data.file.dbsim."+classDef);
						compiledDataFileObject =  processor.importJson(data, new FlightModelTransformer(), type, new FlightModelDTOServiceImpl(), FlightModelDTO.class);
					}
					else if(classDef.equalsIgnoreCase("GunLayout")) {
						type = (Class<? extends DataFile>) Class.forName("org.hercworks.core.data.file.dbsim."+classDef);
						compiledDataFileObject =  processor.importJson(data, new GunLayoutTransformer(), type, new GunLayoutDTOServiceImpl(), GunLayoutDTO.class);
					}
					else if(classDef.equalsIgnoreCase("HercSimDamage")) {
						type = (Class<? extends DataFile>) Class.forName("org.hercworks.core.data.file.dbsim."+classDef);
						compiledDataFileObject =  processor.importJson(data, new HercDamageFileTransformer(), type, new HercSimDmgDTOServiceImpl(), HercDmgDTO.class);
						
					}
					else if(classDef.equalsIgnoreCase("PaperDollGraphic")) {
						type = (Class<? extends DataFile>) Class.forName("org.hercworks.core.data.file.dbsim."+classDef);
						compiledDataFileObject =  processor.importJson(data, new PaperDiagramGraphTransformer(), type, new PaperDollDTOServiceImpl(), PaperDollDTO.class);
					}
					else if(classDef.equalsIgnoreCase("WeaponPaperDiagram")) {
						type = (Class<? extends DataFile>) Class.forName("org.hercworks.core.data.file.dbsim."+classDef);
						compiledDataFileObject =  processor.importJson(data, new WeaponPDGTransformer(), type, new WeapnPDGDTOServiceImpl(), WpnPDGDTO.class);
					}
					else {
						type = (Class<? extends DataFile>) Class.forName("org.hercworks.core.data.file.dat.sim."+classDef);
						
						//System.out.println(data); //XXX - developer debug only
						
						if(type == BeamData.class) {
							compiledDataFileObject =  processor.importJson(data, new BeamDatFileTransformer(), type, new BeamDatDTOServiceImpl(), BeamDatDTO.class);
						}
						else if(type == HercSimDat.class) {
							compiledDataFileObject =  processor.importJson(data, new HercSimDataTransformer(), type, new HercSimDataDTOServiceImpl(), HercSimDatDTO.class);
						}
						else if(type == MissileDatFile.class) {
							compiledDataFileObject =  processor.importJson(data, new MissileDatFileTransformer(), type, new MissileDatDTOServiceImpl(), MissileDatDTO.class);
						}
						else if(type == ProjectileData.class) {
							compiledDataFileObject =  processor.importJson(data, new ProjectileDataTransformer(), type, new ProjectileDatDTOServiceImpl(), ProjectileDataDTO.class);
						}
						else if(type == DebrisHerc.class) {
							compiledDataFileObject = processor.importJson(data, new DebrisHercTransformer(), type, new DebrisHercDTOServiceImpl(), DebrisHercDTO.class);
						}
//						else if(type == Weapons.class) {
//							compiledDataFileObject =  processor.importJson(data, new Weapons(), type, new HercInfoDTOServiceImpl(), HercInfDTO.class);
//						}
					}
					
					if(compiledDataFileObject != null) {
						script.getCompiledResources().add(compiledDataFileObject);
						File destDir = new File(String.join(File.separator, script.getDir(), "export",  compiledDataFileObject.getDir().val()));
						if(!destDir.exists()) {
							if(!destDir.mkdir()) {
								System.err.print("--->ERROR! failed to make export directory [" + destDir.getAbsolutePath() + "]");
								continue;
							}
						}
						File exportedFile = new File(String.join(File.separator, destDir.getAbsolutePath(), compiledDataFileObject.getFileName() 
								+ "." + compiledDataFileObject.getExt().val()));
						
						FileUtils.writeByteArrayToFile(exportedFile, compiledDataFileObject.getRawBytes());
						
						System.out.println("---> compiled: " + entry);
					}
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					continue;
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				
			}
			else {
				System.out.println("---> skipping " + entry + ", bad path [" + srcFile.getAbsolutePath() + "].");
			}	
		}
	}
	
	
	private String readMultilineJsonFile(File file) {
		
		StringBuilder parsedString = new StringBuilder();
		try(BufferedReader buffer = new BufferedReader(new FileReader(file))){
			while(buffer.ready()) {
				String line = buffer.readLine();
				parsedString.append(line);
			}
			buffer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	    
		return parsedString.toString().replaceAll("\\s+", "");
	}
	
	private String readClassDefInJson(String json) {
		

		Pattern pattern = Pattern.compile(classDefRegex, Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(json);
	    if(!matcher.find()) {
			return null;
	    }
	    
		return matcher.group(0).split("\"")[3];
	}
}
