package Logic.Workers;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Logic.OUtil;
import UI.DownloadCheck;
import UI.Main;

public class WorkerLocalDownloadChecker implements Runnable {

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;

	private Main oMain;
	private DownloadCheck ui;

	public WorkerLocalDownloadChecker(Main oMain, DownloadCheck ui) {
		this.oMain = oMain;
		this.ui = ui;
	}

	@Override
	public void run() {
		System.out.println("WorkerLocalDownloadChecker: run");
		ui.lblFile.setText("Launching...");
		connection = OUtil.connectToMysql(oMain.oConf.strDBHost, oMain.oConf.strDBDatabase, oMain.oConf.strDBUser, oMain.oConf.strDBPassword);
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (statement != null) {
			System.out.println("WorkerLocalDownloadChecker: SELECT patreon, name FROM posts WHERE downloaded = 1");
			ui.lblFile.setText("Query database for a list of files");
			try {
				resultSet = statement.executeQuery("SELECT patreon, name, date, ID FROM posts WHERE downloaded = 1");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (resultSet != null) {
				System.out.println("WorkerLocalDownloadChecker: got result set. Start work()");
				int iMissing = 0;
				int iTotal = 0;
				try {
					while (resultSet.next()) {
						String strName = resultSet.getString("date") + resultSet.getString("name").substring(0, resultSet.getString("name").lastIndexOf("."))
								+ resultSet.getInt("ID") + resultSet.getString("name").substring(resultSet.getString("name").lastIndexOf("."));
						int iPatreon = resultSet.getInt("patreon");
						Statement st2 = connection.createStatement();
						ResultSet set2 = st2.executeQuery("SELECT name, category FROM patreons WHERE ID = " + iPatreon);
						if (set2.next()) {
							String strFolderName = set2.getString("name");
							int iCategory = set2.getInt("category");
							if (iCategory == 0) {
								iCategory = 1;
							}
							set2 = st2.executeQuery("SELECT path FROM categories WHERE ID = " + iCategory);
							if (set2.next()) {
								String strCategory = set2.getString("path");

								File oFile = new File(oMain.oConf.strSavepath + strCategory + "\\" + strFolderName + "\\" + strName);
								ui.lblFile.setText(oFile.getAbsolutePath());
								ui.lblTotal.setText("" + ++iTotal);
								boolean bExists = true;
								if (!oFile.exists()) {
									bExists = false;
									if(oFile.getName().substring(oFile.getName().lastIndexOf(".") + 1).equalsIgnoreCase("png")) {
										oFile = new File(oFile.getAbsolutePath() + ".jpg");
										if(oFile.exists()) {
											bExists = true;
										}
									}
									if(!bExists) {
										st2.executeUpdate("UPDATE posts SET downloaded = 0 WHERE ID = " + resultSet.getInt("ID"));
										ui.lblMissing.setText("" + ++iMissing);
									}
								}
							} else {
								continue;
							}
						} else {
							continue;
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
