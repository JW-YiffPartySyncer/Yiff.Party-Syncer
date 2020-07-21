package Logic;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 
 * @author JW
 *
 *         Utility class that holds some misc stuff
 *
 */
public class OUtil {

	public static Connection connectToMysql(String host, String database, String user, String passwd) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			String connectionCommand = "jdbc:mysql://" + host + "/" + database + "?user=" + user + "&password=" + passwd;
			Connection connection = DriverManager.getConnection(connectionCommand);
			return connection;

		} catch (Exception ex) {
			System.out.println("false");
			return null;
		}
	}

	public static void openInBrowser(String strLink) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) && !strLink.equals("")) {
			try {
				Desktop.getDesktop().browse(new URI(strLink));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	public static void openInExplorer(String strPath) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) && !strPath.equals("")) {
			try {
				Desktop.getDesktop().open(new File(strPath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean move(File sourceFile, File destFile)
	{
	    if (sourceFile.isDirectory())
	    {
	        for (File file : sourceFile.listFiles())
	        {
	            move(file, new File(destFile.getAbsolutePath() + "\\" + file.getName()));
	        }
	    }
	    else
	    {
	        try {
	        	System.out.println("move file " + sourceFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());
	        	destFile.getParentFile().mkdirs();
	            Files.move(Paths.get(sourceFile.getPath()), Paths.get(destFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
	            return true;
	        } catch (IOException e) {
	        	e.printStackTrace();
	            return false;
	        }
	    }
	    return false;
	}
}