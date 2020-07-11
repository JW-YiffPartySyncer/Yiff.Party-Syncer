package Logic;

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

}