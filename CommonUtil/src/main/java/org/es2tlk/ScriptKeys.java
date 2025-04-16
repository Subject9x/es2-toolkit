package org.es2tlk;

public enum ScriptKeys {

	DBAFILE("dba_file"),
	DBAName("dba_name"),
	DBMDir("dbm_dir"),
	DBADir("dba_dir"),
	DBADirExport("dba_out"),
	Palette("palette"),
	PNGDir("png_dir"),
	Indx0Alpha("index0alpha"),
	IMGMode("mode"),
	;
	
	
	private String val;
	
	private ScriptKeys(String s) {
		this.val = s.toLowerCase();
	}
	
	public String val() {
		return this.val.toLowerCase();
	}
	
}
