package UI.Test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import Lib.junrar.Archive;
import Lib.junrar.exception.RarException;
import net.miginfocom.swing.MigLayout;

public class TestRAR extends JFrame {

	private JPanel contentPane;
	private JTextField textFieldArchive;
	private JTextField textFieldDestination;

	/**
	 * Create the frame.
	 */
	public TestRAR() {
		setTitle("TestRAR");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][grow]", "[][][][][][][][]"));

		JLabel lblNewLabel = new JLabel("Archive:");
		contentPane.add(lblNewLabel, "cell 0 0,alignx trailing");

		textFieldArchive = new JTextField();
		contentPane.add(textFieldArchive, "cell 1 0,growx");
		textFieldArchive.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("Destination:");
		contentPane.add(lblNewLabel_1, "cell 0 1,alignx trailing,aligny top");

		textFieldDestination = new JTextField();
		contentPane.add(textFieldDestination, "cell 1 1,growx");
		textFieldDestination.setColumns(10);

		JButton btnNewButton = new JButton("Go");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				go();
			}
		});
		contentPane.add(btnNewButton, "cell 0 7");

		this.setVisible(true);
	}

	private void go() {
		String archiveName = textFieldArchive.getText();
		File oDestination = new File(textFieldDestination.getText());
		Archive archive = new Archive();
		try {
			FileInputStream fis = new FileInputStream(archiveName);
			List<String> aFiles = archive.readFileHeaders(fis);
			Collections.sort(aFiles);
			fis.close();
			
			for (String strFile : aFiles) {
				fis = new FileInputStream(archiveName);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				File oDest = new File(oDestination, strFile);
				if(!oDest.getName().contains(".")) {
					oDest.mkdirs();
					continue;
				}
				FileOutputStream oOut = new FileOutputStream(new File(oDestination, strFile));
				archive.extractFile(fis, strFile, baos);
				oOut.write(baos.toByteArray());
				baos.flush();
				oOut.close();
				baos.close();
				fis.close();
			}
		} catch (IOException | RarException e) {
			e.printStackTrace();
		}
	}

}
