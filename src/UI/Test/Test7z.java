package UI.Test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.IOUtils;

import net.miginfocom.swing.MigLayout;

public class Test7z extends JFrame {

	private JPanel contentPane;
	private JTextField textFieldArchive;
	private JTextField textFieldDestination;

	/**
	 * Create the frame.
	 */
	public Test7z() {
		setTitle("Test7Z");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 168);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][grow]", "[][][][][][][][]"));

		JLabel lblNewLabel = new JLabel("Archive Path");
		contentPane.add(lblNewLabel, "cell 0 0,alignx trailing");

		textFieldArchive = new JTextField();
		contentPane.add(textFieldArchive, "cell 1 0,growx");
		textFieldArchive.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("Destination");
		contentPane.add(lblNewLabel_1, "cell 0 1,alignx trailing");

		textFieldDestination = new JTextField();
		contentPane.add(textFieldDestination, "cell 1 1,growx");
		textFieldDestination.setColumns(10);

		JButton btnNewButton = new JButton("Go");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				go();
			}
		});
		contentPane.add(btnNewButton, "cell 0 7");

		this.setVisible(true);
	}

	private void go() {
		File oArchive = new File(textFieldArchive.getText());
		File oDestination = new File(textFieldDestination.getText(), oArchive.getName().substring(0, oArchive.getName().lastIndexOf(".")));
		if (oDestination.exists()) {
			oDestination.mkdirs();
			oDestination.mkdir();
		}
		try {
			try (SevenZFile sevenZFile = new SevenZFile(oArchive)) {
				SevenZArchiveEntry entry;
				while ((entry = sevenZFile.getNextEntry()) != null) {
					if (entry.isDirectory()) {
						continue;
					}
					File file = fileName(oDestination, entry);
					if (file == null) {
						continue;
					}
					File parent = file.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						System.out.println("FailOpenFile" + ":" + file);
						continue;
					}
					try (FileOutputStream out = new FileOutputStream(file)) {
						int length;
						byte[] buf = new byte[1024];
						while ((length = sevenZFile.read(buf)) != -1) {
							out.write(buf, 0, length);
						}
						out.close();
					}
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static File fileName(File destinationDir, ArchiveEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

}
