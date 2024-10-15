package org.es2tlk.manager.io;

import java.util.List;

import org.hercworks.voln.DataFile;

public class ES2CompileScript {

	public static String keyModule = "module=";
	
	public static String vshell = "VSHELL";
	public static String dbsim = "DBSIM";
	
	private String name;
	private String path;
	private String dir;
	private String targetExe;
	
	private List<String> compileList;
	
	private List<DataFile> compiledResources;
	
	public ES2CompileScript() {}
	
	public String getTargetExe() {
		return targetExe;
	}

	public void setTargetExe(String targetExe) {
		this.targetExe = targetExe;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public List<String> getCompileList() {
		return compileList;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setCompileList(List<String> compileList) {
		this.compileList = compileList;
	}

	public List<DataFile> getCompiledResources() {
		return compiledResources;
	}

	public void setCompiledResources(List<DataFile> compiledResources) {
		this.compiledResources = compiledResources;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}
}
