package Logic.Workers;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import Logic.OUtil;
import UI.DownloadCheck;
import UI.Main;

public class WorkerLocalDownloadChecker implements Runnable {

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;

	private Main oMain;
	private DownloadCheck ui;

	private LinkedList<String> aFolders = new LinkedList<String>();
	private LinkedList<String> aFiles = new LinkedList<String>();
	private LinkedList<Integer> aIDs = new LinkedList<Integer>();

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

				// Optimization strings???
				String strQuery1 = "SELECT name, category FROM patreons WHERE ID = ";
				String strQuery2 = "SELECT path FROM categories WHERE ID = ";
				StringBuilder strNameBuilder = new StringBuilder();
				StringBuilder strFolderBuilder = new StringBuilder();
				//End opt

				String strFolderName, strCategory;
				int iFolderIndex, iCounter = 0, iPatreon, iCategory, i;
				Statement st2 = null;
				try {
					st2 = connection.createStatement();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				ResultSet set2;
				try {
					while (resultSet.next()) {
						strNameBuilder.setLength(0);
						strNameBuilder.append(resultSet.getString("date"));
						strNameBuilder.append(resultSet.getString("name").substring(0, resultSet.getString("name").lastIndexOf(".")));
						strNameBuilder.append(resultSet.getInt("ID"));
						strNameBuilder.append(resultSet.getString("name").substring(resultSet.getString("name").lastIndexOf(".")));
						iPatreon = resultSet.getInt("patreon");
						set2 = st2.executeQuery(strQuery1 + iPatreon);
						if (set2.next()) {
							strFolderName = set2.getString("name");
							iCategory = set2.getInt("category");
							if (iCategory == 0) {
								iCategory = 1;
							}
							set2 = st2.executeQuery(strQuery2 + iCategory);
							if (set2.next()) {
								strCategory = set2.getString("path");
								
								strFolderBuilder.setLength(0);
								strFolderBuilder.append(strCategory);
								strFolderBuilder.append(strFolderName);
								strFolderBuilder.append("\\");
								
								iFolderIndex = -1;
								for (i = aFolders.size() - 1; i >= 0; i--) {
									if (aFolders.get(i).equals(strFolderBuilder.toString())) {
										iFolderIndex = i;
										break;
									}
								}
								if (iFolderIndex == -1) {
									aFolders.add(strFolderBuilder.toString());
									iFolderIndex = aFolders.size() - 1;
								}
								aFiles.add(iFolderIndex + "$" + strNameBuilder.toString());
								aIDs.add(resultSet.getInt("ID"));

								ui.lblFile.setText("Enumerating files from Database...");
								ui.lblTotal.setText("" + ++iCounter);
							} else {
								set2.close();
								continue;
							}
							set2.close();
						} else {
							continue;
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		int iMissing = 0;
		int iCounter = 0;
		while (!aFiles.isEmpty()) {
			ui.lblTotal.setText(++iCounter + "/" + aFiles.size());
			ui.lblFile.setText(aFiles.get(0));

			File oFile = new File(oMain.oConf.strSavepath + aFolders.get(Integer.parseInt(aFiles.get(0).substring(0, aFiles.get(0).indexOf('$'))))
					+ aFiles.get(0).substring(aFiles.get(0).indexOf('$') + 1));
			boolean bExists = true;
			if (!oFile.exists()) {
				bExists = false;
				if (oFile.getName().substring(oFile.getName().lastIndexOf(".") + 1).equalsIgnoreCase("png")) {
					oFile = new File(oFile.getAbsolutePath() + ".jpg");
					if (oFile.exists()) {
						bExists = true;
					}
				}
				if (!bExists) {
					try {
						statement.executeUpdate("UPDATE posts SET downloaded = 0 WHERE ID = " + aIDs.get(0));
					} catch (SQLException e) {
						e.printStackTrace();
					}
					ui.lblMissing.setText("" + ++iMissing);
				}
			}
			aFiles.remove();
			aIDs.remove();
		}
	}
}
