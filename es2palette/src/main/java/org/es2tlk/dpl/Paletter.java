package org.es2tlk.dpl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.hercworks.core.data.file.dyn.DynamixPalette;
import org.hercworks.core.data.struct.ColorBytes;
import org.hercworks.core.io.transform.common.DynamixPaletteTransformer;

public class Paletter {

	private static String argExport = "-e";
	private static String argImport = "-i";
	private static String argDir = "-d";
	private static String argGimp = "-gpl";	//export to gimp GPL
	
	public static void main(String[] args) {
		
		String exportDir = null;
		String exportDplPath = null;
		boolean exportGPL = false;
		
		for(int i = 0; i < args.length; i++) {
			String arg = args[i];
			
			if(arg.toLowerCase().equals(argDir)) {
				exportDir = loadArg(args, i+1);
			}
			else if(arg.toLowerCase().equals(argExport)) {
				exportDplPath = loadArg(args, i+1);
			}
			else if(arg.toLowerCase().equals(argGimp)) {
				exportGPL = true;
			}
		}
		
		if(exportDir == null || exportDir.length() == 0) {
			System.out.println("--->ERROR! export dir empty!");
			System.exit(1);
		}
		if(exportDplPath == null || exportDplPath.length() == 0) {
			System.out.println("--->ERROR! .DPL file path empty!");
			System.exit(1);
		}
		
		File exportPath = new File(exportDir);
		if(!exportPath.exists() || !exportPath.isDirectory()) {
			System.out.println("--->ERROR! export path doesn't exist or is not a directory.\n [" + exportPath + "].");
			System.exit(2);
		}
		
		File dplFile = new File(exportDplPath);
		if(!dplFile.exists() || !dplFile.isFile()) {
			System.out.println("--->ERROR! dpl file doesn't exist or is not a file.\n [" + exportDplPath + "].");
			System.exit(2);
		}
		
		DynamixPalette dpl = null;
		
		try(FileInputStream fizz = new FileInputStream(dplFile)){
			DynamixPaletteTransformer dplTransform = new DynamixPaletteTransformer();
			dpl = (DynamixPalette) dplTransform.bytesToObject(fizz.readAllBytes());
			fizz.close();
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		
		if(dpl == null || dpl.getColors().isEmpty()) {
			System.out.println("--->ERROR! problem processing palette file.");
			System.exit(3);
		}
		
		dpl.setFileName(dplFile.getName().substring(0, dplFile.getName().lastIndexOf('.')));
		
		
		BufferedImage paletteImage = generatePaletteImage(dpl);
		
		boolean writeIamge = false;
		
		File file = new File(exportDir + File.separator + dpl.getFileName() + ".png");
		try {
			writeIamge = ImageIO.write(paletteImage, "png", file);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}

		//Generate a raw text file for review.
		String gplFileName = "ES2-" + dpl.getFileName().toUpperCase();
		File rawFile = new File(exportDir + File.separator + gplFileName + ".txt");
		
		try {
			rawFile.createNewFile();
			FileWriter writer = new FileWriter(rawFile);
			
			writer.write("Dynamix Palette List\n");
			writer.write("Name: ");
			writer.write(gplFileName);
			writer.write("\n");
			
			writer.write("Columns: 16\n");
			writer.write("#\n");
			
			for(Integer idx : dpl.getColors().keySet()) {
				ColorBytes clrByt = dpl.getColors().get(idx);
				
				
				StringBuilder row = new StringBuilder();
				row.append("#");
				row.append(idx);
				row.append(" = ");
				
				row = colorString(row, Byte.toUnsignedInt(clrByt.getArray()[0]));
				row.append(' ');
				row = colorString(row, Byte.toUnsignedInt(clrByt.getArray()[1]));
				row.append(' ');
				row = colorString(row, Byte.toUnsignedInt(clrByt.getArray()[2]));
				row.append(' ');
				row = colorString(row, Byte.toUnsignedInt(clrByt.getArray()[3]));
				
				row.append('\n');
				
				writer.write(row.toString());
			}
			writer.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//export GIMP GPL Palette
		if(exportGPL) {
			File gplFile = new File(exportDir + File.separator + gplFileName + ".gpl");

			try {
				gplFile.createNewFile();
				FileWriter writer = new FileWriter(gplFile);
				
				writer.write("GIMP Palette\n");
				writer.write("Name: ");
				writer.write(gplFileName);
				writer.write("\n");
				
				writer.write("Columns: 16\n");
				writer.write("#\n");
				
				for(Integer idx : dpl.getColors().keySet()) {
					ColorBytes clrByt = dpl.getColors().get(idx);
					StringBuilder row = new StringBuilder();
					row = colorToRow(row, clrByt.getJavaColor());
					row.append(" #");
					row.append(idx);
					row.append('\n');
					
					writer.write(row.toString());
				}
				writer.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static BufferedImage generatePaletteImage(DynamixPalette dpl) {
		
		int swatchW = 16; 
		int swatchH = 16;
		
		IndexColorModel colorIndex = new IndexColorModel(8,
				256,
				dpl.toIntColorMap(),
				0,
				false,
				-1,
				DataBuffer.TYPE_BYTE
		);
		
		BufferedImage paletteImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		
		int grdW = paletteImage.getWidth() / swatchW - 1;
		int grdH = paletteImage.getHeight() / swatchH - 1;
		
		int row = 0;
		int col = 0;
		for(int shade : dpl.getColors().keySet()) {
			for(int y=0; y < swatchH; y++) {
				for(int x=0; x < swatchW; x++) {
					paletteImage.setRGB( 
								x + (col * swatchW), 
								y + (row * swatchH),
								dpl.getColors().get(shade).getJavaColor().getRGB());
				}
			}
			if(col < grdW) {
				col++;
			}
			else {
				col = 0;
				if(row < grdH) {
					row++;	
				}
				
			}
		}
		
		return paletteImage;
		
	}
	
	
	private static StringBuilder colorToRow(StringBuilder row, Color color) {
		
		row = colorString(row, color.getRed());
		row.append(' ');
		row = colorString(row, color.getGreen());
		row.append(' ');
		row = colorString(row, color.getBlue());
		
		return row;
	}
	
	private static StringBuilder colorString(StringBuilder row, int value) {
		
		if(value < 100) {
			if(value < 10) {
				row.append(" ");
			}
			row.append(" ");
		}
		row.append(String.valueOf(value));
		
		return row;
	}
	
	private static String loadArg(String[] args, int index) {
		if (index <= args.length - 1) {
			return args[index];
		}
		return null;
	}

}
