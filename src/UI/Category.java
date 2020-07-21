package UI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import Logic.OUtil;
import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author JW
 * 
 *         In this application window, we can define new Categories and
 *         associated Subdirectories. Also, we can recategorize Patreons.
 *
 */
public class Category extends JFrame {

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;

	private Main oMain;
	private JPanel contentPane;

	private JComboBox<String> comboBoxCategories = new JComboBox<String>();
	private JTextField textFieldName;
	private JTextField textFieldSubfolder;
	private JComboBox<String> comboBoxPatreons = new JComboBox<String>();
	private JComboBox<String> comboBoxPatreonCategory = new JComboBox<String>();

	private ArrayList<Integer> aComboboxCategoriesIDs = new ArrayList<Integer>();
	private ArrayList<Integer> aComboboxPatreonsIDs = new ArrayList<Integer>();

	/**
	 * Create the frame.
	 */
	public Category(Main oMain) {
		this.oMain = oMain;
		setTitle("Categories");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 852, 410);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][grow]", "[][][][][][][][]"));

		JLabel lblNewLabel = new JLabel("Category:");
		contentPane.add(lblNewLabel, "cell 0 0,alignx trailing");
		comboBoxCategories.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				reloadInfos();
			}
		});

		contentPane.add(comboBoxCategories, "cell 1 0,growx");

		JLabel lblNewLabel_1 = new JLabel("Name:");
		contentPane.add(lblNewLabel_1, "cell 0 1,alignx trailing");

		textFieldName = new JTextField();
		contentPane.add(textFieldName, "cell 1 1,growx");
		textFieldName.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("Subfolder:");
		contentPane.add(lblNewLabel_2, "cell 0 2,alignx trailing");

		textFieldSubfolder = new JTextField();
		contentPane.add(textFieldSubfolder, "cell 1 2,growx");
		textFieldSubfolder.setColumns(10);

		JButton btnNewButton = new JButton("Add");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addCategory();
			}
		});
		contentPane.add(btnNewButton, "flowx,cell 1 3");

		JButton btnNewButton_1 = new JButton("Edit");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				edit();
			}
		});
		contentPane.add(btnNewButton_1, "cell 1 3,aligny top");

		JButton btnNewButton_2 = new JButton("ReloadPatreonsFromCategory");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadPatreons();
			}
		});
		contentPane.add(btnNewButton_2, "flowx,cell 1 4");

		JLabel lblNewLabel_3 = new JLabel("Patreons:");
		contentPane.add(lblNewLabel_3, "cell 0 5,alignx trailing");

		contentPane.add(comboBoxPatreons, "cell 1 5,growx");

		JButton btnNewButton_3 = new JButton("OpenOn Yiff.Party");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openYiffParty();
			}
		});
		contentPane.add(btnNewButton_3, "cell 1 4");

		JButton btnNewButton_4 = new JButton("Open Folder");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openFolder();
			}
		});
		contentPane.add(btnNewButton_4, "cell 1 4");

		JButton btnNewButton_5 = new JButton("SelectRandom");
		btnNewButton_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectRandom();
			}
		});
		contentPane.add(btnNewButton_5, "cell 1 4");

		JLabel lblNewLabel_4 = new JLabel("Category:");
		contentPane.add(lblNewLabel_4, "cell 0 6,alignx trailing");

		contentPane.add(comboBoxPatreonCategory, "cell 1 6,growx");

		JButton btnNewButton_6 = new JButton("Recategorize&Move");
		btnNewButton_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				recatAndMove();
			}
		});
		contentPane.add(btnNewButton_6, "cell 1 7");

		connection = OUtil.connectToMysql(oMain.oConf.strDBHost, oMain.oConf.strDBDatabase, oMain.oConf.strDBUser, oMain.oConf.strDBPassword);
		try {
			statement = connection.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		initialize();

		this.setVisible(true);
	}

	private void initialize() {
		comboBoxCategories.removeAllItems();
		aComboboxCategoriesIDs.clear();
		textFieldName.setText("");
		textFieldSubfolder.setText("");
		comboBoxPatreonCategory.removeAllItems();
		try {
			resultSet = statement.executeQuery("SELECT * FROM categories");
			if (resultSet != null) {
				if (resultSet.next()) {
					do {
						comboBoxCategories.addItem(resultSet.getString("name"));
						comboBoxPatreonCategory.addItem(resultSet.getString("name"));
						if (textFieldName.getText().equals("")) {
							textFieldName.setText(resultSet.getString("name"));
							textFieldSubfolder.setText(resultSet.getString("path"));
						}
						aComboboxCategoriesIDs.add(resultSet.getInt("ID"));
					} while (resultSet.next());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * When the user selects a Category from the dropdown box, load Infos from
	 * Database
	 */
	private void reloadInfos() {
		textFieldName.setText("");
		textFieldSubfolder.setText("");
		if (aComboboxCategoriesIDs.size() != 0 && comboBoxCategories.getSelectedIndex() != -1) {
			try {
				resultSet = statement.executeQuery("SELECT * FROM categories WHERE ID = " + aComboboxCategoriesIDs.get(comboBoxCategories.getSelectedIndex()));
				if (resultSet != null) {
					if (resultSet.next()) {
						textFieldName.setText(resultSet.getString("name"));
						textFieldSubfolder.setText(resultSet.getString("path"));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Try to add the category
	 */
	private void addCategory() {
		// Check if entry already exists
		try {
			resultSet = statement.executeQuery("SELECT * FROM categories WHERE name = '" + textFieldName.getText() + "' AND path = '"
					+ textFieldSubfolder.getText().replaceAll("\\\\", "\\\\\\\\") + "' LIMIT 1");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		boolean bFound = false;
		if (resultSet != null) {
			try {
				if (resultSet.next()) {
					bFound = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (!bFound) {
			try {
				statement.executeUpdate(
						"INSERT INTO categories (name, path) VALUES ('" + textFieldName.getText() + "', '" + textFieldSubfolder.getText().replaceAll("\\\\", "\\\\\\\\") + "')");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		initialize();
	}

	/**
	 * Edit name or path of the currently selected category
	 */
	private void edit() {
		try {
			statement.executeUpdate("UPDATE categories SET name = '" + textFieldName.getText() + "', path = '" + textFieldSubfolder.getText().replaceAll("\\\\", "\\\\\\\\")
					+ "' WHERE ID = " + aComboboxCategoriesIDs.get(comboBoxCategories.getSelectedIndex()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tries to load all Patreons in the selected category
	 */
	private void loadPatreons() {
		comboBoxPatreons.removeAllItems();
		aComboboxPatreonsIDs.clear();
		try {
			resultSet = statement.executeQuery("SELECT * FROM patreons WHERE category = " + aComboboxCategoriesIDs.get(comboBoxCategories.getSelectedIndex())
					+ (comboBoxCategories.getSelectedIndex() == 0 ? " OR category = 0 " : "") + " AND name != '' ORDER BY name ASC");
			if (resultSet != null) {
				while (resultSet.next()) {
					comboBoxPatreons.addItem(resultSet.getString("name"));
					aComboboxPatreonsIDs.add(resultSet.getInt("ID"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tries to open the selected Patreon on Yiff.Party
	 */
	private void openYiffParty() {
		if (comboBoxPatreons.getSelectedIndex() != -1) {
			try {
				resultSet = statement.executeQuery("SELECT * FROM patreons WHERE ID = " + aComboboxPatreonsIDs.get(comboBoxPatreons.getSelectedIndex()));
				if (resultSet != null) {
					if (resultSet.next()) {
						OUtil.openInBrowser(resultSet.getString("link"));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Tries to open the selected Patreon on local disk
	 */
	private void openFolder() {
		if (comboBoxPatreons.getSelectedIndex() != -1) {
			try {
				resultSet = statement.executeQuery("SELECT * FROM patreons WHERE ID = " + aComboboxPatreonsIDs.get(comboBoxPatreons.getSelectedIndex()));
				if (resultSet != null) {
					if (resultSet.next()) {
						String strName = resultSet.getString("name");

						resultSet = statement.executeQuery("SELECT * FROM categories WHERE ID = " + (resultSet.getInt("category") == 0 ? "1" : resultSet.getInt("category")));
						if (resultSet != null) {
							if (resultSet.next()) {
								OUtil.openInExplorer(oMain.oConf.strSavepath + resultSet.getString("path") + strName);
							}
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Change Category of selected Patreon. Move it at the end and reload all
	 * Information
	 */
	private void recatAndMove() {
		String strOldCatPath = comboBoxCategories.getItemAt(comboBoxCategories.getSelectedIndex());
		String strNewCatPath = comboBoxPatreonCategory.getItemAt(comboBoxPatreonCategory.getSelectedIndex());
		try {
			resultSet = statement.executeQuery("SELECT * FROM patreons WHERE ID = " + aComboboxPatreonsIDs.get(comboBoxPatreons.getSelectedIndex()));
			if (resultSet != null && resultSet.next()) {
				File oOldPath = new File(oMain.oConf.strSavepath + strOldCatPath + "\\" + resultSet.getString("name"));
				File oDest = new File(oMain.oConf.strSavepath + strNewCatPath + "\\" + resultSet.getString("name"));
				boolean bSuccess = false;
				if (oOldPath.exists()) {
					try {
						OUtil.move(oOldPath, oDest);
						bSuccess = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					bSuccess = false;
				}

				if (bSuccess) {
					try {
						statement.executeUpdate("UPDATE patreons SET category = " + aComboboxCategoriesIDs.get(comboBoxPatreonCategory.getSelectedIndex()) + " WHERE ID = "
								+ aComboboxPatreonsIDs.get(comboBoxPatreons.getSelectedIndex()));
						oMain.invalidatePatreonID(aComboboxPatreonsIDs.get(comboBoxPatreons.getSelectedIndex()));
						oOldPath.delete();
						loadPatreons();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Selects a random Patreon from the loaded list
	 */
	private void selectRandom() {
		if (comboBoxPatreons.getItemCount() >= 1) {
			Random r = new Random();
			comboBoxPatreons.setSelectedIndex(r.nextInt(comboBoxPatreons.getItemCount()));
		}
	}

}