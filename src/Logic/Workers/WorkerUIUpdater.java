package Logic.Workers;

import UI.Main;

/**
 * 
 * @author JW
 *
 *         This worker thread is responsible for updating the UI. Because, you
 *         know, we can't update the UI from the main thread or else the UI
 *         freezes because java!
 *
 */
public class WorkerUIUpdater implements Runnable {

	private Main main;

	public WorkerUIUpdater(Main main) {
		this.main = main;
	}

	/**
	 * Main worker thread. Should be self explanatory. Refresh low-load items with
	 * 10 FPS, refresh stats once a second or once every 5 seconds because they
	 * create database load. TODO: maybe lower the updateDownloads and
	 * updatePatreonTracking to even higher values? cause they still cause distinct
	 * database load on reload statistics
	 */
	@Override
	public void run() {
		int iCounter = 0;
		main.updateDownloads();
		main.updatePatreonTracking();
		while (true) {
			if (iCounter % 10 == 0) {
				main.updateDownloads();
			}
			if (iCounter % 50 == 0) {
				main.updatePatreonTracking();
			}
			main.updateDownloadBuffer();
			main.updatePatreonParser();
			try {
				main.updateDownloadThreads();
			} catch (Exception e) {

			}
			main.updateRAM();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			iCounter++;
			if (iCounter == 100000) {
				iCounter = 0;
			}
		}
	}

}
