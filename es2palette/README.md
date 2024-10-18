### ES2Palette
tool for exporting `.DPL` palette files from Earthsiege2/ThreeSpace 2.0

+ Requires Java 19+ to be installed.

Steps
1. Extract `es2palette.jar` to anywhere.
2. in command prompt
    + `java -jar es2palette.jar`
3. Arguments
    + `-e` : full path to directory for export, with trailing /
	+ `-d` : full path to `.DPL` file to export.
	+ `-gpl` : <OPTIONAL> will generated a GIMP-valid version of palette for use in GIMP.
4. OUTPUT
    + a `.png` of all colors in palette.
	+ a plain text list of exact values in the `.DPL`.
	+ <OPTIONAL> a `.gpl` palette file for GIMP.