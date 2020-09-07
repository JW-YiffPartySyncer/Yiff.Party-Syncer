package Logic.Workers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import Logic.OUtil;
import UI.Main;

/**
 * 
 * @author JW
 * 
 *         This worker parses yiff.party patreon sites for posts and saves all
 *         downloadable files from yiff.party in the DB
 *
 */
public class WorkerPatreonUpdater implements Runnable {

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;
	boolean bSuccess = true;

	private int iPatreon = 0;

	public String strStatus = "NULL";
	public long lTimestamp;

	private Main oMain;

	public WorkerPatreonUpdater(Main oMain) {
		this.oMain = oMain;
	}

	/**
	 * Main thread loop
	 */
	@Override
	public void run() {
		connection = OUtil.connectToMysql(oMain.oConf.strDBHost, oMain.oConf.strDBDatabase, oMain.oConf.strDBUser, oMain.oConf.strDBPassword);
		while (true) {
			strStatus = "Idle";
			lTimestamp = System.currentTimeMillis();
			bSuccess = true;
			long limitCheck = System.currentTimeMillis() - oMain.oConf.iPatreonCheckTimeout;
			long limitCheckFailed = System.currentTimeMillis() - oMain.oConf.iPatreonCheckFailedTimeout;
			if (statement != null) {
				try {
					resultSet = statement.executeQuery("SELECT * FROM patreons WHERE last_checked < " + limitCheckFailed
							+ " AND success = FALSE AND last_checked != 0 AND wanted = 1 ORDER BY last_checked ASC LIMIT 1");
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				if (resultSet != null) {
					try {
						if (resultSet.next()) {
							work();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				try {
					resultSet = statement.executeQuery("SELECT * FROM patreons WHERE last_checked < " + limitCheck + " AND wanted = 1 ORDER BY last_checked ASC LIMIT 1");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if (resultSet != null) {
					try {
						if (resultSet.next()) {
							bSuccess = true;
							work();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					statement = connection.createStatement();
					continue;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (!bSuccess) {
				try {
					System.out.println("WorkerPatreonUpdater: Wait " + (oMain.oConf.iPatreonWaitTimeout / 1000)
							+ ((oMain.oConf.iPatreonWaitTimeout / 1000) == 1 ? "second" : "seconds") + " for next patreon site update");
					Thread.sleep(oMain.oConf.iPatreonWaitTimeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				resultSet = statement.executeQuery("SELECT COUNT('ID') FROM posts");
				if (resultSet.next()) {
					statement.executeUpdate("UPDATE stats SET value = '" + resultSet.getInt(1) + "' WHERE entry = 'totalFiles'");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Do stuff. Mainly, call other methods in order.
	 */
	private void work() {
		boolean cont = false;
		String strLink = null;
		String strName = null;
		boolean name = false;
		try {
			strLink = resultSet.getString("link");
			iPatreon = resultSet.getInt("ID");
			strName = resultSet.getString("name");
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		if (strLink != null) {
			if (strName != null) {
				if (strName.equals("")) {
					name = true;
				} else {
					name = false;
				}
			}
			int i = 1;
			// Intro to recursive patreon parsing. i = 1, cause we want the first site.
			cont = parseGlobal(strLink, i, name);
			i++;
			if (cont) {
				do {
					System.out.println("Cont page " + i);
					cont = parseGlobal(strLink + "?p=" + i, i, false);
					i++;
					// Wait in between sites, as to not DDoS yiff.party
					try {
						Thread.sleep(oMain.oConf.iPatrenConsecutiveSiteTimeout);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} while (cont);
			}
		}
	}

	/**
	 * Prettify downloaded HTML website data. When we download the website HTML,
	 * evetything is in one single line. Now I simply insert a line separator after
	 * every ">". works decently well.
	 * 
	 * @param strData - Single-line website data
	 * @return String[] with the prettified website data
	 */
	private String[] prettify(String strData) {
		ArrayList<String> aResult = new ArrayList<String>();
		char[] aData = strData.toCharArray();
		int iStart = 0;
		int iEnd = 0;
		boolean bFound = true;
		StringBuilder sb = new StringBuilder();
		while (bFound) {
			bFound = false;
			for (int i = iStart; i < aData.length; i++) {
				if (aData[i] == '>') {
					bFound = true;
					iEnd = i;
					break;
				}
			}
			if (bFound) {
				for (int i = iStart; i <= iEnd; i++) {
					sb.append(aData[i]);
				}
				aResult.add(sb.toString());
				sb.setLength(0);
			}
			iStart = iEnd + 1;
		}
		String[] result = new String[aResult.size()];
		for (int i = 0; i < aResult.size(); i++) {
			result[i] = aResult.get(i);
		}
		return result;
	}

	/**
	 * Parse website recursively
	 * 
	 * @param strLink - URL to the current yiff.party Patreon page we are working on
	 * @param iPage   - What page are we on?
	 * @param name    - Page Name of the creator
	 * @return boolean success
	 */
	private boolean parseGlobal(String strLink, int iPage, boolean name) {
		try {
			strStatus = strLink;
			lTimestamp = System.currentTimeMillis();
			System.out.println("WorkerPatreonUpdater work: Starting on " + strLink);
			System.setProperty("http.agent", "Mozilla/5.0 (Linux; Android 4.2. 2; en-us; SAMSUNG SGH-M919 Build/JDQ39)");
			URL url = new URL(strLink);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			// Try to at least sanitize the path a little.
			String strPath = "pages\\" + strLink.replaceAll("/", "").replaceAll("https", "").replaceAll(":", "").replaceAll("=", "") + ".html";
			if (strPath.contains("?")) {
				strPath = strPath.substring(0, strPath.indexOf("?")) + strPath.substring(strPath.indexOf("?") + 1);
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(strPath));
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				// writer.write(line);
			}
			String strDataSingle = sb.toString();
			String[] strData = prettify(strDataSingle);
			// Save page to disk. Path is besides the .jar in a folder called "pages"
			for (int i = 0; i < strData.length; i++) {
				writer.write(strData[i]);
			}
			writer.close();
			System.out.println("Page downloaded.");

			if (name) {
				parseName(strData);
			}

			parseSite(strDataSingle);
			boolean cont = parseCont(strData, iPage);

			updateStatus(iPatreon, true);
			if (cont) {
				return true;
			}
		} catch (Exception e) {
			bSuccess = false;
			e.printStackTrace();
			System.out.println("Setting status to FAIL for " + iPatreon);
			updateStatus(iPatreon, false);
		}
		bSuccess = false;
		return false;
	}

	/**
	 * Parse the current site. This is a crude implementation, but it works. Use
	 * JSoup to parse HTML, try to extract single posts, try to see if it has
	 * attachments. Get Attachments HREF links, if it has no attachments, try to get
	 * the file that has been attached as the main file in the Post itself. TODO:
	 * can this be expanded to work with for example Google Drive, Mega links ETC?
	 * 
	 * @param strData - Single-line website data
	 */
	private void parseSite(String strData) {
		Document doc = Jsoup.parse(strData);
		Element card = doc.select("div[class=col s12 m6]").first();
		// Try to iterate through posts in the yiff.party site
		// TODO: yiff.party currently uses the div class "col s12 m6" for single posts.
		// if yiffparty changes this designator, everything breaks. is there a way to
		// make this more... futureproof?
		do {
			Element header = card.select("div[class^=card-content]").first();

			if (header != null) {
				String strHeader = header.toString();
				String strTitle = extractTitle(strHeader);

				Element IDholder = card.select("div[class=card large yp-post]").first();
				String strID = IDholder.attr("id");

				String strTimestamp = extractTimestamp(IDholder.toString());

				// Try to extract post text body
				String strBody = null;
				try {
					strBody = card.select("div[class=post-body]").text();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (strBody != null) {
					updatePostText(strID, strBody);
				}

				// Try to extract attachments
				Element attachment = card.select("div[class=card-attachments]").first();
				if (attachment != null) {
					String[] strAttachment = prettify(attachment.toString());
					extractLinks(strAttachment, strID, strTitle, strTimestamp);
				} else {
					// If no attachments are found, try to extract the file that has been posted
					// with the post
					attachment = card.select("div[class=card-action]").first();
					if (attachment != null) {
						do {
							try {
								Element link = attachment.select("a").first();
								String absHref = link.attr("abs:href");
								String strName = absHref.substring(absHref.lastIndexOf("/") + 1);
								if (strName.contains(".") && strName.substring(strName.length() - 5).contains(".")) {
									updatePosts(strName, absHref, strID, strTitle, strTimestamp);
								}
							} catch (Exception e) {
								// e.printStackTrace();
							}
							attachment = attachment.nextElementSibling();
						} while (attachment != null);
					}
				}
				scanForMEGALinks(card.toString(), "MEGA.NZ", strID, strTitle, strTimestamp);
			} else {
				bSuccess = false;
				System.out.println("Error?");
			}

			card = card.nextElementSibling();
		} while (card != null);
		// Funfact: I never thought I'd need a do-while loop any time in my life
	}

	/**
	 * Insert found files into yiffparty.posts database
	 * 
	 * @param strName      - Name of the file
	 * @param strHref      - absolute link where we can download the file from
	 * @param strID        - ID of the post on yiff.party
	 * @param strTitle     - Title of the post on yiff.party
	 * @param strTimestamp - Sanitized timestamp when the post was created on
	 *                     yiff.party
	 */
	private void updatePosts(String strName, String strHref, String strID, String strTitle, String strTimestamp) {
		// Check if entry already exists
		try {
			resultSet = statement.executeQuery("SELECT * FROM posts WHERE patreon = " + iPatreon + " AND href = '" + strHref + "' LIMIT 1");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		boolean bFound = false;
		if (resultSet != null) {
			try {
				if (resultSet.next()) {
					bFound = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (!bFound) {
			try {
				statement.executeUpdate("INSERT INTO posts (patreon, name, href, post, date) VALUES (" + iPatreon + ", '" + strName + "', '" + strHref + "', '" + strID + "', '"
						+ strTimestamp + "') ON DUPLICATE KEY UPDATE ID = ID");
			} catch (SQLException e) {
				bSuccess = false;
				e.printStackTrace();
			}
		}
	}

	/**
	 * Iterate through the attachments and get all links
	 * 
	 * @param strData      - String[] with prettified post attachment HTML data
	 * @param strID        - ID of the post
	 * @param strTitle     - title of the post
	 * @param strTimestamp - sanitized timestamp of the post
	 */
	private void extractLinks(String[] strData, String strID, String strTitle, String strTimestamp) {
		try {
			for (String strLine : strData) {
				if (strLine.contains("href=\"https://data.yiff.party/patreon_data/") || strLine.contains("href=\\\"https://data.yiff.party/patreon_inline/")) {
					String strLink = strLine.substring(strLine.indexOf("href") + 6, strLine.indexOf('"', strLine.indexOf("href") + 7));
					String strName = strLink.substring(strLink.lastIndexOf("/") + 1);
					if (strName.contains(".") && strName.substring(strName.length() - 5).contains(".")) {
						updatePosts(strName, strLink, strID, strTitle, strTimestamp);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Try to extract title from post
	 * 
	 * @param strData - Single-line HTML data of the website
	 * @return String title of the page
	 */
	private String extractTitle(String strData) {
		int iStart = strData.indexOf("span");
		int iReal = strData.indexOf(">", iStart);
		String strResult = strData.substring(iReal + 1, strData.indexOf("<", iReal));
		return strResult;
	}

	/**
	 * Update status of the patreon that we worked on
	 * 
	 * @param iID     - ID of the patreon in the DB
	 * @param success - Success?
	 */
	private void updateStatus(int iID, boolean success) {
		try {
			statement.executeUpdate("UPDATE patreons SET success = " + (success ? "TRUE" : "FALSE") + ", last_checked = " + System.currentTimeMillis() + " WHERE ID = " + iID);
		} catch (SQLException e) {
			bSuccess = false;
			e.printStackTrace();
		}
		try {
			resultSet = statement.executeQuery("SELECT COUNT('ID') FROM patreons WHERE success = 1");
			if (resultSet.next()) {
				statement.executeUpdate("UPDATE stats SET value = '" + resultSet.getInt(1) + "' WHERE entry = 'syncedPatreons'");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			resultSet = statement.executeQuery("SELECT COUNT('ID') FROM patreons WHERE success = 0 AND last_checked != 0");
			if (resultSet.next()) {
				statement.executeUpdate("UPDATE stats SET value = '" + resultSet.getInt(1) + "' WHERE entry = 'retryPatreons'");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if there are more pages that we can look at
	 * 
	 * @param strData - prettified Website HTML data
	 * @param iPage   - the page we were on
	 * @return boolean if we have more pages to go
	 */
	private boolean parseCont(String[] strData, int iPage) {
		for (String strTemp : strData) {
			if (strTemp.contains("?p=" + (iPage + 1))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Try to parse the name of the patreon page
	 * 
	 * @param strData - prettified website HTML data
	 */
	private void parseName(String[] strData) {
		String strName = "";
		for (int i = 0; i < strData.length; i++) {
			if (strData[i].contains("<title>")) {
				strName = strData[i + 1].substring(0, strData[i + 1].indexOf("|")).replaceAll(" ", "").replaceAll("", "");
			}
		}
		try {
			statement.executeUpdate("UPDATE patreons SET name = '" + strName + "' WHERE ID = " + iPatreon);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Try to parse timestamp of the post
	 * 
	 * @param strData - Single-Line post HTML data
	 * @return String timestamp
	 */
	private String extractTimestamp(String strData) {
		String[] aData = prettify(strData);
		String strResult = "";
		for (int i = 0; i < aData.length; i++) {
			if (aData[i].contains("grey-text post-time")) {
				strResult = aData[i + 1].substring(2, aData[i + 1].indexOf('+')).replaceAll("-", "").replaceAll(":", "").replace("T", "");
			}
		}
		return strResult;
	}

	/**
	 * Try and insert Post text body, if not exists
	 * 
	 * @param strID   - ID of the Post on yiff.party
	 * @param strData - Text Body of the post message
	 */
	private void updatePostText(String strID, String strData) {
		ResultSet ownSet = null;
		try {
			ownSet = statement.executeQuery("SELECT * FROM posttext WHERE postID = '" + strID + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		boolean cont = false;
		if (ownSet != null) {
			try {
				if (ownSet.next()) {
					if (ownSet.getString("text").equals("") && !strData.equals("")) {
						cont = true;
					}
				} else {
					cont = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (cont) {
			strData = strData.replaceAll("\\\\", "\\\\\\\\");
			strData = strData.replaceAll("'", "\\\\'");
			try {
				statement.executeUpdate("INSERT INTO posttext (postID, text) VALUES ('" + strID + "', '" + strData + "')");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return;
	}

	private void scanForMEGALinks(String strData, String strName, String strID, String strTitle, String strTimestamp) {
		String[] aData = prettify(strData);
		for (String strLine : aData) {
			if (strLine.contains("mega.nz/")) {
				try {
					String strLink = "";
					if (strLine.startsWith("http")) {
						strLink = strLine.substring(0, strLine.indexOf('<'));
					} else if (strLine.contains("")) {
						strLink = strLine.substring(strLine.indexOf('"') + 1, strLine.indexOf('"', strLine.indexOf('"') + 1));
					} else {
						continue; // TODO: improve Mega Link scanning
					}
					if (!strLink.equals("https://mega.nz/")) {
						updatePosts(strName, strLink, strID, strTitle, strTimestamp);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return;
	}

}
