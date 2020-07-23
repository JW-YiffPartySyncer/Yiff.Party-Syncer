package Logic.Workers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import UI.Main;

public class WorkerPNGConverter implements Runnable {

	private Main oMain;
	private LinkedBlockingQueue<File> aConvertQueue;
	
	public boolean bWorking = false;

	public WorkerPNGConverter(Main oMain, LinkedBlockingQueue<File> aConvertQueue) {
		this.oMain = oMain;
		this.aConvertQueue = aConvertQueue;
	}

	@Override
	public void run() {
		while(true) {
			bWorking = false;
			try {
				File oFile = aConvertQueue.take();
				bWorking = true;
				try {
					convert(oFile.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Convert a .png to .jpg. TODO: This routine uses *MUCH* RAM. app crashes with
	 * OoM when converting big PNGs (10000x10000 and bigger, like seriously
	 * unreasonably big) and -xmx is set to lower than 4G. So be on the safe side,
	 * always launch with 8G as maximum ram. Converts the PNG at the same location,
	 * will simply create a new file with [FILENAME].jpg
	 * 
	 * @param strPath - Absolute path to the PNG that needs to be converted
	 * @return Boolean whether file has successfully been converted.
	 */
	public boolean convert(String strPath) {
		File input = new File(strPath);
		File output = new File(strPath + ".jpg");
		if (output.exists()) {
			output.delete();
		}
		try {
			System.out.println("Starting conversion on " + strPath);
			output.createNewFile();

			BufferedImage image = ImageIO.read(input);
			BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			result.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
			image.flush();
			// ImageIO.write(result, "jpg", output);

			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
			ImageWriter writer = iter.next();
			ImageWriteParam iwp = writer.getDefaultWriteParam();
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwp.setCompressionQuality(oMain.oConf.fDLWJPGQuality);
			ImageOutputStream ios = ImageIO.createImageOutputStream(output);
			writer.setOutput(ios);
			writer.write(null, new IIOImage(result, null, null), iwp);
			writer.dispose();
			ios.close();
			result.flush();

			input.delete();
			System.out.println("Converted " + output.getAbsolutePath());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			output.delete();
			return false;
		}
	}

}
