package org.es2tlk.manager;

import java.util.Arrays;
import java.util.List;

import org.hercworks.voln.FileType;

public class MainEntry {
	
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
		
		if(cmdArgs.INSTALL.values.contains(args[0])) {
			System.out.println("-> Found install -i arg first, installing.");
			ES2ModProjectSetup setup = new ES2ModProjectSetup();
			setup.setupNewProject(args);
		}
		else if(cmdArgs.COMPILE.values.contains(args[0])) {
			ES2CompileMod compileMod = new ES2CompileMod();
			compileMod.compileMod(args);
		}
	}

	private enum cmdArgs{
		
		INSTALL(Arrays.asList("-i", "install")),
		COMPILE(Arrays.asList("-c", "compile"));
		
		private List<String> values;
		
		private cmdArgs(List<String> vals) {
			this.values = vals;
		}

		public List<String> getValues() {
			return values;
		}

		public void setValues(List<String> values) {
			this.values = values;
		}
	}
}
