package Logic.Workers;

import java.io.File;
import java.io.IOException;

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
	
	private WorkerDownloader oConverter;

	public WorkerLocalUnzip(Main oMain, UnzipLocal oWindow) {
		this.oMain = oMain;
		this.oWindow = oWindow;
		oConverter = new WorkerDownloader(null, oMain);
	}

	@Override
	public void run() {
		File oRootfolder = new File(oMain.oConf.strSavepath);
		recurWork(oRootfolder);
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
						OUtil.unzipSameDir(oFile, oMain.oConf.bDLWConvertPNGs, oConverter);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (oWindow != null) {
			oWindow.lblStatus.setText("Finished.");
		}
	}

}
