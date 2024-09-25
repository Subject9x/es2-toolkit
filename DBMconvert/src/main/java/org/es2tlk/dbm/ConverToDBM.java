package org.es2tlk.dbm;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.hercworks.core.data.file.dyn.DynamixBitmap;
import org.hercworks.core.data.file.dyn.DynamixPalette;
import org.hercworks.core.data.struct.ColorBytes;
import org.hercworks.core.io.transform.common.DynamixBitmapTransformer;
import org.hercworks.core.io.transform.common.DynamixPaletteTransformer;
import org.hercworks.voln.FileType;

import at.favre.lib.bytes.Bytes;

public class ConverToDBM {
	
	public static List<String> params = Arrays.asList("-h", "-d", "-p", "-f");
	
	public static void main(String[] args) {
		
		String exportDir = null;
		String dplPath = null;
		String fileFrag = null;
		
		System.out.println("1. Tool will convert .PNG with alpha to .DBM.");
		System.out.println("2. Required directory must have source .PNG.");
		System.out.println("3. Directory also destination for output .DBM.");
		System.out.println("4. -F is file name fragment, will scan for this.");
		
		for(int i = 0; i < args.length; i++) {
			String arg = args[i];
			
			if(arg.toLowerCase().equals("-d")) {
				exportDir = loadArg(args, i+1);
			}
			else if(arg.toLowerCase().equals("-p")) {
				dplPath = loadArg(args, i+1);
			}
			else if(arg.toLowerCase().equals("-f")) {
				fileFrag = loadArg(args, i+1);
			}
		}
		
		if(exportDir == null) {
			System.out.println("--->ERROR! no dir arg -d found.");
			System.exit(1);
		}
		if(dplPath == null) {
			System.out.println("--->ERROR! no palette arg -p found.");
			System.exit(1);
		}
		if(fileFrag == null) {
			System.out.println("--->ERROR! no file name arg -f found.");
			System.exit(1);
		}
		
		File workingDir = new File(exportDir);
		if(!workingDir.exists()) {
			System.out.println("--->ERROR! directory doesn't exist. \n [" +  workingDir + "].");
			System.exit(2);
		}
		if(!workingDir.isDirectory()) {
			System.out.println("--->ERROR! path isn't a directory. \n [" +  workingDir + "].");
			System.exit(2);
		}
		File[] scan = workingDir.listFiles();
		List<File> found = new ArrayList<File>();
		
		for(File f : scan) {
			if(!f.getName().toLowerCase().contains(".png".toLowerCase())) {
				continue;
			}
			if(!f.getName().contains(fileFrag)) {
				continue;
			}
			found.add(f);
		}
		
		
		if(found.size() <= 0) {
			System.out.println("--->ERROR! no files found for schema:" + fileFrag);
			System.exit(5);
		}
		
		System.out.println("Found " + found.size() + " file(s).");
		
		File dplFile = new File(dplPath);
		if(!dplFile.exists()) {
			System.out.println("--->ERROR! DPL file not found. \n [" +  dplPath + "].");
			System.exit(3);
		}
		if(!dplFile.isFile()) {
			System.out.println("--->ERROR! DPL wasn't a file. \n [" +  dplPath + "].");
			System.exit(3);
		}
		
		DynamixPalette dpl = null;
		
		try(FileInputStream fizz = new FileInputStream(dplFile)){

			DynamixPaletteTransformer dplTransform = new DynamixPaletteTransformer(4);
			dpl = (DynamixPalette) dplTransform.bytesToObject(fizz.readAllBytes());
			fizz.close();
			
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			System.exit(4);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(4);
		}
		
		if(dpl == null) {
			System.out.println("--->ERROR! problem processing DPL file.");
			System.exit(5);
		}
		
		try {
			for(File f : found) {
				DynamixBitmap dbm = convertImage(f, dpl);
				if(dbm != null) {
					
					dbm.setImageDataLen(dbm.getImageData().length());
					
					File writeDBM = new File(f.getAbsolutePath().substring(0, 
								f.getAbsolutePath().toLowerCase().lastIndexOf(".png")) + "." + FileType.DBM.val());
					DynamixBitmapTransformer dbmTransform = new DynamixBitmapTransformer();
					FileOutputStream fos = new FileOutputStream(writeDBM);
					fos.write(dbmTransform.objectToBytes(dbm));
					fos.close();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(6);
		}
		
	}
	
	public static DynamixBitmap convertImage(File imgPath, DynamixPalette dpl) throws IOException {
		
		BufferedImage targImage = ImageIO.read(imgPath);
		System.out.println(imgPath.getName());//debug
		if(targImage != null) {
			DynamixBitmap dbm = new DynamixBitmap();
			dbm.setExt(FileType.DBM);
			dbm.setDir(FileType.DBM);
			
			dbm.setRows((short)targImage.getHeight());
			dbm.setCols((short)targImage.getWidth());
			dbm.setBitDepth((short)8);
			
			byte[] rasterData = new byte[(targImage.getHeight() * targImage.getWidth())];
			
			int i = 0;
			for(int r=0; r < dbm.getRows(); r++) {
				for(int c=0; c < dbm.getCols(); c++) {
					if(i >= rasterData.length) {
						break;
					}
					int cell = (r * dbm.getCols()) + c;
					int[] color = targImage.getRaster().getPixel(c, r, new int[4]);
					color[3] = color[3] == 255 ? 1 : 0; //condense alpha back to 1 byte
	
					
					color[0] = color[0] & 0xff;
					color[1] = color[1] & 0xff;
					color[2] = color[2] & 0xff;
					color[3] = color[3] & 0xff;
					
					int index = findNearestColorIndex(dpl, color);
					rasterData[cell] = (byte)index;
					
					
					String clrArr = "[";
					for(int b : color) {
						clrArr += b + ",";
					}
					clrArr += "]";
					System.out.println(clrArr + "=" + index);
					
					i++;
				}
			}
			dbm.setImageData(Bytes.from(rasterData));
			return dbm;
		}
		
		return null;
	}
	
	private static double calculateDistance(Color c1, Color c2) {
		int rDiff = c1.getRed() - c2.getRed();
		int gDiff = c1.getGreen() - c2.getGreen();
		int bDiff = c1.getBlue() - c2.getBlue();
		return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
	}

	private static int findNearestColorIndex(DynamixPalette dpl, int[] color) {

		Color checkColor = new Color(color[0], color[1], color[2], color[3]);

		int index = 0;

		double drift = Double.MAX_VALUE;
		for (int k : dpl.getColors().keySet()) {
			Color dplColor = dpl.getColors().get(k).getJavaColor();
			double delta = calculateDistance(dplColor, checkColor);
			if (delta < drift) {
				drift = delta;
				index = k;
			}
		}
		return index;
	}

	private static String loadArg(String[] args, int index) {
		if (index <= args.length - 1) {
			return args[index];
		}
		return null;
	}

}
