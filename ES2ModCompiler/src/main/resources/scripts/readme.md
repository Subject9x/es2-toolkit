### ES2 Mod compiler
This tool enables players to extract, edit, and re-compile Earthsiege2 game data files.

#### Setup a mod workspace
+ Requires Java 19+ to be installed.

Steps
1. Extract `ES2ModCompiler.jar` to anywhere.
2. in command prompt
    + `java -jar ES2ModCompiler.jar -i`

3. Follow install steps in command prompt:
    + set the install path for ES2
    + set the directory for your mod to be installed, can be different from ES2 dir.
    + set mod name.
    + optionally add mod author.
4. Jar will create directory, and populate it with:
    + extracted and decompiled game files (.json).
    + standard compile scripts for VSHELL and DBSIM files.
    + an info.txt with your mod information.
5. Edit files as desired, happy modding!



#### Compiling mod.
+ Edit .json files as desired.
+ Assumes you have `ES2ModCompiler.jar`

Steps
1. In command prompt
    + `java -jar ES2ModCompiler.jar -c -dir <mod path> <scripts>`
    +  <mod path> = full path to your mod directory.
    + <scripts> space-delimited name of script files to run.
        + example: `java -jar ES2ModCompiler.jar -c -dir e:/earthsiege2/ compile_shell.txt  compile_sim.txt`
2. Jar will then compile all files that are listed in the compile scripts.
3. Files that are successful compiled will then be placed in `<your mod>/export/<dir>`.
4. Copy sub-dirs of `/export` to your `<earthsiege 2>` directory.
5. Run ES2.exe and see what happens.


#### Notes and such
+ Currently compile validation is loose, what might be a compilable json might in fact crash the game.
    + it's recommended that most data editing you do is 'like for like' where you don't mess with a lot of the data structure.
+ Script files can have `any` game files in them, but they must have the `module=` key at the top.