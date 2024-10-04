package org.es2tlk.manager;

import java.util.Arrays;
import java.util.List;

public class MainEntry {
	
	public static void main(String[] args) {
		
		if(args.length > 1) {
			System.out.println("Too many args");
			System.exit(1);
		}
		
		if(cmdArgs.INSTALL.values.contains(args[0])) {
			ES2ModProjectSetup.setupNewProject(args);
		}
		if(cmdArgs.COMPILE.values.contains(args[0])) {
			
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
