package Logic.Workers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JLabel;

import Logic.OUtil;
import UI.Category;
import UI.Main;

/**
 * 
 * @author JW
 * 
 *         This worker thread gets spawned when a Patreon Relocation Job is
 *         queued. The reason for a separate thread is simple, so we can
 *         continue using the UI while the patreon folder gets relocated on disk
 *
 */
public class WorkerRelocater implements Runnable {

	private String strSource;
	private String strDestination;
	private String strName = "";
	private int iCategory;
	private int iPatreonID;

	private JLabel oUpdateLabel = null;

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;
	private Main oMain;
	private Category oCategory;

	private int mode;

	private int copiedFiles = 0;
	private int totalFiles = 0;

	/**
	 * Creater a new relocation worker.
	 * 
	 * @param strSource      - Absolute path to the source file/folder
	 * @param strDestination - Absolute path to the destination file/folder
	 * @param iCategory      - Category ID of the destination category
	 * @param iPatreonID     - Patreon ID in yiffparty.patreons
	 * @param oUpdateLabel   - JLabel of the source frame, on which we can print
	 *                       update information
	 * @param oMain          - Main thread. Used for pulling config
	 * @param oCategory      - Category UI Object. Can be null if mode != 1
	 * @param mode           - Mode. 1 = CategoryUI
	 */
	public WorkerRelocater(String strSource, String strDestination, int iCategory, int iPatreonID, JLabel oUpdateLabel, Main oMain, Category oCategory, int mode) {
		this.strSource = strSource;
		this.strDestination = strDestination;
		this.oUpdateLabel = oUpdateLabel;
		this.oMain = oMain;
		this.iCategory = iCategory;
		this.iPatreonID = iPatreonID;
		this.oCategory = oCategory;
		this.mode = mode;
		connection = OUtil.connectToMysql(oMain.oConf.strDBHost, oMain.oConf.strDBDatabase, oMain.oConf.strDBUser, oMain.oConf.strDBPassword);
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if (mode == 1) {
			categoryRelocate();
			categoryRelocate(); // Do stuff double, maybe a file got downloaded to the source path before we
								// update the database?
			// TODO: Make this download safe. Maybe flag in DB that we set first and
			// invalidate all downloads?
		}
	}

	/**
	 * Gets called when mode == 1. Contains logic to relocate a folder on disk to a
	 * new location based on category, and on success, will update the DB with the
	 * new location.
	 */
	private void categoryRelocate() {
		File oSource = new File(strSource);
		File oDest = new File(strDestination);
		strName = oSource.getName();
		boolean bSuccess = false;
		if (oSource.exists()) {
			try {
				moveOwn(oSource, oDest);
				oUpdateLabel.setText("Copied " + strName + ", tidying up...");
				bSuccess = true;
			} catch (Exception e) {
				moveOwn(oDest, oSource);
				oUpdateLabel.setText("Copy " + strName + "FAILED! tried to revert. Please check download consistency.");
				e.printStackTrace();
				bSuccess = false;
			}
		} else {
			bSuccess = false;
		}

		if (bSuccess) {
			try {
				statement.executeUpdate("UPDATE patreons SET category = " + iCategory + " WHERE ID = " + iPatreonID);
				oMain.invalidatePatreonID(iPatreonID);
				oSource.delete();
				oCategory.loadPatreons();
				oUpdateLabel.setText("Copied " + strName);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean moveOwn(File sourceFile, File destFile) {
		if (sourceFile.isDirectory()) {
			totalFiles += sourceFile.listFiles().length;
			for (File file : sourceFile.listFiles()) {
				moveOwn(file, new File(destFile.getAbsolutePath() + "\\" + file.getName()));
			}
		} else {
			try {
				System.out.println("move file " + sourceFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());
				destFile.getParentFile().mkdirs();
				Files.move(Paths.get(sourceFile.getPath()), Paths.get(destFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
				copiedFiles++;
				oUpdateLabel.setText("copying " + strName + ", done " + copiedFiles + "/" + totalFiles);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
}
