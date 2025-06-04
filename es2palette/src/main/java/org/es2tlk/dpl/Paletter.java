package org.es2tlk.dpl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.es2tlk.CmdArgs;
import org.hercworks.core.data.file.dyn.DynamixPalette;
import org.hercworks.core.data.struct.ColorBytes;
import org.hercworks.core.io.transform.common.DynamixPaletteTransformer;

public class Paletter {
	
	public static void main(String[] args) {
		
		String exportDir = null;
		String exportDplPath = null;
		boolean exportGPL = false;
		boolean index0Alpha = false;
		
		for(int i = 0; i < args.length; i++) {
			String arg = args[i];
			
			if(arg.toLowerCase().equals(CmdArgs.Dir.val())) {
				exportDir = loadArg(args, i+1);
			}
			else if(arg.toLowerCase().equals(CmdArgs.File.val())) {
				exportDplPath = loadArg(args, i+1);
			}
			else if(arg.toLowerCase().equals(CmdArgs.GPLpalette.val())) {
				exportGPL = true;
			}
			else if(arg.toLowerCase().equals(CmdArgs.Alpha.val())) {
				index0Alpha = true;
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
		
		BufferedImage paletteImage = generatePaletteImage(dpl, index0Alpha);
		
		boolean writeImage = false;
		
		File file = new File(exportDir + File.separator + dpl.getFileName() + ".png");
		try {
			writeImage = ImageIO.write(paletteImage, "png", file);
		} 
		catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		
		if(!writeImage) {
			System.out.println("ERROR: failed to write palette image.");
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
				ColorBytes clrByt = null;
				if(idx == 0 && index0Alpha) {
					clrByt = dpl.getIndex0AlphaKey();
				}
				else{
					clrByt = dpl.getColors().get(idx);
				}
				
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
			System.err.println("---> ERROR " + e.getLocalizedMessage());
		} catch (IOException e) {
			System.err.println("---> ERROR " + e.getLocalizedMessage());
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
					ColorBytes clrByt = null;
					if(idx == 0 && index0Alpha) {
						clrByt = dpl.getIndex0AlphaKey();
					}
					else{
						clrByt = dpl.getColors().get(idx);
					}
					
					StringBuilder row = new StringBuilder();
					row = colorToRow(row, clrByt.getJavaColor());
					row.append(" #");
					row.append(idx);
					row.append('\n');
					
					writer.write(row.toString());
				}
				writer.close();
				
			} catch (FileNotFoundException e) {
				System.err.println("---> ERROR " + e.getLocalizedMessage());
			} catch (IOException e) {
				System.err.println("---> ERROR " + e.getLocalizedMessage());
			}
		}
		System.out.println("-- Write complete --");
	}
	
	private static BufferedImage generatePaletteImage(DynamixPalette dpl, boolean isIndex0Alpha) {
		
		int swatchW = 16; 
		int swatchH = 16;
		
//		IndexColorModel colorIndex = new IndexColorModel(8,
//				256,
//				dpl.toIntColorMap(),
//				0,
//				false,
//				-1,
//				DataBuffer.TYPE_BYTE
//		);
		
		BufferedImage paletteImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		
		int grdW = paletteImage.getWidth() / swatchW - 1;
		int grdH = paletteImage.getHeight() / swatchH - 1;
		
		int row = 0;
		int col = 0;
		for(int index : dpl.getColors().keySet()) {
			for(int y=0; y < swatchH; y++) {
				for(int x=0; x < swatchW; x++) {
					int colorVal = 0;
					if(index == 0 && isIndex0Alpha) {
						colorVal = dpl.getIndex0AlphaKey().getJavaColor().getRGB();
					}
					else{
						colorVal = dpl.getColors().get(index).getJavaColor().getRGB();
					}
					
					paletteImage.setRGB( 
								x + (col * swatchW), 
								y + (row * swatchH),
								colorVal);
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
