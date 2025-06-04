package org.es2tlk;

public enum CmdArgs {
	
	Help("-h"),
	Dir("-d"),
	Palette("-p"),
	File("-f"),
	Alpha("-a"),
	Export("-e"),
	Import("-i"),
	GPLpalette("-gpl")
	;
	
	
	private String val;
	
	private CmdArgs(String s) {
		this.val = s.toLowerCase();
	}
	
	public String val() {
		return this.val.toLowerCase();
	}
	
}
