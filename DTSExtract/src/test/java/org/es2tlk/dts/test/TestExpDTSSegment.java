package org.es2tlk.dts.test;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.es2tlk.dts.MeshDataToDTSObjects;
import org.junit.Test;

public class TestExpDTSSegment {

	
	@Test
	public void testExport() {
		MeshDataToDTSObjects meshExporter = new MeshDataToDTSObjects();
	
		try(Scanner scr = new Scanner(new File("e://es2_os//dev/earthsiege2/unpack/dts/tsgroup.ts"))){
			
			meshExporter.parseGroup(scr, "e://es2_os//dev/earthsiege2/unpack/dts/tsgroup.json");
			scr.close();
		}
		catch(IOException e) {
			System.err.println(e.getMessage());
		}
		
	}
}
