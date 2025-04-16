package org.es2tlk;

public enum ModeOptions {

	ImageDBM("dbm"),
	ImagePNG("png");
	
	
	private String val;
	
	private ModeOptions(String val) {
		this.val = val;
	}
	
	public String val() {
		return this.val.toLowerCase();
	}
}
