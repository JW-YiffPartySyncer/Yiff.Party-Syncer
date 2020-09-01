package UI;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import Logic.Config;
import Logic.OUtil;
import Logic.Workers.WorkerCreatorParser;
import Logic.Workers.WorkerDownloadManager;
import Logic.Workers.WorkerPatreonUpdater;
import Logic.Workers.WorkerUIUpdater;
import UI.Test.Test7z;
import UI.Test.TestMega;
import UI.Test.TestRAR;
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
	private JCheckBox chckbxAutoOpen = new JCheckBox("Auto-open on decision");
	private JLabel lblDLThreads = new JLabel("NULL");

	private WorkerPatreonUpdater oPatreonUpdater = new WorkerPatreonUpdater(this);
	private WorkerDownloadManager oDownloadManager = new WorkerDownloadManager(this);

	private DefaultListModel<String> listActivityModel = new DefaultListModel<>();
	private JList<String> listDownloaders = new JList<String>(listActivityModel);

	private JComboBox<String> comboBoxCategory = new JComboBox<String>();
	private ArrayList<Integer> aComboCategoryIDs = new ArrayList<Integer>();

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
		reloadCategories();
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

		chckbxAutoOpen.setSelected(oConf.bUIAutoOpen);

		JLabel lblNewLabel_7 = new JLabel("| Category:");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_7, "cell 1 1");

		frmYiffpartySyncer.getContentPane().add(comboBoxCategory, "cell 1 1");

		JMenuBar menuBar = new JMenuBar();
		frmYiffpartySyncer.setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);

		JMenuItem mntmNewMenuItem = new JMenuItem("Close");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnNewMenu.add(mntmNewMenuItem);

		JMenu mnNewMenu_1 = new JMenu("Settings");
		menuBar.add(mnNewMenu_1);

		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Open Settings Editor");
		mntmNewMenuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openSettingsEditor();
			}
		});
		mnNewMenu_1.add(mntmNewMenuItem_1);

		JMenuItem mntmNewMenuItem_2 = new JMenuItem("Manage Categories");
		mntmNewMenuItem_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openCategoryGUI();
			}
		});
		mnNewMenu_1.add(mntmNewMenuItem_2);

		JMenu mnNewMenu_2 = new JMenu("Batch Actions");
		menuBar.add(mnNewMenu_2);

		JMenuItem mntmNewMenuItem_3 = new JMenuItem("Local - Search .zip and unzip");
		mntmNewMenuItem_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				unzipLocal();
			}
		});
		mnNewMenu_2.add(mntmNewMenuItem_3);

		JMenuItem mntmNewMenuItem_6 = new JMenuItem("Batch-Convert PNGs");
		mntmNewMenuItem_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				batchConvertPNGs();
			}
		});
		mnNewMenu_2.add(mntmNewMenuItem_6);

		JMenu mnNewMenu_3 = new JMenu("Test");
		menuBar.add(mnNewMenu_3);

		JMenuItem mntmNewMenuItem_4 = new JMenuItem("Mega Download TestUI");
		mntmNewMenuItem_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testMegaUI();
			}
		});
		mnNewMenu_3.add(mntmNewMenuItem_4);

		JMenuItem mntmNewMenuItem_5 = new JMenuItem("7z Decompress TestUI");
		mntmNewMenuItem_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				test7z();
			}
		});
		mnNewMenu_3.add(mntmNewMenuItem_5);
		
		JMenuItem mntmNewMenuItem_7 = new JMenuItem("RAR Decompress TestUI");
		mntmNewMenuItem_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				testRAR();
			}
		});
		mnNewMenu_3.add(mntmNewMenuItem_7);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmYiffpartySyncer = new JFrame();
		frmYiffpartySyncer.setTitle("Yiff.Party syncer");
		frmYiffpartySyncer.setBounds(100, 100, 881, 413);
		frmYiffpartySyncer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmYiffpartySyncer.getContentPane().setLayout(new MigLayout("", "[][grow]", "[][][][][][][][][][][grow][][][]"));

		JLabel lblNewLabel = new JLabel("Do you want to download:");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel, "cell 0 0,alignx trailing");

		textFieldLink = new JTextField();
		textFieldLink.setEditable(false);
		frmYiffpartySyncer.getContentPane().add(textFieldLink, "cell 1 0,growx");
		textFieldLink.setColumns(10);

		JButton btnYes = new JButton("Yes");
		btnYes.setToolTipText("Track the Creator");
		btnYes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update(1);
			}
		});
		frmYiffpartySyncer.getContentPane().add(btnYes, "flowx,cell 0 1");

		JButton btnNewButton = new JButton("No");
		btnNewButton.setToolTipText("Creator unwanted. Don't track");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update(2);
			}
		});
		frmYiffpartySyncer.getContentPane().add(btnNewButton, "cell 0 1");

		JButton btnNewButton_1 = new JButton("Next");
		btnNewButton_1.setToolTipText("If there are unchecked Creators, will load the creator to check");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				next();
			}
		});
		frmYiffpartySyncer.getContentPane().add(btnNewButton_1, "cell 0 1");

		JButton btnNewButton_2 = new JButton("Open in Browser");
		btnNewButton_2.setToolTipText("Tries to open the link above in the standard OS Browser.");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				open();
			}
		});
		frmYiffpartySyncer.getContentPane().add(btnNewButton_2, "flowx,cell 1 1");

		JLabel lblNewLabel_1 = new JLabel("Manually add to watch:");
		lblNewLabel_1.setToolTipText("Add a Patreon manually.\r\nLink needs to be a creator main page link, for example\r\n\"https://yiff.party/patreon/xxxxxxx\"");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_1, "cell 0 2,alignx trailing");

		textFieldManual = new JTextField();
		frmYiffpartySyncer.getContentPane().add(textFieldManual, "flowx,cell 1 2,growx");
		textFieldManual.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("Status");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_2, "cell 0 4");

		JLabel lblPatreonTracking = new JLabel("Patreon Tracking:");
		lblPatreonTracking.setToolTipText(
				"Statistics about patreon tracking\r\n\r\nUnchecked: Number of Patreons that have been added by the UserScript and need manual checking\r\nTracking: Number of Patreons that are wanted and actively tracked\r\nUnwanted: Number of Patreons that are not wanted\r\n\r\nSynced: Number of up-to-date Patreons\r\nRetry: Number of Patreons that have failed their update and need re-checking");
		frmYiffpartySyncer.getContentPane().add(lblPatreonTracking, "cell 0 5");
		lblPatreons.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblPatreons.setToolTipText(
				"Statistics about patreon tracking\r\n\r\nUnchecked: Number of Patreons that have been added by the UserScript and need manual checking\r\nTracking: Number of Patreons that are wanted and actively tracked\r\nUnwanted: Number of Patreons that are not wanted\r\n\r\nSynced: Number of up-to-date Patreons\r\nRetry: Number of Patreons that have failed their update and need re-checking");

		frmYiffpartySyncer.getContentPane().add(lblPatreons, "cell 1 5");

		JLabel lblNewLabel_3 = new JLabel("Downloads:");
		lblNewLabel_3.setToolTipText(
				"Statistics about all Downloads tracked by Yiff.Party Syncer\r\n\r\nIn DB: Number of total Downloads in Database\r\nDownloaded: Number of Downloads that are completed to disk\r\nFailed: Number of Downloads that have failed in certain ways. Will be retried at the end of the Download List\r\nFNF: FileNotFound, the Download is no longer available on Yiff.Party. Most likely the Creator got excluded.");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_3, "cell 0 6");
		lblDownloads.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblDownloads.setToolTipText(
				"Statistics about all Downloads tracked by Yiff.Party Syncer\r\n\r\nIn DB: Number of total Downloads in Database\r\nDownloaded: Number of Downloads that are completed to disk\r\nFailed: Number of Downloads that have failed in certain ways. Will be retried at the end of the Download List\r\nFNF: FileNotFound, the Download is no longer available on Yiff.Party. Most likely the Creator got excluded.");

		frmYiffpartySyncer.getContentPane().add(lblDownloads, "cell 1 6");

		JLabel lblNewLabel_4 = new JLabel("Download Buffer:");
		lblNewLabel_4.setToolTipText(
				"Total/Unused - Maximum buffered downloads\r\n\r\nThis stat shows how many pending downloads are buffered.\r\nThe unused number represents how many buffers are free, the Total number represents how many buffers there are total.\r\nEvery Download Thread can use a buffer object, so at maximum the unused objects should amount to (Maximum Download Buffers - Amount of Download Threads)");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_4, "cell 0 7");
		lblDownloadBuffer.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblDownloadBuffer.setToolTipText(
				"Total/Unused - Maximum buffered downloads\r\n\r\nThis stat shows how many pending downloads are buffered.\r\nThe unused number represents how many buffers are free, the Total number represents how many buffers there are total.\r\nEvery Download Thread can use a buffer object, so at maximum the unused objects should amount to (Maximum Download Buffers - Amount of Download Threads)");

		frmYiffpartySyncer.getContentPane().add(lblDownloadBuffer, "cell 1 7");

		JLabel lblNewLabel_5 = new JLabel("Patreon Parser:");
		lblNewLabel_5.setToolTipText("Status of the WorkerPatreonUpdater Worker Thread. Idles for one minute between updates, to not create unnescessary load on yiff.party.");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_5, "cell 0 8");
		lblPatreonParser.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblPatreonParser.setToolTipText("Status of the WorkerPatreonUpdater Worker Thread. Idles for one minute between updates, to not create unnescessary load on yiff.party.");

		frmYiffpartySyncer.getContentPane().add(lblPatreonParser, "cell 1 8");

		JLabel lblNewLabel_6 = new JLabel("DL Threads:");
		frmYiffpartySyncer.getContentPane().add(lblNewLabel_6, "flowx,cell 0 9");

		lblDLThreads.setFont(new Font("Monospaced", Font.PLAIN, 12));
		frmYiffpartySyncer.getContentPane().add(lblDLThreads, "cell 1 9");

		JScrollPane scrollPane = new JScrollPane();
		frmYiffpartySyncer.getContentPane().add(scrollPane, "cell 0 10 2 1,grow");
		listDownloaders.setEnabled(false);
		listDownloaders.setFont(new Font("Monospaced", Font.PLAIN, 12));

		scrollPane.setViewportView(listDownloaders);

		JButton btnAddThread = new JButton("+");
		btnAddThread.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addDLThread();
			}
		});
		btnAddThread.setFont(new Font("Monospaced", Font.PLAIN, 10));
		frmYiffpartySyncer.getContentPane().add(btnAddThread, "cell 0 9");

		JButton btnRemThread = new JButton("-");
		btnRemThread.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeDLThread();
			}
		});
		btnRemThread.setFont(new Font("Monospaced", Font.PLAIN, 10));
		frmYiffpartySyncer.getContentPane().add(btnRemThread, "cell 0 9");

		chckbxAutoOpen.setToolTipText(
				"If checked, the next queued Patreon to Check will automatically open in the default OS Browser. Basically replaces the Button \"Open in Browser\"");
		frmYiffpartySyncer.getContentPane().add(chckbxAutoOpen, "cell 1 1");

		JButton btnNewButton_3 = new JButton("Add");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				add();
			}
		});
		frmYiffpartySyncer.getContentPane().add(btnNewButton_3, "cell 1 2");

		connection = OUtil.connectToMysql(oConf.strDBHost, oConf.strDBDatabase, oConf.strDBUser, oConf.strDBPassword);
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
				statement.executeUpdate("UPDATE patreons SET wanted = " + i + ", category = " + (i == 1 ? aComboCategoryIDs.get(comboBoxCategory.getSelectedIndex()) : 1)
						+ " WHERE ID = " + iCurrentLink);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		next();
		if (chckbxAutoOpen.isSelected()) {
			open();
		}
	}

	/**
	 * Invoked by the "Add" button. Manually add a yiff.party patreon to the
	 * database to track.
	 */
	private void add() {
		if (!textFieldManual.getText().equals("")) {
			String strLink = textFieldManual.getText();
			URL u;
			Boolean bSuccess = true;
			try {
				u = new URL(strLink);
				u.toURI();
			} catch (MalformedURLException e1) {
				bSuccess = false;
				e1.printStackTrace();
			} catch (URISyntaxException e) {
				bSuccess = false;
				e.printStackTrace();
			}

			if (bSuccess && strLink.contains("yiff.party/patreon")) {
				try {
					resultSet = statement.executeQuery("SELECT COUNT('ID') FROM patreons WHERE link = '" + strLink + "'");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if (resultSet != null) {
					try {
						if (resultSet.next()) {
							if (resultSet.getInt(1) == 0) {
								statement.executeUpdate("INSERT INTO patreons (link, wanted, category) VALUES ('" + strLink + "', 1, "
										+ aComboCategoryIDs.get(comboBoxCategory.getSelectedIndex()) + ")");
							} else {
								statement.executeUpdate("UPDATE patreons SET wanted = 1, category = " + aComboCategoryIDs.get(comboBoxCategory.getSelectedIndex())
										+ " WHERE link = '" + strLink + "'");
							}
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		textFieldManual.setText("");
	}

	/**
	 * Reloads patreon tracking statistics
	 */
	public void updatePatreonTracking() {
		StringBuilder sb = new StringBuilder();
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
					sb.append("Unchecked: " + resultSetOwn.getInt(1));
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM patreons WHERE wanted = 1");
				if (resultSetOwn.next()) {
					sb.append("| Tracking: " + resultSetOwn.getInt(1));
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM patreons WHERE wanted = 2");
				if (resultSetOwn.next()) {
					sb.append("| Unwanted: " + resultSetOwn.getInt(1));
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM patreons WHERE success = 1");
				if (resultSetOwn.next()) {
					sb.append("| Status: " + resultSetOwn.getInt(1) + " synced / ");
				}
				resultSetOwn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ResultSet resultSetOwn = statementOwn.executeQuery("SELECT COUNT('ID') FROM patreons WHERE success = 0 AND last_checked != 0");
				if (resultSetOwn.next()) {
					sb.append(resultSetOwn.getInt(1) + " retry");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		lblPatreons.setText(sb.toString());
		try {
			statementOwn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
		lblDownloadBuffer.setText(oDownloadManager.aDOs.size() + "/" + oDownloadManager.aQueue.size() + " - max: " + oConf.iDLBuffer);
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
		lblDLThreads.setText(oDownloadManager.aDownloaders.size() + " Threads active, " + (oDownloadManager.iWorkerIndex + 1) + " Threads wanted");
	}

	/**
	 * Attempts to spawn a new DownloadWorker
	 */
	private void addDLThread() {
		oDownloadManager.startWorker();
	}

	/**
	 * Attempts to close a DownloadWorker after its current download
	 */
	private void removeDLThread() {
		oDownloadManager.stopWorker();
	}

	/**
	 * Invoked by the "Open in browser" button. Starts a system call to open the
	 * loaded patreon in the default browser
	 */
	private void open() {
		OUtil.openInBrowser(textFieldLink.getText());
	}

	/**
	 * Opens the Category GUI
	 */
	private void openCategoryGUI() {
		Category oC = new Category(this);
	}

	/**
	 * Launches Settings GUI
	 */
	private void openSettingsEditor() {
		Settings oSettings = new Settings(this);
	}

	/**
	 * Tries to invalidate all running and buffered DownloadObjects that are
	 * designated to the given patreon
	 */
	public void invalidatePatreonID(int iID) {
		oDownloadManager.invalidate(iID);
	}

	/**
	 * Reloads categories from DB. Public because gets called from Category UI
	 */
	public void reloadCategories() {
		comboBoxCategory.removeAllItems();
		aComboCategoryIDs.clear();
		try {
			resultSet = statement.executeQuery("SELECT * FROM categories");
			if (resultSet != null) {
				if (resultSet.next()) {
					do {
						comboBoxCategory.addItem(resultSet.getString("name"));
						aComboCategoryIDs.add(resultSet.getInt("ID"));
					} while (resultSet.next());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Spawns the Mega.nz Test UI
	 */
	private void testMegaUI() {
		TestMega oT = new TestMega(this);
	}

	/**
	 * Spawns the 7Zip Unpack Test UI
	 */
	private void test7z() {
		Test7z oT = new Test7z();
	}

	/**
	 * Tries to start the Batch-unzip worker
	 */
	private void unzipLocal() {
		UnzipLocal oU = new UnzipLocal(this);
	}

	private void batchConvertPNGs() {
		BatchConvertPNGs oBCPNGs = new BatchConvertPNGs(this);
	}
	
	private void testRAR() {
		TestRAR oRAR = new TestRAR();
	}

}
