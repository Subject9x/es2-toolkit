### DBMConvert
tool for converting `.png` image files to Earhthsiege2 / ThreeSpace 2.0 `.DBM` files

+ Requires Java 19+ to be installed.

Steps
1. Extract `DBMConvert.jar` to anywhere.
2. in command prompt
    + `java -jar DBMConvert.jar`
3. Arguments
    + `-d` : full path to directory with `.png` files to convert, include trailing /
	+ `-f` : file-name fragment to scan on, will detect all files with this fragment.
	    + example `-f medium_` will find all files with "medium_" in them.
	+ `-p` : full path to ES2 `.dpl` file to be used with compiling.