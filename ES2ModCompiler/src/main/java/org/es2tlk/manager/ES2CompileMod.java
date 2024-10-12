package org.es2tlk.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.es2tlk.manager.io.ES2CompileScript;
import org.es2tlk.manager.io.ModFileJsonProcessor;
import org.hercworks.core.data.file.dat.shell.ArmHerc;
import org.hercworks.core.io.transform.shell.ArmHercTransformer;
import org.hercworks.transfer.dto.file.shell.ArmHercDTO;
import org.hercworks.transfer.svc.impl.shell.ArmHercDTOServiceImpl;
import org.hercworks.voln.DataFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sub-class responsible for 'compiling' a mod's files and then exporting them to
 * the target install directory.
 */
public class ES2CompileMod {

	private static String argExport = "-e";
	private static String argVerbose = "-x";
	private static String argMod = "-mod_dir=";
	
	private static String argKeyShell = "compile_shell";
	private static String argKeySim = "compile_sim";
	
	private static String keyClassDef = "\"class_def\" : \"";
	
	private boolean willInstall = false;
	private boolean verbose = false;
	
	private String modName = "";
	private File modDir;
	
	private List<String> scripts;
	
	private ObjectMapper objectMapper;
	
	public void compileMod(String[] args) {
		
		scripts = new ArrayList<String>();
		
		for(String arg : args) {
			if(arg.equalsIgnoreCase(argExport)) {
				System.out.println(argExport + " = will install mod to root es2/ directory.");
				willInstall = true;
			}
			if(arg.equalsIgnoreCase(argVerbose)) {
				System.out.println(argVerbose + " = verbose logging output.");
				verbose = true;
			}
			if(arg.toLowerCase().contains(argKeyShell) || arg.toLowerCase().contains(argKeySim)) {
				System.out.println("+ add script: " + arg);
				scripts.add(arg);
			}
			if(arg.toLowerCase().contains(argMod)) {
				modName = arg.substring(arg.indexOf(argMod)+argMod.length());
				System.out.println("mod_dir= " + modName);
			}
		}
		
		if(modName.equals("")) {
			System.err.println("---> ERROR! missing [-dir=] arg with mod folder directory.");
			System.exit(1);
		}
		
		if(scripts.isEmpty() || scripts.size() == 0) {
			System.err.println("---> ERROR! no compile scripts to run!");
			System.exit(1);
		}
		
        try {
    		URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
			modDir = new File(Paths.get(url.toURI()).toString() + File.separator + modName);
			
			if(!modDir.exists()) {
				System.err.println("---> ERROR! mod dir [" + modDir.getAbsolutePath() + "] not found!");
				System.exit(1);
			}
			
			objectMapper = new ObjectMapper();
			for(String script : scripts) {
				processCompileScript(script);
			}
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private void processCompileScript(String scriptName) {
		
		File scriptFile = new File(modDir + File.separator + scriptName);
		
		if(!scriptFile.exists()){
			System.err.println("---> ERROR! script file [" + scriptFile.getAbsolutePath() + "] not found!");
			return;
		}
		
		ES2CompileScript compileScript = new ES2CompileScript();
		compileScript.setName(scriptName);
		compileScript.setPath(scriptFile.getAbsolutePath());
		compileScript.setDir(scriptFile.getAbsolutePath().substring(0, scriptFile.getAbsolutePath().lastIndexOf(scriptName)));
		
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
			File srcFile = new File(script.getDir() + File.separator + entry);
			if(srcFile.exists()) {
				String[] data = readMultilineJsonFile(srcFile);
			
				if(data[0] == null || data[0].equals("")) {
					System.err.print("Error!\n     missing class_def field for [" + entry +"], skipping compile");
					continue;
				}
				try {
					@SuppressWarnings("unchecked")
					Class<? extends DataFile> type = (Class<? extends DataFile>) Class.forName("org.hercworks.core.data.file.dat.shell."+data[0]);
					
					DataFile file = null;
					if(type == ArmHerc.class) {
						System.out.println(data[1]);
						file =  processor.importJson(entry, new ArmHercTransformer(), type, new ArmHercDTOServiceImpl(), ArmHercDTO.class);
					}
					
					if(file != null) {
						script.getCompiledResources().add(file);
					}
					
					
				} catch (ClassNotFoundException e) {
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
				if(line.contains("class_def")) {
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
