package org.es2tlk.dts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.hercworks.core.data.file.dts.TSObjectHeader;
import org.hercworks.core.data.file.dts.poly.TSSolidPoly;
import org.hercworks.transfer.dto.file.sim.dts.TSGroupDTO;
import org.hercworks.transfer.dto.file.sim.dts.TSObjectDTO;
import org.hercworks.transfer.dto.file.sim.dts.TSSurfaceEntryDTO;
import org.hercworks.transfer.dto.file.sim.dts.poly.TSPolyDTO;
import org.hercworks.transfer.dto.file.sim.dts.poly.TSShadedPolyDTO;
import org.hercworks.transfer.dto.file.sim.dts.poly.TSSolidPolyDTO;
import org.hercworks.transfer.dto.file.sim.dts.poly.TSTexture4PolyDTO;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * WARN: unstable, this converts some specific 'script' files to their json counter parts.
 * 
 * I have no idea if mesh editing is doable, but I want to try,and this is part of that doomed effort.
 * 
 * it exports to the DTO copy of objects so I can past them into the DTS.json files with minimal fuss.
 */
public class MeshDataToDTSObjects {

	private static List<String> tsgroupNodes = Arrays.asList(TSObjectHeader.TS_POLY.id(), TSObjectHeader.TS_SOLID_POLY.id(), TSObjectHeader.TS_SHADED_POLY.id(), TSObjectHeader.TS_TEXTURE4_POLY.id()); 
	private static List<String> surfaceTags = Arrays.asList("dba", "shd");
	
	private static Float[] v3Zero = new Float[] {0.0f, 0.0f, 0.0f};
	
	public MeshDataToDTSObjects() {}
	
	
	public void parseGroup(Scanner scr, String filePath) throws StreamWriteException, DatabindException, IOException {
		TSObjectDTO o = null;
		while(scr.hasNextLine()) {
			String line = scr.nextLine();
			if(line.length() == 0 || line.startsWith("//")) {
				continue;
			}
			else if(line.toLowerCase().equals(TSObjectHeader.TS_GROUP.id().toLowerCase())) {
				o = parseTSGroup(scr);
			}
		}
		
		if(o != null) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
			
			File export = new File(filePath);
			
			mapper.writeValue(export, o);
		}
	}
	
	public TSGroupDTO parseTSGroup(Scanner scr) {

		TSGroupDTO dto = new TSGroupDTO();
		dto.setTransform(Integer.valueOf(parseKey(scr.nextLine())));
		dto.setIdNumber(Integer.valueOf(parseKey(scr.nextLine())));
		dto.setRadius(Float.valueOf(Float.valueOf(parseKey(scr.nextLine()))));
		dto.setCenter(parseVecString(parseKey(scr.nextLine())));
		
		List<Float[]> verTList = new ArrayList<Float[]>();
		verTList.add(v3Zero);
		verTList.add(v3Zero);
		
		List<Integer> index = new ArrayList<Integer>();
		List<TSSurfaceEntryDTO> surfaces = new ArrayList<TSSurfaceEntryDTO>();
		List<TSObjectDTO> polys =  new ArrayList<TSObjectDTO>();
		
		
		
		while(scr.hasNextLine()) {
			String line = scr.nextLine();
			if(line.length() == 0 || line.startsWith("//")) {
				continue;
			}
			else if(line.toLowerCase().contains("v ")) {
				System.out.println(line);
				verTList.add(parseVertex(line));
			}
			else if(line.toLowerCase().equals("surface")) {
				while(scr.hasNextLine()) {
					String srfLine = scr.nextLine();
					System.out.println(srfLine);
					if(srfLine.toLowerCase().contains("dba")) {
						surfaces.add(parseSurfaceInfo(srfLine));
					}
					else if(srfLine.toLowerCase().contains("shd")){
						surfaces.add(parseSurfaceInfo(srfLine));
					}
					else if(srfLine.length() == 0 || srfLine.startsWith("//")) {
						continue;
					}
					else if(srfLine.contains("#")) {
						break;
					}
				}
			}
			else if(line.toLowerCase().equals("parts")){
				while(scr.hasNextLine()) {
					String polyLine = scr.nextLine();
					if(polyLine.toLowerCase().equals(TSObjectHeader.TS_POLY.id().toLowerCase())) {
						polys.add(parseTSPoly(null, scr, index));
					}
					else if(polyLine.toLowerCase().equals(TSObjectHeader.TS_SOLID_POLY.id().toLowerCase())) {
						polys.add(parseTSSolidPoly(null, scr, index));
						
					}
					else if(polyLine.toLowerCase().equals(TSObjectHeader.TS_SHADED_POLY.id().toLowerCase())) {
						polys.add(parseTSShadedPoly(scr, index));
						
					}
					else if(polyLine.toLowerCase().equals(TSObjectHeader.TS_TEXTURE4_POLY.id().toLowerCase())) {
						polys.add(parseTSTexture4Poly(scr, index));
						
					}
					else if(polyLine.length() == 0 || polyLine.startsWith("//")) {
						continue;
					}
					else if(polyLine.contains("#")) {
						break;
					}
				}
			}
		}
		float[][] verts = new float[verTList.size()][3];
		for(int f=0; f < verTList.size(); f++) {
			verts[f] = new float[3];
			verts[f][0] = verTList.get(f)[0];
			verts[f][1] = verTList.get(f)[1];
			verts[f][2] = verTList.get(f)[2];
		}
		dto.setVertices(verts);
		
		TSSurfaceEntryDTO[] srfArr = new TSSurfaceEntryDTO[surfaces.size()]; 
		dto.setSurfaces(surfaces.toArray(srfArr));
		
		TSObjectDTO[] arr = new TSObjectDTO[polys.size()];
		dto.setPolys(polys.toArray(arr));
		
		int[] idx = new int[index.size()];
		for(int i=0; i < index.size(); i++) {
			idx[i] = index.get(i);
		}
		dto.setIndexes(idx);	
		
		return dto;
	}
	
	
	public TSSurfaceEntryDTO parseSurfaceInfo(String line) {
		
		
		TSSurfaceEntryDTO dto = new TSSurfaceEntryDTO();
		
		String[] v = line.split(" ");
		
		if(v[0].equals("dba")) {
			dto.setFront(dto.newSurfaceColor(Integer.valueOf(v[1]), 0, -1, -1));
			dto.setBack(dto.newSurfaceColor(-1, -1, -1, -1));
		}
		else if(v[0].equals("shd")) {
			dto.setFront(dto.newSurfaceColor(Integer.valueOf(v[1]), 
											Integer.valueOf(v[2]), 
											Integer.valueOf(v[3]), 
											Integer.valueOf(v[4])));
			
			dto.setBack(dto.newSurfaceColor(Integer.valueOf(v[6]), 
											Integer.valueOf(v[7]),
											Integer.valueOf(v[8]), 
											Integer.valueOf(v[9])));
		}
		return dto;
	}
	
	public TSPolyDTO parseTSPoly(TSPolyDTO dto, Scanner scr, List<Integer> vIndex) {

		if(dto == null) {
			dto = new TSPolyDTO();
		}
		
		dto.setNormal(Integer.valueOf(parseKey(scr.nextLine())));
		dto.setCenter(Integer.valueOf(parseKey(scr.nextLine())));
		
		int start = vIndex.size();
		int vrtCount = 0;
		String face = scr.nextLine();
		if(face.startsWith("f ")){
			String[] vIds = face.split(" ");
			for(int i=1; i < vIds.length; i++){
				vIndex.add(Integer.valueOf(vIds[i]) + 1);	//+ 1 for vindex offset, first 2 verts seem to be 'normal' and 'center' somehow.
				vrtCount += 1;
			}
		}
		
		dto.setVertexTotal(vrtCount);
		dto.setVertixListStartNum(start);
		
		dto.setCenter(2);
		dto.setNormal(1);
		
		return dto;
	}
	
	public TSSolidPolyDTO parseTSSolidPoly(TSSolidPolyDTO dto, Scanner scr, List<Integer> vIndex) {
		
		if(dto == null) {
			dto = new TSSolidPolyDTO();
		}
		dto = (TSSolidPolyDTO)parseTSPoly((TSPolyDTO)dto, scr, vIndex);
		
		String c = scr.nextLine();
		dto.setSurfaceNum(Integer.valueOf(parseKey(c)));
		
		return dto;
	}
	
	public TSShadedPolyDTO parseTSShadedPoly(Scanner scr, List<Integer> vIndex) {
		
		TSShadedPolyDTO dto = new TSShadedPolyDTO();
		
		return (TSShadedPolyDTO)parseTSSolidPoly((TSSolidPolyDTO)dto, scr, vIndex);
	}
	
	public TSTexture4PolyDTO parseTSTexture4Poly(Scanner scr, List<Integer> vIndex) {
		
		TSTexture4PolyDTO dto = new TSTexture4PolyDTO();
		
		return (TSTexture4PolyDTO)parseTSSolidPoly((TSSolidPolyDTO)dto, scr, vIndex);
	}
	
	private String parseKey(String line){
		if(line.contains("=")) {
			return line.substring(line.lastIndexOf('=')+1).toLowerCase();	
		}
		return "";
	}
	
	private float[] parseVecString(String line) {
		String[] v = line.split(" ");
		
		float[] vec3 = new float[3];
		vec3[0] = Float.valueOf(v[0]);
		vec3[1] = Float.valueOf(v[1]);
		vec3[2] = Float.valueOf(v[2]);
		
		return vec3;
	}
	
	private Float[] parseVertex(String line) {
		String[] v = line.split(" ");
		
		Float[] vec3 = new Float[3];
		for(int s=1; s < v.length; s++) {
			float f1 = Float.valueOf(v[s]);
			f1 = Math.round(f1);
			vec3[s-1] = f1;
		}
		
		return vec3;
	}
}
