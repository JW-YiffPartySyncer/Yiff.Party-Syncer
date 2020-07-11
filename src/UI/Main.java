package UI;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import Logic.Config;
import Logic.OUtil;
import Logic.Workers.WorkerCreatorParser;
import Logic.Workers.WorkerDownloadManager;
import Logic.Workers.WorkerPatreonUpdater;
import Logic.Workers.WorkerUIUpdater;
import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author JW
 *
 *         main interface and main starting class. Build with WindowBuilder
 *         cause I am lazy.
 *
 */
public class Main {
	public Config oConf = new Config();

	private JFrame frmYiffpartySyncer;

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;
	private JTextField textFieldLink;

	private int iCurrentLink = 0;
	private JTextField textFieldManual;
	private JLabel lblPatreons = new JLabel("NULL");
	private JLabel lblDownloads = new JLabel("NULL");
	private JLabel lblDownloadBuffer = new JLabel("NULL");
	private JLabel lblPatreonParser = new JLabel("NULL");

	private WorkerPatreonUpdater oPatreonUpdater = new WorkerPatreonUpdater(this);
	private WorkerDownloadManager oDownloadManager = new WorkerDownloadManager(this);

	private DefaultListModel<String> listActivityModel = new DefaultListModel<>();
	private JList<String> listDownloaders = new JList<String>(listActivityModel);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmYiffpartySyncer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
		updatePatreonTracking();

		WorkerCreatorParser oW = new WorkerCreatorParser(this);
		Thread oThread = new Thread(oW);
		oThread.setName("WorkerCreatorParser");
		oThread.start();

		Thread oThread2 = new Thread(oPatreonUpdater);
		oThread2.setName("WorkerPatreonUpdater");
		oThread2.start();

		Thread oThread3 = new Thread(oDownloadManager);
		oThread3.setName("WorkerDownloadManager");
		oThread3.start();

		WorkerUIUpdater oW4 = new WorkerUIUpdater(this);
		Thread oThread4 = new Thread(oW4);
		oThread4.setName("WorkerUIUpdater");
		oThread4.start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmYiffpartySyncer = new JFrame();
		frmYiffpartySyncer.setTitle("Yiff.Party syncer");
		frmYiffpartySyncer.setBounds(100, 100, 881, 402);
		frmYiffpartySyncer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmYiffpartySyncer.getContentPane().setLayout(new MigLayout("", "[][grow]", "[][][][][][][][][][][grow][][][]"));

		JLabel lblNewLabel = new JLabel("Do you want to download:");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel, "cell 0 0,alignx trailing");

		textFieldLink = new JTextField();
		textFieldLink.setEditable(false);
		frmYiffpartySyncer.getContentPane().add(textFieldLink, "cell 1 0,growx");
		textFieldLink.setColumns(10);

		JButton btnYes = new JButton("Yes");
		btnYes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update(1);
			}
		});
		frmYiffpartySyncer.getContentPane().add(btnYes, "flowx,cell 0 1");

		JButton btnNewButton = new JButton("No");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update(2);
			}
		});
		frmYiffpartySyncer.getContentPane().add(btnNewButton, "cell 0 1");

		JButton btnNewButton_1 = new JButton("Next");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				next();
			}
		});
		frmYiffpartySyncer.getContentPane().add(btnNewButton_1, "cell 0 1");

		JButton btnNewButton_2 = new JButton("Open in Browser");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				open();
			}
		});
		frmYiffpartySyncer.getContentPane().add(btnNewButton_2, "cell 1 1");

		JLabel lblNewLabel_1 = new JLabel("Manually add to watch:");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_1, "cell 0 2,alignx trailing");

		textFieldManual = new JTextField();
		frmYiffpartySyncer.getContentPane().add(textFieldManual, "cell 1 2,growx");
		textFieldManual.setColumns(10);

		JButton btnNewButton_3 = new JButton("Add");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				add();
			}
		});
		frmYiffpartySyncer.getContentPane().add(btnNewButton_3, "cell 0 3");

		JLabel lblNewLabel_2 = new JLabel("Status");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_2, "cell 0 4");

		JLabel lblPatreonTracking = new JLabel("Patreon Tracking:");
		frmYiffpartySyncer.getContentPane().add(lblPatreonTracking, "cell 0 5");

		frmYiffpartySyncer.getContentPane().add(lblPatreons, "cell 1 5");

		JLabel lblNewLabel_3 = new JLabel("Downloads:");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_3, "cell 0 6");

		frmYiffpartySyncer.getContentPane().add(lblDownloads, "cell 1 6");

		JLabel lblNewLabel_4 = new JLabel("Download Buffer:");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_4, "cell 0 7");

		frmYiffpartySyncer.getContentPane().add(lblDownloadBuffer, "cell 1 7");

		JLabel lblNewLabel_5 = new JLabel("Patreon Parser:");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_5, "cell 0 8");

		frmYiffpartySyncer.getContentPane().add(lblPatreonParser, "cell 1 8");

		JLabel lblNewLabel_6 = new JLabel("Download Threads:");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_6, "cell 0 9");

		JScrollPane scrollPane = new JScrollPane();
		frmYiffpartySyncer.getContentPane().add(scrollPane, "cell 0 10 2 1,grow");
		listDownloaders.setEnabled(false);
		listDownloaders.setFont(new Font("Monospaced", Font.PLAIN, 12));

		scrollPane.setViewportView(listDownloaders);

		connection = OUtil.connectToMysql("localhost", "yiffparty", "client", "keins");
		if (connection != null) {
			try {
				statement = connection.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Main: Connection = null... somethings gone horribly wrong.");
		}
	}

	/**
	 * Invoked by the "Next" button. Loads the next Patreon that needs to be checked
	 */
	private void next() {
		textFieldLink.setText("");
		try {
			resultSet = statement.executeQuery("SELECT * FROM patreons WHERE wanted = 0 LIMIT 1");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (resultSet != null) {
			try {
				if (resultSet.next()) {
					iCurrentLink = resultSet.getInt("ID");
					textFieldLink.setText(resultSet.getString("link"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		updatePatreonTracking();
	}

	/**
	 * Invoked by the "Yes" or "No" button. Saves the user decision in the DB,
	 * whether the user wants to track the loaded patreon or not
	 * 
	 * @param i - 1 = wanted; 2 = not wanted, gets saved in SQL column
	 *          patreons.wanted
	 */
	private void update(int i) {
		if (iCurrentLink != 0) {
			try {
				statement.executeUpdate("UPDATE patreons SET wanted = " + i + " WHERE ID = " + iCurrentLink);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		next();
	}

	/**
	 * Invoked by the "Add" button. Manually add a yiff.party patreon to the
	 * database to track.
	 */
	private void add() {
		if (!textFieldManual.getText().equals("")) {
			try {
				resultSet = statement.executeQuery("SELECT COUNT('ID') FROM patreons WHERE link = '" + textFieldManual.getText() + "'");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (resultSet != null) {
				try {
					if (resultSet.next()) {
						if (resultSet.getInt(1) == 0) {
							statement.executeUpdate("INSERT INTO patreons (link, wanted) VALUES ('" + textFieldManual.getText() + "', 1)");
						}
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			textFieldManual.setText("");
		}
	}

	/**
	 * Reloads patreon tracking statistics
	 */
	public void updatePatreonTracking() {
		String strResult = "";
		Statement statementOwn = null;
		try {
			statementOwn = connection.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		if (statementOwn != null) {
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM patreons WHERE wanted = 0");
				if (resultSetOwn.next()) {
					strResult = strResult + "Unchecked: " + resultSetOwn.getInt(1);
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM patreons WHERE wanted = 1");
				if (resultSetOwn.next()) {
					strResult = strResult + "| Tracking: " + resultSetOwn.getInt(1);
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM patreons WHERE wanted = 2");
				if (resultSetOwn.next()) {
					strResult = strResult + "| Unwanted: " + resultSetOwn.getInt(1);
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM patreons WHERE success = 1");
				if (resultSetOwn.next()) {
					strResult = strResult + "| Status: " + resultSetOwn.getInt(1) + " synced / ";
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM patreons WHERE success = 0 AND last_checked != 0");
				if (resultSetOwn.next()) {
					strResult = strResult + resultSetOwn.getInt(1) + " retry";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			statementOwn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		lblPatreons.setText(strResult);
	}

	/**
	 * Reloads file download statistics
	 */
	public void updateDownloads() {
		String strResult = "";
		Statement statementOwn = null;
		try {
			statementOwn = connection.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		if (statementOwn != null) {
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM posts");
				if (resultSetOwn.next()) {
					strResult = strResult + "In DB: " + resultSetOwn.getInt(1);
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM posts WHERE downloaded = TRUE");
				if (resultSetOwn.next()) {
					strResult = strResult + "| Downloaded: " + resultSetOwn.getInt(1);
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM posts WHERE downloaded = FALSE AND last_checked != 0");
				if (resultSetOwn.next()) {
					strResult = strResult + "| Failed (in retry list): " + resultSetOwn.getInt(1);
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM posts WHERE downloaded = 2 AND last_checked != 0");
				if (resultSetOwn.next()) {
					strResult = strResult + "| FNF: " + resultSetOwn.getInt(1);
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		lblDownloads.setText(strResult);
	}

	/**
	 * Update current RAM usage in window title
	 */
	public void updateRAM() {
		frmYiffpartySyncer.setTitle("Yiff.party Syncer v" + oConf.strVersion + " - " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024)
				+ "/" + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB");
	}

	/**
	 * Update DownloadManager download buffer usage in UI
	 */
	public void updateDownloadBuffer() {
		lblDownloadBuffer.setText(oDownloadManager.aDOs.size() + "/" + oDownloadManager.aQueue.size());
	}

	/**
	 * Update patreon parser status in UI
	 */
	public void updatePatreonParser() {
		lblPatreonParser.setText(oPatreonUpdater.strStatus + ", since " + ((System.currentTimeMillis() - oPatreonUpdater.lTimestamp) / 1000) + " seconds");
	}

	/**
	 * Update the status of all download worker threads in UI
	 */
	public void updateDownloadThreads() {
		listActivityModel.removeAllElements();
		for (int i = 0; i < oDownloadManager.aDownloaders.size(); i++) {
			listActivityModel.addElement(oDownloadManager.aDownloaders.get(i).strStatus + ", since "
					+ ((System.currentTimeMillis() - oDownloadManager.aDownloaders.get(i).lTimestamp) / 1000) + " seconds");
		}
	}

	/**
	 * Invoked by the "Open in browser" button. Starts a system call to open the
	 * loaded patreon in the default browser
	 */
	private void open() {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) && !textFieldLink.getText().equals("")) {
			try {
				Desktop.getDesktop().browse(new URI(textFieldLink.getText()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

}
