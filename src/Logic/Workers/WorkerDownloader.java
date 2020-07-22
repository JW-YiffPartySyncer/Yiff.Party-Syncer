package Logic.Workers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import Lib.mega.Mega;
import Lib.mega.MegaSession;
import Lib.mega.auth.MegaAuthCredentials;
import Logic.DownloadObject;
import Logic.OUtil;
import UI.Main;

/**
 * 
 * @author JW
 * 
 *         This is just a download worker :)
 */
public class WorkerDownloader implements Runnable {

	private WorkerDownloadManager oManager = null;

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;

	public String strStatus = "NULL";
	public long lTimestamp = System.currentTimeMillis();

	private boolean bRunning = false;
	public boolean bWorking = true;

	private Main oMain;

	private Random r = new Random();

	public WorkerDownloader(WorkerDownloadManager oM, Main oMain) {
		oManager = oM;
		this.oMain = oMain;
	}

	/**
	 * Main thread loop
	 */
	@Override
	public void run() {
		// we need to set a user agent cause without it we get an error when we try to
		// connect to yiff.party. I googled this string.... maybe set to something more
		// reasonable?
		System.setProperty("http.agent", "Mozilla/5.0 (Linux; Android 4.2. 2; en-us; SAMSUNG SGH-M919 Build/JDQ39)");
		connection = OUtil.connectToMysql(oMain.oConf.strDBHost, oMain.oConf.strDBDatabase, oMain.oConf.strDBUser, oMain.oConf.strDBPassword);
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		bRunning = true;
		boolean bConverted = false;
		while (bRunning) {
			bConverted = false;
			DownloadObject DO = null;
			strStatus = "Idle";
			lTimestamp = System.currentTimeMillis();
			// Retrieve a DonloadObject from the DownloadManager LinkedBlockingQueue
			try {
				DO = oManager.aQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (DO != null) {
				strStatus = DO.strURL;
				lTimestamp = System.currentTimeMillis();
				boolean success = true;
				boolean fnf = false;
				System.out.println("WorkerDownloader: starting on " + DO.ID + " | " + DO.strURL);
				DO.touched = true;

				// Check if URL points to mega.nz
				if (DO.strURL.contains("mega.nz/")) {
					if (oMain.oConf.bDLWMega) {
						megaDownload(DO);
					}
				} else {
					URL website;
					// File oFile = new File(DO.strPath);
					File oFile = new File(oMain.oConf.strSavepath + "Temp\\" + DO.strName);
					if (oFile.exists()) {
						oFile.delete();
					}
					try {
						// Try to download the File.
						website = new URL(DO.strURL);
						int iFilesize = getFileSize(website);
						URLConnection conn = website.openConnection();
						conn.setReadTimeout(1000 * 30);
						conn.setConnectTimeout(1000 * 15);
						ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
						FileOutputStream fos = new FileOutputStream(oFile);
						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
						fos.close();
						// If the downloaded filesize doesn't match the reported filesize from the
						// webserver, mark download as failed and retry later
						if (iFilesize != 0) {
							if (oFile.length() != iFilesize) {
								success = false;
							}
						}
						if (success && !DO.invalid) {
							// If the downloaded file is a PNG, convert it to high quality JPG. Cause PNGs
							// are *big*
							if (oMain.oConf.bDLWConvertPNGs) {
								if (DO.strName.substring(DO.strName.lastIndexOf('.') + 1).equalsIgnoreCase("png")) {
									success = convert(oFile.getAbsolutePath());
									if (success) {
										oFile = new File(oFile.getAbsolutePath() + ".jpg");
										bConverted = true;
									}
								}
							}
							if (success && !DO.invalid) {
								File oDest = new File(DO.strPath + (bConverted ? ".jpg" : ""));
								try {
									System.out.println("Copy from " + oFile.getAbsolutePath() + " to " + oDest.getAbsolutePath());
									oDest.getParentFile().mkdirs();
									Files.copy(Paths.get(oFile.getPath()), Paths.get(oDest.getPath()), StandardCopyOption.REPLACE_EXISTING);
									updatePost(DO, true, false);
									// Auto-unzip
									if (oMain.oConf.bDLWAutoUnzip) {
										if (DO.strName.substring(DO.strName.lastIndexOf('.') + 1).equalsIgnoreCase("zip")) {
											try {
												System.out.println("Trying to unzip " + oDest.getAbsolutePath());
												OUtil.unzipSameDir(oDest);
												System.out.println("Unzip successfull of " + oDest.getAbsolutePath());
											} catch (Exception e2) {
												e2.printStackTrace();
											}
										}
									}
									System.out.println("WorkerDownloader: finished " + DO.ID);
								} catch (IOException ioe) {
									success = false;
									ioe.printStackTrace();
								}
							}
						}
						oFile.delete();
					} catch (FileNotFoundException fnfe) {
						// This exception gets called when yiff.party sends a 404 error. This is most
						// likely the case when the creator got excluded from yiffparty, hence we mark
						// it as excluded in the DB so we don't retry in the future
						fnfe.printStackTrace();
						success = false;
						fnf = true;
					} catch (Exception e) {
						e.printStackTrace();
						success = false;
					}
				}
				if (!success) {
					// Shit's hit the fan, clean up the mess.
					updatePost(DO, false, fnf);
					System.out
							.println("WorkerDownloader: failed " + DO.ID + ", Timeout " + oMain.oConf.iDLWFailTimeout + " second " + (oMain.oConf.iDLWFailTimeout == 1 ? "" : "s"));
					try {
						Thread.sleep(1000 * oMain.oConf.iDLWFailTimeout);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				strStatus = "Finished";
				lTimestamp = System.currentTimeMillis();
				DO.finished = true;
			}
		}
		bWorking = false;
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
	private boolean convert(String strPath) {
		File input = new File(strPath);
		File output = new File(strPath + ".jpg");
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

	/**
	 * Update yiffparty.posts on finish
	 * 
	 * @param DO           - DownloadObject that we worked on
	 * @param success      - whether we successfully finished all tasks
	 * @param fileNotFound - If the webserver told us 404, we mark that in the DB so
	 *                     we don't try again in the future
	 */
	private void updatePost(DownloadObject DO, boolean success, boolean fileNotFound) {
		try {
			statement.executeUpdate("UPDATE posts SET downloaded = " + (success ? "TRUE" : (fileNotFound ? "2" : "FALSE")) + ", last_checked = " + System.currentTimeMillis()
					+ " WHERE ID = " + DO.ID);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Try to get the filesize from the webserver
	 * 
	 * @param url - URL to the download file
	 * @return integer with the filesize of url
	 */
	private int getFileSize(URL url) {
		URLConnection conn = null;
		try {
			conn = url.openConnection();
			conn.setConnectTimeout(1000 * 30);
			conn.setReadTimeout(1000 * 15);
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).setRequestMethod("HEAD");
			}
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).disconnect();
			}
		}
	}

	private void megaDownload(DownloadObject DO) {
		strStatus = "Mega Subroutine: " + DO.strURL;
		System.out.println("Trying to download file from mega.nz: " + DO.strURL);
		File oTempFolder = new File(oMain.oConf.strSavepath + "Temp\\" + getRandomString());
		int i = 0;
		while (i < 10 && oTempFolder.exists()) {
			oTempFolder = new File(oMain.oConf.strSavepath + "Temp\\" + getRandomString());
			i++;
		}
		oTempFolder.mkdir();
		MegaSession megaSession = Mega.login(new MegaAuthCredentials(oMain.oConf.strMegaUser, oMain.oConf.strMegaPW));
		boolean bSuccess = true;
		try {
			megaSession.get(DO.strURL, oTempFolder.getAbsolutePath()).run();
		} catch (Exception e) {
			e.printStackTrace();
			bSuccess = false;
			updatePost(DO, false, false);
		}

		if (bSuccess) {
			if (oMain.oConf.bDLWAutoUnzip) {
				WorkerLocalUnzip oW = new WorkerLocalUnzip(oMain, null);
				oW.recurWork(oTempFolder);
			}
			if (oMain.oConf.bDLWConvertPNGs) {
				recurPNGConvert(oTempFolder);
			}
			File oDest = new File(DO.strPath);
			oDest.getParentFile().mkdirs();
			try {
				OUtil.move(oTempFolder, oDest.getParentFile());
				updatePost(DO, true, false);
			} catch (Exception e) {
				e.printStackTrace();
				updatePost(DO, false, false);
			}
		}
		try {
			megaSession.logout();
		} catch (Exception er) {
			er.printStackTrace();
		}

		if (oTempFolder.exists()) {
			oTempFolder.delete();
		}
		return;
	}

	/**
	 * Recursively sift through directories to look for PNGs to convert
	 * 
	 * @param oFile - The Folder/File we want to convert
	 */
	private void recurPNGConvert(File oFile) {
		if (oFile.isDirectory()) {
			for (File oTarget : oFile.listFiles()) {
				recurPNGConvert(oTarget);
			}
		} else {
			if (oFile.getName().substring(oFile.getName().lastIndexOf('.') + 1).equalsIgnoreCase("png")) {
				convert(oFile.getAbsolutePath());
			}
		}
	}

	/**
	 * Creates a random String containing 10 numbers
	 * 
	 * @return String - the random String
	 */
	private String getRandomString() {
		String strResult = "";
		for (int i = 0; i < 10; i++) {
			strResult += r.nextInt(9) + "";
		}
		return strResult;
	}

	/**
	 * Set bRunning to false to stop work after current download
	 */
	public void stop() {
		bRunning = false;
	}
}
