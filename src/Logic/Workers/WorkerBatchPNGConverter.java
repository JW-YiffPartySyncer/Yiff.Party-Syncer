package Logic.Workers;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import UI.BatchConvertPNGs;
import UI.Main;

public class WorkerBatchPNGConverter implements Runnable {

	private Main oMain;
	private BatchConvertPNGs oFrame;

	private ArrayList<WorkerPNGConverter> aWorkers = new ArrayList<WorkerPNGConverter>();
	private ArrayList<Thread> aThreads = new ArrayList<Thread>();
	private LinkedBlockingQueue<File> aQueue = new LinkedBlockingQueue<File>();

	private int totalFiles;
	private int listedFiles;

	public WorkerBatchPNGConverter(Main oMain, BatchConvertPNGs oFrame) {
		this.oMain = oMain;
		this.oFrame = oFrame;
	}

	@Override
	public void run() {
		oFrame.lblAction.setText("Starting worker threads");
		for (int i = 0; i < oMain.oConf.iBatchPNGConverters; i++) {
			WorkerPNGConverter oW = new WorkerPNGConverter(oMain, aQueue);
			Thread oThread = new Thread(oW);
			oThread.setName("Converter " + i);
			oThread.start();

			aWorkers.add(oW);
			aThreads.add(oThread);
		}

		File oRootDirectory = new File(oMain.oConf.strSavepath);
		recurConvert(oRootDirectory);
		while (aQueue.size() != 0) {
			oFrame.lblAction.setText("Waiting for convert workers");
			oFrame.lblStatus.setText("Remaining queue elems: " + aQueue.size());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		boolean bFound = true;
		while (bFound) {
			bFound = false;
			for (WorkerPNGConverter oW : aWorkers) {
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

		for (int i = 0; i < aThreads.size(); i++) {
			try {
				aThreads.get(i).join(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void recurConvert(File oFile) {
		oFrame.lblAction.setText("Processing " + oFile.getAbsolutePath());
		oFrame.lblStatus.setText("done " + listedFiles + "/" + totalFiles);
		if (oFile.isDirectory()) {
			File[] aFiles = oFile.listFiles();
			totalFiles += aFiles.length;
			for (File oTarget : aFiles) {
				recurConvert(oTarget);
			}
		} else {
			if (oFile.getName().contains(".")) {
				if (oFile.getName().substring(oFile.getName().lastIndexOf('.') + 1).equalsIgnoreCase("png")) {
					try {
						aQueue.put(oFile);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		listedFiles++;
	}

}
