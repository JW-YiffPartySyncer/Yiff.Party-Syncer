package Logic.Workers;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import Logic.DownloadObject;
import Logic.OUtil;
import UI.Main;

/**
 * 
 * @author JW
 * 
 *         This class manages the download threads and feeds it stuff to work
 *         on.
 *
 */
public class WorkerDownloadManager implements Runnable {

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;

	public ArrayList<DownloadObject> aDOs = new ArrayList<DownloadObject>();
	public LinkedBlockingQueue<DownloadObject> aQueue = new LinkedBlockingQueue<DownloadObject>();
	public ArrayList<WorkerDownloader> aDownloaders = new ArrayList<WorkerDownloader>();

	public ArrayList<DownloadObject> aDOsTimeout = new ArrayList<DownloadObject>();
	private ArrayList<DownloadObject> aTemp = new ArrayList<DownloadObject>();

	private Main oMain;
	public int iWorkerIndex = -1; // Last running worker in the aDownloaders ArrayList
	private boolean bArrayAccess = false;

	public WorkerDownloadManager(Main oMain) {
		this.oMain = oMain;
	}

	/**
	 * Main run loop
	 */
	@Override
	public void run() {
		connection = OUtil.connectToMysql(oMain.oConf.strDBHost, oMain.oConf.strDBDatabase, oMain.oConf.strDBUser, oMain.oConf.strDBPassword);
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		File oFile = new File(oMain.oConf.strSavepath + "Temp\\");
		if (!oFile.exists()) {
			oFile.mkdirs();
		}

		// Spawn initial number of download threads
		for (int i = 0; i < oMain.oConf.iNumDLWorkers; i++) {
			startWorker();
		}

		while (true) {
			checkQueue();
			if (aDOs.size() < oMain.oConf.iDLBuffer) {
				if (!getNewDO()) {
					System.out.println("WorkerDownloadManager: nothing to download. Wait 1 minute");
					try {
						Thread.sleep(1000 * 60);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			checkWorkers();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * if we finished a DownloadObject, remove it from the DO ArrayList Also, check
	 * timeout array list for objects which are cleared for another download attempt
	 */
	private void checkQueue() {
		if (!bArrayAccess) {
			ArrayList<DownloadObject> aTemp = new ArrayList<DownloadObject>();
			for (DownloadObject DO : aDOs) {
				if (DO.finished || DO.invalid) {
					aTemp.add(DO);
					System.out.println("WorkerDownloadManager: removed " + DO.ID + " from Queue");
				}
			}

			aDOs.removeAll(aTemp);
		}
		for (int i = 0; i < aDOsTimeout.size(); i++) {
			if (aDOsTimeout.get(i).iTimeout < System.currentTimeMillis()) {
				aTemp.add(aDOsTimeout.get(i));
			}
		}
		for (int i = 0; i < aTemp.size(); i++) {
			aDOsTimeout.remove(aTemp.get(i));
			System.out.println("WorkerDownloadManager: cleared " + aTemp.get(i).ID + " for another download attempt");
		}
		aTemp.clear();
	}

	/**
	 * Tries to poll the DB for pending downloads
	 * 
	 * @return - success?
	 */
	private boolean getNewDO() {
		DownloadObject DO = new DownloadObject();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM posts WHERE downloaded = FALSE AND last_checked < " + (System.currentTimeMillis() - oMain.oConf.iDLWRetryTimeoutInitial));
		for (int i = 0; i < aDOs.size(); i++) {
			sb.append(" AND ID != " + aDOs.get(i).ID);
		}
		for (int i = 0; i < aDOsTimeout.size(); i++) {
			sb.append(" AND ID != " + aDOsTimeout.get(i).ID);
		}
		if (!oMain.oConf.bDLWMega) {
			sb.append(" AND href NOT LIKE '%mega.nz%'");
		}
		sb.append(" ORDER BY last_checked ASC LIMIT 1");

		try {
			resultSet = statement.executeQuery(sb.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (resultSet != null) {
			try {
				if (resultSet.next()) {
					String strName = null;
					String strTimestamp = null;
					int patreon = -1;
					try {
						DO.ID = resultSet.getInt("ID");
						DO.strURL = resultSet.getString("href");
						strName = resultSet.getString("name");
						DO.strName = strName;
						patreon = resultSet.getInt("patreon");
						strTimestamp = resultSet.getString("date");
					} catch (SQLException e) {
						e.printStackTrace();
						return false;
					}

					if (resultSet.getInt("failcount") >= 1) {
						int additionalTime = (int) (oMain.oConf.iDLWRetryTimeoutInitial * (oMain.oConf.fDLWRetryMultiplier * resultSet.getInt("failcount")));
						if (resultSet.getInt("last_checked") + (additionalTime > oMain.oConf.iDLWRetryTimeoutMax ? additionalTime : oMain.oConf.iDLWRetryTimeoutMax) > System
								.currentTimeMillis()) {
							DO.iTimeout = resultSet.getInt("last_checked") + additionalTime > oMain.oConf.iDLWRetryTimeoutMax ? additionalTime : oMain.oConf.iDLWRetryTimeoutMax;
							aDOsTimeout.add(DO);
							System.out.println("WorkerDownloadManager: ignore " + DO.ID + " for " + ((DO.iTimeout - System.currentTimeMillis()) / 1000) + " seconds");
						}
						return true;
					}

					if (strName != null && patreon != -1) {
						try {
							resultSet = statement.executeQuery("SELECT * FROM patreons WHERE ID = " + patreon);
						} catch (SQLException e) {
							e.printStackTrace();
						}
						if (resultSet != null) {
							try {
								if (resultSet.next()) {
									String strRoot = resultSet.getString("name");
									ResultSet resultSet2 = statement.executeQuery(
											"SELECT * FROM categories WHERE ID = " + (resultSet.getInt("category") == 0 ? " 1 OR ID = 0" : resultSet.getInt("category")));
									if (resultSet2 != null) {
										if (resultSet2.next()) {
											if (strRoot != null) {
												DO.strPath = oMain.oConf.strSavepath + resultSet2.getString("path") + strRoot + "\\" + strTimestamp
														+ strName.substring(0, strName.lastIndexOf('.')) + DO.ID + strName.substring(strName.lastIndexOf('.'));
												aDOs.add(DO);
												aQueue.put(DO);
												return true;
											}
										} else {
											return false;
										}
									} else {
										return false;
									}
								} else {
									return false;
								}
							} catch (SQLException | InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					return false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			return false;
		}
		return false;
	}

	/**
	 * Attempts to start a worker thread
	 */
	public void startWorker() {
		// Hardcode a max of 6 download threads
		// to not DDoS yiff.party with too many concurrent connections
		if (iWorkerIndex < 6) {
			iWorkerIndex++;
			WorkerDownloader oW = new WorkerDownloader(this, oMain);
			aDownloaders.add(oW);
			Thread oThread = new Thread(oW);
			oThread.setName("WorkerDownloader " + iWorkerIndex);
			oThread.start();
			oMain.oConf.iDLBuffer = aDownloaders.size() * oMain.oConf.iDLBufferMultiplier;
		}
	}

	/**
	 * Attempts to stop a worker thread
	 */
	public void stopWorker() {
		if (iWorkerIndex != -1) {
			aDownloaders.get(iWorkerIndex).stop();
			iWorkerIndex--;
		}
	}

	/**
	 * Periodically check aDownloaders for stopped workers
	 */
	private void checkWorkers() {
		int iFound = -1;
		for (int i = 0; i < aDownloaders.size(); i++) {
			if (!aDownloaders.get(i).bWorking) {
				iFound = i;
				break;
			}
		}
		if (iFound != -1) {
			aDownloaders.remove(iFound);
			oMain.oConf.iDLBuffer = aDownloaders.size() * oMain.oConf.iDLBufferMultiplier;
		}
	}

	/**
	 * Go through all DownloadObjects and invalidate DownloadObjects that match the
	 * given PatreonID
	 * 
	 * @param iPatreonID - Patreon ID that will get all buffered objects invalidated
	 */
	public void invalidate(int iPatreonID) {
		bArrayAccess = true;
		try {
			for (DownloadObject o : aDOs) {
				if (o.iPatreonID == iPatreonID) {
					o.invalid = true;
				}
			}
		} catch (Exception e) {
			bArrayAccess = false;
			e.printStackTrace();
		}
		bArrayAccess = false;
	}

}
