package Logic.Workers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Logic.OUtil;
import UI.Main;

/**
 * 
 * @author JW
 * 
 *         This worker thread is responsible for loading the data that the
 *         Browser UserScript puts in the yiffparty.webrip table and sift
 *         through it to discover new patreons that are currently not tracked or
 *         unwanted. This way we can discover updated patreons which can be
 *         looked at with the "next" button and decided upon with the "yes" and
 *         "no" buttons. The Program can work without the discovery userscript,
 *         this worker will not do anything in that case.
 *
 */
public class WorkerCreatorParser implements Runnable {

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;

	private Main oMain;

	public WorkerCreatorParser(Main oMain) {
		this.oMain = oMain;
	}

	/**
	 * Main worker thread loop
	 */
	@Override
	public void run() {
		connection = OUtil.connectToMysql(oMain.oConf.strDBHost, oMain.oConf.strDBDatabase, oMain.oConf.strDBUser, oMain.oConf.strDBPassword);
		while (true) {
			if (statement != null) {
				try {
					if (statement.isClosed()) {
						statement = connection.createStatement();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				try {
					resultSet = statement.executeQuery("SELECT * FROM webrip LIMIT 1"); // Get one line from the database. should represent a line in the front page
																						// yiff.party html
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if (resultSet != null) {
					try {
						if (resultSet.next()) {
							System.out.print("#"); // Spam the console with hashtags so we can see that the frontpage is getting
													// parsed.
							String strData = resultSet.getString("data");
							int iID = resultSet.getInt("ID");
							if (strData.contains("/patreon/") && strData.contains("href=")) {
								add(strData); // If the current line contains a /patreon/ and a href=, we can be pretty sure
												// that we have stumbled upon a patreon
							}
							remove(iID);
						} else {
							try {
								System.out.println("WorkerCreatorParser run: sleep one minute, result set empty");
								Thread.sleep(1000 * 60);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					statement = connection.createStatement();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Add strData to DB, if doesnt exist
	 * 
	 * @param strData - a formatted String that contains a HREF reference to the
	 *                yiff.party patreon page
	 */
	private void add(String strData) {
		// build the link
		String strLink = "https://yiff.party" + (strData.substring(strData.indexOf("href=") + 6, strData.indexOf("target") - 2));

		try {
			// Look if DB doesn't contain the link already
			resultSet = statement.executeQuery("SELECT COUNT('link') FROM patreons WHERE link = '" + strLink + "'");
			if (resultSet != null) {
				if (resultSet.next()) {
					if (resultSet.getInt(1) == 0) {
						// If the link is not present, add it to the DB and queue it for manual checking
						statement.executeUpdate("INSERT INTO patreons (link, name, last_checked) VALUES ('" + strLink + "', '', 0)");
						try {
							resultSet = statement.executeQuery("SELECT COUNT('ID') FROM patreons WHERE wanted = 0");
							if (resultSet.next()) {
								statement.executeUpdate("UPDATE stats SET value = '" + resultSet.getInt(1) + "' WHERE entry = 'uncheckedPatreons'");
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove the currently worked line from yiffparty.webrip
	 * 
	 * @param iID - ID of the entry we worked on in yiffparty.webrip
	 */
	private void remove(int iID) {
		try {
			statement.executeUpdate("DELETE FROM webrip WHERE ID = " + iID);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
