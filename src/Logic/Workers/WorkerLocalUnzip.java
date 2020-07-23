package Logic.Workers;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import Logic.OUtil;
import UI.Main;
import UI.UnzipLocal;

/**
 * 
 * @author JW
 * 
 *         Walks through the whole local collection, searches for .zip files to
 *         unzip
 *
 */
public class WorkerLocalUnzip implements Runnable {

	private Main oMain;
	private UnzipLocal oWindow;

	private int totalFiles = 0;
	private int finishedFiles = 0;

	private ArrayList<WorkerPNGConverter> aConverters = new ArrayList<WorkerPNGConverter>();
	private ArrayList<Thread> aThreads = new ArrayList<Thread>();
	private LinkedBlockingQueue<File> aConvertQueue = new LinkedBlockingQueue<File>();

	public WorkerLocalUnzip(Main oMain, UnzipLocal oWindow) {
		this.oMain = oMain;
		this.oWindow = oWindow;
	}

	@Override
	public void run() {
		for (int i = 0; i < oMain.oConf.iBatchPNGConverters; i++) {
			WorkerPNGConverter oC = new WorkerPNGConverter(oMain, aConvertQueue);
			aConverters.add(oC);
			Thread oThread = new Thread(oC);
			oThread.setName("Converter " + i);
			oThread.start();
			aThreads.add(oThread);
		}
		File oRootfolder = new File(oMain.oConf.strSavepath);
		recurWork(oRootfolder);
		if (oWindow != null) {
			oWindow.lblStatus.setText("Waiting for converters to finish work.");
		}
		while (aConvertQueue.size() > 1) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		boolean bFound = true;
		while (bFound) {
			bFound = false;
			for (WorkerPNGConverter oW : aConverters) {
				if (oW.bWorking) {
					bFound = true;
					break;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (Thread oThread : aThreads) {
			try {
				oThread.join(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void recurWork(File oFile) {
		if (oFile.isDirectory()) {
			finishedFiles++;
			if (oWindow != null) {
				oWindow.lblAction.setText("Listing directory " + oFile.getAbsolutePath());
			}
			String[] aSubFiles = oFile.list();
			totalFiles += aSubFiles.length;
			if (oWindow != null) {
				oWindow.lblStatus.setText("Total Files: " + finishedFiles + "/" + totalFiles);
			}
			for (String strFile : aSubFiles) {
				recurWork(new File(oFile.getAbsolutePath() + "\\" + strFile));
			}
		} else {
			finishedFiles++;
			if (oWindow != null) {
				oWindow.lblAction.setText("processing directory " + oFile.getParentFile().getAbsolutePath());
				oWindow.lblStatus.setText("Total Files: " + finishedFiles + "/" + totalFiles);
			}
			if (oFile.getName().contains(".")) {
				if (oFile.getName().substring(oFile.getName().lastIndexOf('.') + 1).equals("zip")) {
					try {
						if (oWindow != null) {
							oWindow.lblAction.setText("Unzipping " + oFile.getAbsolutePath());
						}
						OUtil.unzipSameDir(oFile, false, null);
						pngConvert(oFile);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (oFile.getName().substring(oFile.getName().lastIndexOf('.') + 1).equalsIgnoreCase("7z")) {
					try {
						if (oWindow != null) {
							oWindow.lblAction.setText("Unzipping " + oFile.getAbsolutePath());
						}
						OUtil.unzip7zSameDir(oFile, false, null);
						pngConvert(oFile);
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		}
	}

	private void pngConvert(File oFile) {
		if (oMain.oConf.bDLWConvertPNGs) {
			File oDir = new File(oFile.getParentFile().getAbsolutePath(), oFile.getName().substring(0, oFile.getName().lastIndexOf('.')));
			recurConvert(oDir);
		}
	}

	private void recurConvert(File oFile) {
		if (oFile.isDirectory()) {
			for (File oContinue : oFile.listFiles()) {
				recurConvert(oContinue);
			}
		} else {
			if (oFile.getName().contains(".")) {
				if (oFile.getName().substring(oFile.getName().lastIndexOf('.') + 1).equalsIgnoreCase("png")) {
					try {
						aConvertQueue.put(oFile);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
