package org.es2tlk.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
	
	private static String keyClassDef = "\"classDef\" : \"";
	
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
		

	}
	
	private void compileShellResource(ES2CompileScript script) {
		ModFileJsonProcessor processor = new ModFileJsonProcessor();
		processor.init(verbose, null, objectMapper);
		
		for(String entry : script.getCompileList()) {
			File srcFile = new File(String.join(File.separator, script.getDir(), entry));
			if(srcFile.exists()) {
				String[] data = readMultilineJsonFile(srcFile);
			
				if(data[0] == null || data[0].equals("")) {
					System.err.print("Error!\n     missing class_def field for [" + entry +"], skipping compile");
					continue;
				}
				try {
					@SuppressWarnings("unchecked")
					Class<? extends DataFile> type = (Class<? extends DataFile>) Class.forName("org.hercworks.core.data.file.dat.shell."+data[0]);
					
					DataFile compiledDataFileObject = null;
					System.out.println(data[1]);
					
					if(type == ArmHerc.class) {
						compiledDataFileObject =  processor.importJson(data[1], new ArmHercTransformer(), type, new ArmHercDTOServiceImpl(), ArmHercDTO.class);
					}
					else if(type == ArmWeap.class) {
						compiledDataFileObject =  processor.importJson(data[1], new ArmWeapTransformer(), type, new ArmWeapDTOServiceImpl(), ArmWeapDTO.class);
					}
					else if(type == CareerMissions.class) {
						compiledDataFileObject =  processor.importJson(data[1], new CareerDataTransformer(), type, new CareerMissionsDTOServiceImpl(), CareerMissionsDTO.class);
					}
					else if(type == HardpointOverlayConfig.class) {
						compiledDataFileObject =  processor.importJson(data[1], new HardpointOverlayTransformer(), type, new HardpointOverlayDTOServiceImpl(), HardpointOverlayDTO.class);
					}
					else if(type == HercInf.class) {
						compiledDataFileObject =  processor.importJson(data[1], new HercInfoTransformer(), type, new HercInfoDTOServiceImpl(), HercInfDTO.class);
					}
					else if(type == Hercs.class) {
						compiledDataFileObject =  processor.importJson(data[1], new HercsStartTransformer(), type, new StartingHercsDTOServiceImpl(), StartHercsDTO.class);
					}
					else if(type == InitHerc.class) {
						compiledDataFileObject =  processor.importJson(data[1], new InitHercTransformer(), type, new InitHercDTOServiceImpl(), InitHercDTO.class);
					}
					else if(type == RprHerc.class) {
						compiledDataFileObject =  processor.importJson(data[1], new RprHercTransform(), type, new RepairHercDTOServiceImpl(), RepairHercDTO.class);
					}
					else if(type == TrainingHercs.class) {
						compiledDataFileObject =  processor.importJson(data[1], new TrainingHercsTransform(), type, new TrainingHercsDTOServiceImpl(), TrainingHercsDTO.class);
					}
					else if(type == WeaponsDat.class) {
						compiledDataFileObject =  processor.importJson(data[1], new WeaponsDatTransformer(), type, new WeaponsDatShellDTOServiceImpl(), WeaponsDatDTO.class);
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
	
	private void compileSimResource(ES2CompileScript script) {
			
	}
	
	
	
	
	
	private String[] readMultilineJsonFile(File file) {
		String[] ret = new String[2];
		
		StringBuilder parsedString = new StringBuilder();
		String classDef = null;
		try(BufferedReader buffer = new BufferedReader(new FileReader(file))){
			while(buffer.ready()) {
				String line = buffer.readLine();
				parsedString.append(line);
				if(line.contains("classDef")) {
					int classDefIdx = line.lastIndexOf(keyClassDef) ;
					ret[0] = line.substring(classDefIdx + keyClassDef.length() , line.lastIndexOf('\"'));
				}
			}
			buffer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new String[] {null, null};
		} catch (IOException e) {
			e.printStackTrace();
			return new String[] {null, null};
		}
		ret[1] = parsedString.toString().replaceAll("\\s+", "");
		return ret;
	}
	
}
