package UI;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 
 * @author JW
 * 
 *         A Settings GUI.
 *
 */
public class Settings extends JFrame {

	private JPanel contentPane;
	private JTextField textFieldDBHostname;
	private JTextField textFieldDBName;
	private JTextField textFieldUsername;
	private JTextField textFieldPassword;
	private JSpinner spinnerDLThreads = new JSpinner();
	private JCheckBox chckbxAutoOpen = new JCheckBox("Auto-Open default state");
	private JSpinner spinnerJPGQ = new JSpinner();
	private JCheckBox chckbxConvertPNG = new JCheckBox("Automatically convert PNGs to High Quality JPGs");

	private Main oMain;
	private JTextField textFieldSavepath;

	public Settings(Main oMain) {
		this.oMain = oMain;
		initialize();
	}

	/**
	 * Load all data from Config into UI
	 */
	private void initialize() {
		setTitle("Yiff.Party Syncer Settings");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 775, 367);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][grow]", "[][][][][][][][][][][][]"));

		JLabel lblNewLabel = new JLabel("Database Settings");
		contentPane.add(lblNewLabel, "cell 0 0");

		JLabel lblNewLabel_1 = new JLabel("Host:");
		contentPane.add(lblNewLabel_1, "cell 0 1,alignx trailing");

		textFieldDBHostname = new JTextField();
		contentPane.add(textFieldDBHostname, "cell 1 1,growx");
		textFieldDBHostname.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("Database:");
		contentPane.add(lblNewLabel_2, "cell 0 2,alignx trailing");

		textFieldDBName = new JTextField();
		contentPane.add(textFieldDBName, "cell 1 2,growx");
		textFieldDBName.setColumns(10);

		JLabel lblNewLabel_3 = new JLabel("Username:");
		contentPane.add(lblNewLabel_3, "cell 0 3,alignx trailing");

		textFieldUsername = new JTextField();
		contentPane.add(textFieldUsername, "cell 1 3,growx");
		textFieldUsername.setColumns(10);

		JLabel lblNewLabel_4 = new JLabel("Password:");
		contentPane.add(lblNewLabel_4, "cell 0 4,alignx trailing");

		textFieldPassword = new JTextField();
		contentPane.add(textFieldPassword, "cell 1 4,growx");
		textFieldPassword.setColumns(10);

		JLabel lblNewLabel_5 = new JLabel("Downloader Settings");
		contentPane.add(lblNewLabel_5, "cell 0 5");

		JLabel lblNewLabel_6 = new JLabel("Default # of DL Threads:");
		contentPane.add(lblNewLabel_6, "flowx,cell 0 6,alignx trailing,aligny baseline");

		spinnerDLThreads.setModel(new SpinnerNumberModel(0, 0, 6, 1));
		contentPane.add(spinnerDLThreads, "flowx,cell 1 6");

		JLabel lblNewLabel_7 = new JLabel("UI Settings");
		contentPane.add(lblNewLabel_7, "cell 0 7");

		contentPane.add(chckbxAutoOpen, "cell 0 8");

		JButton btnNewButton = new JButton("Save");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});

		JLabel lblNewLabel_8 = new JLabel("Save Path (absolute):");
		contentPane.add(lblNewLabel_8, "cell 0 10,alignx trailing");

		textFieldSavepath = new JTextField();
		contentPane.add(textFieldSavepath, "cell 1 10,growx");
		textFieldSavepath.setColumns(10);
		contentPane.add(btnNewButton, "cell 1 11,alignx right");
		contentPane.add(chckbxConvertPNG, "cell 1 6");
		
		JLabel lblNewLabel_9 = new JLabel("| JPG Quality:");
		contentPane.add(lblNewLabel_9, "cell 1 6");
		
		spinnerJPGQ.setModel(new SpinnerNumberModel(new Float(0), new Float(0), new Float(100), new Float(1)));
		contentPane.add(spinnerJPGQ, "cell 1 6");

		textFieldDBHostname.setText(oMain.oConf.strDBHost);
		textFieldDBName.setText(oMain.oConf.strDBDatabase);
		textFieldUsername.setText(oMain.oConf.strDBUser);
		textFieldPassword.setText(oMain.oConf.strDBPassword);

		spinnerDLThreads.setValue(oMain.oConf.iNumDLWorkers);
		chckbxConvertPNG.setSelected(oMain.oConf.bDLWConvertPNGs);
		spinnerJPGQ.setValue((int) (oMain.oConf.fDLWJPGQuality * 100.0));

		chckbxAutoOpen.setSelected(oMain.oConf.bUIAutoOpen);

		textFieldSavepath.setText(oMain.oConf.strSavepath);

		this.setVisible(true);
	}

	/**
	 * Save data back to Config
	 */
	private void save() {
		oMain.oConf.strDBHost = textFieldDBHostname.getText();
		oMain.oConf.strDBDatabase = textFieldDBName.getText();
		oMain.oConf.strDBUser = textFieldUsername.getText();
		oMain.oConf.strDBPassword = textFieldPassword.getText();

		oMain.oConf.iNumDLWorkers = (int) spinnerDLThreads.getValue();
		oMain.oConf.bDLWConvertPNGs = chckbxConvertPNG.isSelected();
		oMain.oConf.fDLWJPGQuality = ((float) spinnerJPGQ.getValue()) / 100.0f;

		oMain.oConf.bUIAutoOpen = chckbxAutoOpen.isSelected();

		oMain.oConf.strSavepath = textFieldSavepath.getText();

		JOptionPane.showMessageDialog(this, "Some values need a restart");

		oMain.oConf.saveData();

		this.dispose();
	}

}
