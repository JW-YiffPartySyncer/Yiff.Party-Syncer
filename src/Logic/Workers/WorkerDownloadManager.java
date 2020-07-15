package Logic.Workers;

import java.sql.Connection;
import java.sql.DriverManager;
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

	private Main oMain;
	private int iWorkerIndex = -1; // Last running worker in the aDownloaders ArrayList

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
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * if we finished a DownloadObject, remove it from the DO ArrayList
	 */
	private void checkQueue() {
		ArrayList<DownloadObject> aTemp = new ArrayList<DownloadObject>();
		for (DownloadObject DO : aDOs) {
			if (DO.finished) {
				aTemp.add(DO);
				System.out.println("WorkerDownloadManager: removed " + DO.ID + " from Queue");
			}
		}

		aDOs.removeAll(aTemp);
	}

	/**
	 * Tries to poll the DB for pending downloads
	 * 
	 * @return - success?
	 */
	private boolean getNewDO() {
		DownloadObject DO = new DownloadObject();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM posts WHERE downloaded = FALSE AND last_checked < " + (System.currentTimeMillis() - oMain.oConf.iDLWRetryTimeout));
		for (int i = 0; i < aDOs.size(); i++) {
			sb.append(" AND ID != " + aDOs.get(i).ID);
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
						patreon = resultSet.getInt("patreon");
						strTimestamp = resultSet.getString("date");
					} catch (SQLException e) {
						e.printStackTrace();
						return false;
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
									if (strRoot != null) {
										DO.strPath = oMain.oConf.strSavepath + strRoot + "\\" + strTimestamp + strName.substring(0, strName.lastIndexOf('.')) + DO.ID
												+ strName.substring(strName.lastIndexOf('.'));
										aDOs.add(DO);
										aQueue.put(DO);
										return true;
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
		iWorkerIndex++;
		WorkerDownloader oW = new WorkerDownloader(this, oMain);
		aDownloaders.add(oW);
		Thread oThread = new Thread(oW);
		oThread.setName("WorkerDownloader " + iWorkerIndex);
		oThread.start();
		oMain.oConf.iDLBuffer = aDownloaders.size() * oMain.oConf.iDLBufferMultiplier;
	}
	
	/**
	 * Attempts to stop a worker thread
	 */
	public void stopWorker() {
		if(iWorkerIndex != -1) {
			aDownloaders.get(iWorkerIndex).stop();
			iWorkerIndex--;
		}
	}
	
	/**
	 * Periodically check aDownloaders for stopped workers
	 */
	private void checkWorkers() {
		int iFound = -1;
		for(int i = 0; i < aDownloaders.size(); i++) {
			if(!aDownloaders.get(i).bWorking) {
				iFound = i;
				break;
			}
		}
		if(iFound != -1) {
			aDownloaders.remove(iFound);
			oMain.oConf.iDLBuffer = aDownloaders.size() * oMain.oConf.iDLBufferMultiplier;
		}
	}

}
