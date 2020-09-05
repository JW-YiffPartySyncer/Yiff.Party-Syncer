package Logic;

/**
 * 
 * @author JW
 * 
 *         This class holds information for the Download Manager and Download
 *         Worker threads. Should be self explanatory.
 *
 */
public class DownloadObject {
	public String strPath;
	public String strURL;
	public int ID;
	public int iPatreonID;
	public String strName;
	
	public int iTimeout;

	public boolean invalid = false;
	public boolean finished;
	public boolean touched;
}
