package UI.Test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import Lib.mega.Mega;
import Lib.mega.MegaSession;
import Lib.mega.auth.MegaAuthCredentials;
import Lib.mega.auth.MegaAuthFolder;
import UI.Main;
import net.miginfocom.swing.MigLayout;

public class TestMega extends JFrame {

	private JPanel contentPane;
	private JTextField textFieldURL;
	private JLabel lblNewLabel_1;
	private JTextField textFieldPath;
	private JButton btnNewButton;

	private Main oMain;

	public TestMega(Main oMain) {
		this.oMain = oMain;
		initialize();
	}

	/**
	 * Create the frame.
	 */
	public void initialize() {
		setTitle("MegaDLTest");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 630, 218);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][grow]", "[][][]"));

		JLabel lblNewLabel = new JLabel("Mega URL:");
		contentPane.add(lblNewLabel, "cell 0 0,alignx trailing");

		textFieldURL = new JTextField();
		contentPane.add(textFieldURL, "cell 1 0,growx");
		textFieldURL.setColumns(10);

		lblNewLabel_1 = new JLabel("Save Path:");
		contentPane.add(lblNewLabel_1, "cell 0 1,alignx trailing,aligny baseline");

		textFieldPath = new JTextField();
		contentPane.add(textFieldPath, "cell 1 1,growx");
		textFieldPath.setColumns(10);

		btnNewButton = new JButton("Do Stuff");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				go();
			}
		});
		contentPane.add(btnNewButton, "cell 0 2");

		this.setVisible(true);
	}

	private void go() {
		try {
			MegaSession megaSession = Mega.login(new MegaAuthCredentials(oMain.oConf.strMegaUser, oMain.oConf.strMegaPW));
			// MegaSession megaSession = Mega.currentSession();
			megaSession.get(textFieldURL.getText(), textFieldPath.getText()).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

}
