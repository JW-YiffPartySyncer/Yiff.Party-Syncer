package UI;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

public class DownloadCheck extends JFrame {

	private JPanel contentPane;
	public JLabel lblFile = new JLabel("NULL");
	public JLabel lblMissing = new JLabel("NULL");
	public JLabel lblTotal = new JLabel("NULL");

	/**
	 * Create the frame.
	 */
	public DownloadCheck() {
		setTitle("Yiff.Party Syncer - Checking local files for integrity");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 655, 214);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][]", "[][][]"));

		JLabel lblNewLabel = new JLabel("Current File:");
		contentPane.add(lblNewLabel, "cell 0 0,alignx right");
		contentPane.add(lblFile, "cell 1 0");

		JLabel lblNewLabel_1 = new JLabel("Missing Files:");
		contentPane.add(lblNewLabel_1, "cell 0 1,alignx right");

		contentPane.add(lblMissing, "cell 1 1");

		JLabel lblNewLabel_2 = new JLabel("Processed Files:");
		contentPane.add(lblNewLabel_2, "cell 0 2");

		contentPane.add(lblTotal, "cell 1 2");

		this.setVisible(true);
	}

}
