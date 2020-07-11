package Logic;

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
	public String strVersion = "0.1.2";
	// Database Configuration
	public String strDBHost = "localhost";
	public String strDBDatabase = "yiffparty";
	public String strDBUser = "client";
	public String strDBPassword = "keins";

	// Local save master folder
	public String strSavepath = "W:\\Private\\yiffparty\\";

	// DownloadManager configuration
	// How many download workers will be spawned. More download workers = higher
	// download parallelism.
	// Please don't set too high, we don't want to DDoS yiff.party...
	// 4 workers can reach 500+ MBit/s download bandwidth in best case
	public int iNumDLWorkers = 4;
	// How many download files should be buffered by the DownloadManager
	// Anything above 20 seems to have diminishing returns because the SQL query
	// will get bigger the more objects we try to cache.
	public int iDLBuffer = 20;

	// DownloadWorker config
	// How many seconds to wait after a download has failed
	public int iDLWFailTimeout = 1;

	// PatreonUpdater config
	// Timeout for checking all tracked patreons for new posts
	public int iPatreonCheckTimeout = (1000 * 60 * 60 * 24); // 1 day
	// Timeout for rechecking if yiff.party is down while we checked a patreon
	public int iPatreonCheckFailedTimeout = (1000 * 60 * 60 * 1); // 1 hour
	// Wait time in between consecutive checks
	public int iPatreonWaitTimeout = (1000 * 60); // 1 second
}
