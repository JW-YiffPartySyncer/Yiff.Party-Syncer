package Logic;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import Logic.Workers.WorkerDownloader;
import Logic.Workers.WorkerPNGConverter;

/**
 * 
 * @author JW
 *
 *         Utility class that holds some misc stuff
 *
 */
public class OUtil {

	public static Connection connectToMysql(String host, String database, String user, String passwd) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			String connectionCommand = "jdbc:mysql://" + host + "/" + database + "?user=" + user + "&password=" + passwd;
			Connection connection = DriverManager.getConnection(connectionCommand);
			return connection;

		} catch (Exception ex) {
			System.out.println("false");
			return null;
		}
	}

	public static void openInBrowser(String strLink) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) && !strLink.equals("")) {
			try {
				Desktop.getDesktop().browse(new URI(strLink));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	public static void openInExplorer(String strPath) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) && !strPath.equals("")) {
			try {
				Desktop.getDesktop().open(new File(strPath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean move(File sourceFile, File destFile) {
		if (sourceFile.isDirectory()) {
			for (File file : sourceFile.listFiles()) {
				move(file, new File(destFile.getAbsolutePath() + "\\" + file.getName()));
			}
		} else {
			try {
				System.out.println("move file " + sourceFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());
				destFile.getParentFile().mkdirs();
				Files.move(Paths.get(sourceFile.getPath()), Paths.get(destFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public static void unzip7zSameDir(File oFile, boolean bConvert, WorkerPNGConverter convertWorker) throws IOException, Exception {
		unzip7z(oFile.getAbsolutePath(), oFile.getParentFile().getAbsolutePath(), bConvert, convertWorker);
	}

	/**
	 * Extracts Archive from strArchive to strDestination with path =
	 * strDestination/strArchive.substring(0, ".")
	 * 
	 * @param strArchive
	 * @param strDestination
	 * @throws IOException, Exception
	 */
	public static void unzip7z(String strArchive, String strDestination, boolean bConvert, WorkerPNGConverter convertWorker) throws IOException, Exception {
		File oArchive = new File(strArchive);
		File oDestination = new File(strDestination, oArchive.getName().substring(0, oArchive.getName().lastIndexOf(".")));
		if (oDestination.exists()) {
			oDestination.mkdirs();
			oDestination.mkdir();
		}
		try (SevenZFile sevenZFile = new SevenZFile(oArchive)) {
			SevenZArchiveEntry entry;
			while ((entry = sevenZFile.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					continue;
				}
				File file = fileName(oDestination, entry);
				if (file == null) {
					continue;
				}
				File parent = file.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					System.out.println("FailOpenFile" + ":" + file);
					continue;
				}

				

				boolean bFound = false;
				if (!file.exists()) {
					if (file.getName().contains(".")) {
						if (file.getName().substring(file.getName().lastIndexOf('.') + 1).equalsIgnoreCase("png")) {
							File toCheck = new File(file.getParentFile().getAbsolutePath(), file.getName() + ".jpg");
							if (toCheck.exists()) {
								bFound = true;
							}
						}
					}
					if (!bFound) {
						try (FileOutputStream out = new FileOutputStream(file)) {
							int length;
							byte[] buf = new byte[1024];
							while ((length = sevenZFile.read(buf)) != -1) {
								out.write(buf, 0, length);
							}
							out.close();
						}
					}

				}
				if (file.getName().contains(".") && !bFound) {
					if (file.getName().substring(file.getName().lastIndexOf('.') + 1).equalsIgnoreCase("png") && bConvert) {
						try {
							convertWorker.convert(file.getAbsolutePath());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}
		}
	}

	public static void unzipSameDir(File oFile, boolean convertPNG, WorkerPNGConverter convertWorker) throws IOException, IllegalArgumentException {
		File destDir = new File(
				oFile.getParentFile().getAbsolutePath() + "\\" + oFile.getName().substring(isNumeric(oFile.getName().substring(0, 12)) ? 12 : 0, oFile.getName().lastIndexOf('.')));
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(oFile.getAbsolutePath()));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			File newFile = newFile(destDir, zipEntry);
			if (newFile.exists() && !newFile.isDirectory() && zipEntry.isDirectory()) {
				newFile.delete();
			}
			boolean bFound = false;
			if (!newFile.exists()) {
				newFile.getParentFile().mkdirs();
				if (zipEntry.isDirectory()) {
					newFile.mkdir();
				} else {
					if (newFile.getName().contains(".")) {
						if (newFile.getName().substring(newFile.getName().lastIndexOf('.') + 1).equalsIgnoreCase("png")) {
							File toCheck = new File(newFile.getParentFile().getAbsolutePath(), newFile.getName() + ".jpg");
							if (toCheck.exists()) {
								bFound = true;
							}
						}
					}
					if (!bFound) {
						FileOutputStream fos = new FileOutputStream(newFile);
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						fos.close();
					}
				}
			}
			if (newFile.getName().contains(".") && !bFound) {
				if (newFile.getName().substring(newFile.getName().lastIndexOf('.') + 1).equalsIgnoreCase("png") && convertPNG) {
					try {
						convertWorker.convert(newFile.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}

	private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	private static File fileName(File destinationDir, ArchiveEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}