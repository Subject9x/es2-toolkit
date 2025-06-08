### DTSExtracot
Extracts ThreeSpace 2.0 DTS files to:
.obj file, matching .mtl file, and .json  for raw data.

+ Requires Java 19+ to be installed.

Steps
1. Extract `DTSExtract-0.2.0.jar` to anywhere.
2. in command prompt
    + `java -jar DTSExtract-0.2.0.jar`
3. Copy unpack.txt script.
4. Fill out fields of the script.
5. Follow install steps in command prompt.

Output
1. .OBJ static file with mesh data.
2. .MTL file with material bindings.
    + You must extract the corresponding .DBA file to .png with DBAUnpack and put the images in the same folder.
3. .JSON - this the entire DTS as raw data, including animations.

NOTES
+ Animations aren't supported at this time, just the data.
+ Some models are vertex-frame based, like weapons.
    + Hercs are bone based.
+ Exported obj will only pull out the mesh data which might have overlaps or unneeded faces, requiring some cleanup.
+ Exporter will split meshes out by highest object grouping.
    + Hercs have 6 Level of Detail models, each .obj is 1 of those LoD meshes.
    + Weapons are split by weapon, but the models have no naming so we have to pick through them for now.