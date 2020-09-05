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

	private final int iQuerySize = 1000;
	private long lNextCheck = 0;

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
			if (aDOs.size() < oMain.oConf.iDLBuffer && System.currentTimeMillis() > lNextCheck) {
				if (!getNewDO()) {
					System.out.println("WorkerDownloadManager: nothing to download. Wait 1 minute");
					lNextCheck = System.currentTimeMillis() + (1000 * 60);
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
			if (aDOsTimeout.get(i).lTimeout < System.currentTimeMillis()) {
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
		sb.append(" ORDER BY last_checked ASC LIMIT " + iQuerySize);

		try {
			resultSet = statement.executeQuery(sb.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		boolean bReturn = false;
		boolean bReachedEnd = false;
		int iCounter = 0;
		while (aDOs.size() < oMain.oConf.iDLBuffer) {
			DownloadObject DO = new DownloadObject();
			iCounter++;
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
							bReturn = false;
							break;
						}

						if (resultSet.getInt("failcount") >= 1) {
							int additionalTime = (int) (oMain.oConf.iDLWRetryTimeoutInitial * (oMain.oConf.fDLWRetryMultiplier * resultSet.getInt("failcount")));
							long lastChecked = resultSet.getLong("last_checked");
							if (lastChecked + (additionalTime > oMain.oConf.iDLWRetryTimeoutMax ? additionalTime : oMain.oConf.iDLWRetryTimeoutMax) > System.currentTimeMillis()) {
								DO.lTimeout = lastChecked + (additionalTime > oMain.oConf.iDLWRetryTimeoutMax ? additionalTime : oMain.oConf.iDLWRetryTimeoutMax);
								aDOsTimeout.add(DO);
								System.out.println("WorkerDownloadManager: ignore " + DO.ID + " for " + ((DO.lTimeout - System.currentTimeMillis()) / 1000) + " seconds");
								bReturn = true;
								continue;
							}
							bReturn = true;
						}

						if (strName != null && patreon != -1) {
							Statement st2 = connection.createStatement();
							ResultSet rs2 = null;
							try {
								rs2 = st2.executeQuery("SELECT * FROM patreons WHERE ID = " + patreon);
							} catch (SQLException e) {
								e.printStackTrace();
							}
							if (rs2 != null) {
								try {
									if (rs2.next()) {
										String strRoot = rs2.getString("name");
										ResultSet resultSet2 = st2
												.executeQuery("SELECT * FROM categories WHERE ID = " + (rs2.getInt("category") == 0 ? " 1 OR ID = 0" : rs2.getInt("category")));
										if (resultSet2 != null) {
											if (resultSet2.next()) {
												if (strRoot != null) {
													DO.strPath = oMain.oConf.strSavepath + resultSet2.getString("path") + strRoot + "\\" + strTimestamp
															+ strName.substring(0, strName.lastIndexOf('.')) + DO.ID + strName.substring(strName.lastIndexOf('.'));
													aDOs.add(DO);
													aQueue.put(DO);
													bReturn = true;
												}
											} else {
												bReturn = false;
												break;
											}
										} else {
											bReturn = false;
											break;
										}
									} else {
										bReturn = false;
										break;
									}
								} catch (SQLException | InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					} else {
						bReturn = false;
						bReachedEnd = true;
						break;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				bReturn = false;
				break;
			}
		}
		if (bReachedEnd && iCounter >= iQuerySize) {
			return true;
		}
		return bReturn;
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
