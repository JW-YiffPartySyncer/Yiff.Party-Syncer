package Logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author JW
 * 
 *         Holds configuration for the whole program. At the moment everything
 *         is hardcoded. TODO: Expand to load settings from elsewhere. Maybe
 *         Database? Maybe a file?
 *
 */
public class Config {
	public String strVersion = "0.6.5";
	// Database Configuration
	public String strDBHost = "localhost";
	public String strDBDatabase = "yiffparty";
	public String strDBUser = "client";
	public String strDBPassword = "keins";

	// Local save master folder
	public String strSavepath = "W:\\Private\\yiffparty\\";
	// batch unzip max amount of PNG converters
	public int iBatchPNGConverters = 8;

	// Credentials for different websites.
	// MEGA.nz
	public String strMegaUser = "";
	public String strMegaPW = "";

	// UI Configuration
	// Auto-Open default state
	public boolean bUIAutoOpen = true;

	// DownloadManager configuration
	// How many download workers will be spawned. More download workers = higher
	// download parallelism.
	// Please don't set too high, we don't want to DDoS yiff.party...
	// 4 workers can reach 500+ MBit/s download bandwidth in best case
	public int iNumDLWorkers = 1;
	// How many download files should be buffered by the DownloadManager
	// Anything above 20 seems to have diminishing returns because the SQL query
	// will get bigger the more objects we try to cache.
	public int iDLBuffer = 5;
	public int iDLBufferMultiplier = 5;

	// DownloadWorker config
	// How many seconds to wait after a download has failed to continue with the
	// next download item
	public int iDLWFailTimeout = 1;
	// How long to wait for a single download to retry again
	public int iDLWRetryTimeout = (1000 * 60 * 15); // 15 minutes
	// Should we convert PNGs to JPegs?
	public boolean bDLWConvertPNGs = true;
	// Quality of the converted JPGs. 0.95f = 95% JPG Quality
	public float fDLWJPGQuality = 0.95f;
	// Automatically try to unzip downloaded files.
	public boolean bDLWAutoUnzip = true;
	// If Link points to mega.nz, try to download the file with given credentials
	public boolean bDLWMega = false;

	// PatreonUpdater config
	// Timeout for checking all tracked patreons for new posts
	public int iPatreonCheckTimeout = (1000 * 60 * 60 * 24); // 1 day
	// Timeout for rechecking if yiff.party is down while we checked a patreon
	public int iPatreonCheckFailedTimeout = (1000 * 60 * 60 * 1); // 1 hour
	// Wait time in between same-patreon consecutive sites
	public int iPatrenConsecutiveSiteTimeout = (1000 * 1); // 1 second
	// Wait time in between consecutive checks
	public int iPatreonWaitTimeout = (1000 * 60); // 1 minute

	public Config() {
		loadData();
	}

	/**
	 * Save all config data to file
	 */
	public void saveData() {
		File oFile = new File("yp.ini");
		if (oFile.exists()) {
			oFile.delete();
		}

		String ls = System.lineSeparator();

		StringBuilder sb = new StringBuilder();
		sb.append("Yiff.Party Syncer v" + strVersion + " config file" + ls);
		sb.append("[Database]" + ls);
		sb.append("DBHOST:" + strDBHost + ls);
		sb.append("DBDATABASE:" + strDBDatabase + ls);
		sb.append("DBUSER:" + strDBUser + ls);
		sb.append("DBPW:" + strDBPassword + ls);
		sb.append("[Master]" + ls);
		sb.append("SAVEPATH:" + strSavepath + ls);
		sb.append("BPNGC#:" + iBatchPNGConverters + ls);
		sb.append("[Credentials]" + ls);
		sb.append("MEGU:" + strMegaUser + ls);
		sb.append("MEGPW:" + strMegaPW + ls);
		sb.append("[UI]" + ls);
		sb.append("UIAO:" + (bUIAutoOpen ? "TRUE" : "FALSE") + ls);
		sb.append("[DLWorkers]" + ls);
		sb.append("DLW#:" + iNumDLWorkers + ls);
		sb.append("DLB:" + iDLBuffer + ls);
		sb.append("DLBM:" + iDLBufferMultiplier + ls);
		sb.append("DLFT:" + iDLWFailTimeout + ls);
		sb.append("DLRT:" + iDLWRetryTimeout + ls);
		sb.append("CONVERTPNG:" + (bDLWConvertPNGs ? "TRUE" : "FALSE") + ls);
		sb.append("CVQUAL:" + fDLWJPGQuality + ls);
		sb.append("DLAU:" + (bDLWAutoUnzip ? "TRUE" : "FALSE") + ls);
		sb.append("DLMEGA:" + (bDLWMega ? "TRUE" : "FALSE") + ls);
		sb.append("[PatreonWorkers]" + ls);
		sb.append("PCHKT:" + iPatreonCheckTimeout + ls);
		sb.append("PCHKFT:" + iPatreonCheckFailedTimeout + ls);
		sb.append("PCST:" + iPatrenConsecutiveSiteTimeout + ls);
		sb.append("PWT:" + iPatreonWaitTimeout + ls);
		sb.append("//eof");

		try {
			FileWriter fw = new FileWriter(oFile);
			fw.write(sb.toString());
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * If file yp.ini exists in workingdir, try to load and parse it.
	 */
	public void loadData() {
		File oFile = new File("yp.ini");
		if (oFile.exists()) {
			String[] aData = null;
			try {
				FileReader fileReader = new FileReader(oFile);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				List<String> lines = new ArrayList<String>();
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					lines.add(line);
				}
				bufferedReader.close();
				aData = lines.toArray(new String[lines.size()]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (aData != null) {
				for (String strLine : aData) {
					parseLine(strLine);
				}
			}
		}
	}

	/**
	 * Parse a single line of the config file
	 * 
	 * @param strLine - A single line in the config file
	 */
	private void parseLine(String strLine) {
		if (strLine.contains(":")) {
			String strKey = strLine.substring(0, strLine.indexOf(':'));
			String strValue = strLine.substring(strLine.indexOf(':') + 1);
			try {
				switch (strKey) {
				case "DBHOST":
					strDBHost = strValue;
					break;
				case "DBDATABASE":
					strDBDatabase = strValue;
					break;
				case "DBUSER":
					strDBUser = strValue;
					break;
				case "DBPW":
					strDBPassword = strValue;
					break;
				case "SAVEPATH":
					strSavepath = strValue;
					break;
				case "BPNGC#":
					iBatchPNGConverters = Integer.parseInt(strValue);
					break;
				case "MEGU":
					strMegaUser = strValue;
					break;
				case "MEGPW":
					strMegaPW = strValue;
					break;
				case "UIAO":
					bUIAutoOpen = strValue.equals("TRUE");
					break;
				case "DLW#":
					iNumDLWorkers = Integer.parseInt(strValue);
					break;
				case "DLB":
					iDLBuffer = Integer.parseInt(strValue);
					break;
				case "DLBM":
					iDLBufferMultiplier = Integer.parseInt(strValue);
					break;
				case "DLFT":
					iDLWFailTimeout = Integer.parseInt(strValue);
					break;
				case "DLRT":
					iDLWRetryTimeout = Integer.parseInt(strValue);
					break;
				case "CONVERTPNG":
					bDLWConvertPNGs = strValue.equals("TRUE");
					break;
				case "CVQUAL":
					fDLWJPGQuality = Float.parseFloat(strValue);
					break;
				case "DLAU":
					bDLWAutoUnzip = strValue.equals("TRUE");
					break;
				case "DLMEGA":
					bDLWMega = strValue.equals("TRUE");
					break;
				case "PCHKT":
					iPatreonCheckTimeout = Integer.parseInt(strValue);
					break;
				case "PCHKFT":
					iPatreonCheckFailedTimeout = Integer.parseInt(strValue);
					break;
				case "PCST":
					iPatrenConsecutiveSiteTimeout = Integer.parseInt(strValue);
					break;
				case "PWT":
					iPatreonWaitTimeout = Integer.parseInt(strValue);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
