package org.es2tlk.manager.io;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.es2tlk.manager.ES2ModInfo;
import org.hercworks.core.io.transform.ThreeSpaceByteTransformer;
import org.hercworks.transfer.dto.file.TransferObject;
import org.hercworks.transfer.svc.GeneralDTOService;
import org.hercworks.voln.DataFile;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ModFileJsonProcessor {

	private ES2ModInfo modInfo;
	private ObjectMapper objectMapper;
	private boolean logVerbose = false;

	public void init(boolean logVerbose, ES2ModInfo modInfo, ObjectMapper objectMapper) {
		setModInfo(modInfo);
		setObjectMapper(objectMapper);
		setLogVerbose(logVerbose);
	}
	
	public DataFile importJson(String file, ThreeSpaceByteTransformer transformerClass, Class<? extends DataFile> dataClass, GeneralDTOService dtoService, Class<? extends TransferObject> dtoClass) {
		DataFile compiledFile = null;
		try {	
			TransferObject dto = objectMapper.readValue(file, dtoClass);
			
			dtoClass.cast(dto);
			
			if(dto == null) {
				throw new Exception("ERROR - failed to convert [" + file + "] to File Object.");
			}
			
			compiledFile = dtoService.fromDTO(dto);
			compiledFile = dataClass.cast(compiledFile);
		
			((DataFile)compiledFile).setFileName(getCleanFileName(fileNoExt(file)));
			
			//make file name accessible to the transformer
			String fileName = ((DataFile)compiledFile).getFileName();
			String cleanFileName = new String(fileName.substring(0,
					fileName.lastIndexOf(".")));
			
			byte[] data = transformerClass.objectToBytes(compiledFile);
			compiledFile.setRawBytes(data);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return compiledFile;
	}
	
//	public void exportJson(String file, ThreeSpaceByteTransformer transformerClass, Class<? extends DataFile> dataClass, GeneralDTOService dtoService, Class<? extends TransferObject> dtoClass) {
//		try {
//			getLogger().console("--------------------------EXPORTING " + file + " -----------------------------------");
//			
//			DataFile exportDat = transformerClass.bytesToObject(loadFileBytes(getAppPath() + file));
//			exportDat = dataClass.cast(exportDat);
//			
//			if(exportDat == null) {
//				throw new Exception("ERROR - failed to convert [" + file + "] to File Object.");
//			}
//			
//			((DataFile)exportDat).setFileName(getCleanFileName(fileNoExt(file)));
//			
//			String targDirPath = null;
//			if(cmdLine.checkOption(OptionArgs.SRC)) {
//				getLogger().consoleDebug("--keeping DAT export to source directory.");
//				((DataFile)exportDat).assignDir(getAppPath());
//				targDirPath = makeExportPath(getAppPath() + fileNoExt(file));
//			}
//			else {
//				targDirPath = makeExportPath(this.unpackPath + File.separator + ((DataFile)exportDat).getDir().name() + File.separator);
//			}
//			
//			if(targDirPath == null) {
//				throw new IOException("ERROR - target dir path was null.\n rootPath=[" + getAppPath() + "]");
//			}
//			
//			File targDir = new File(targDirPath);
//			if(!targDir.exists()) {
//				if(!targDir.mkdir()) {
//					getLogger().console("Error: could not make dir [" + targDirPath + "], exiting process");
//					return;
//				}
//			}
//
//			Object dto = dtoService.convertToDTO(((DataFile)exportDat));
//			
//			dto = dtoClass.cast(dto);
//			
//			((TransferObject)dto).setFileName(((DataFile)exportDat).getFileName());
//			((TransferObject)dto).setDir(((DataFile)exportDat).getDir().val());
//			((TransferObject)dto).setFileExt(((DataFile)exportDat).getExt().val());
//			
//			String fullExportPath = targDirPath + File.separator + ((DataFile)exportDat).getFileName()  + ".json";
//			
//			File json = new File(fullExportPath);
//			json.setWritable(true);
//			objectMapper.writeValue(json, dto);
//			
//			getLogger().console("+ Write complete to [" + fullExportPath +"]");
//			
//		} catch(Exception e) {
//			e.printStackTrace();
//			return;
//		}
//		
//		getLogger().console("------------------------------------------------------------------------------------");
//	}
	
	protected String getCleanFileName(String fileNamePath) {
		int dirIdx = fileNamePath.lastIndexOf('\\');
		if(dirIdx <= 0) {
			dirIdx = fileNamePath.lastIndexOf('/');
		}
		if(dirIdx <= 0) {
			dirIdx = 0;
		}
		else {
			dirIdx += 1;
		}
		return  fileNamePath.substring(dirIdx);
	}
	protected String fileNoExt(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}
	
	protected byte[] loadFileBytes(String path) {
		
		byte[] data = null;
		
		try(FileInputStream fizz = new FileInputStream(path)){
			ByteArrayOutputStream bizz = new ByteArrayOutputStream();
			
			bizz.write(fizz.readAllBytes());
			data = bizz.toByteArray();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return data;
	}
	
	public ES2ModInfo getModInfo() {
		return modInfo;
	}

	public void setModInfo(ES2ModInfo modInfo) {
		this.modInfo = modInfo;
	}

	public boolean isLogVerbose() {
		return logVerbose;
	}

	public void setLogVerbose(boolean logVerbose) {
		this.logVerbose = logVerbose;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
}
