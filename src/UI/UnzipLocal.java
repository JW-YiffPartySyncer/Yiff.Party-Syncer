package UI;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import Logic.Workers.WorkerLocalUnzip;
import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author JW
 *
 *         just a small info panel to get stats while the programm is unzipping
 *
 */
public class UnzipLocal extends JFrame {

	private JPanel contentPane;

	public JLabel lblStatus = new JLabel("NULL");
	public JLabel lblAction = new JLabel("NULL");

	private Main oMain;
	private Thread oThread;

	/**
	 * Create the frame.
	 */
	public UnzipLocal(Main oMain) {
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
			      if(oThread != null) {
			    	  try {
						oThread.join(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			      }
		    }
		});
		
		setTitle("Searching local Files...");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 926, 177);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][]", "[][]"));

		JLabel lblNewLabel = new JLabel("Status:");
		contentPane.add(lblNewLabel, "cell 0 0,alignx right");

		contentPane.add(lblStatus, "cell 1 0");

		JLabel lblNewLabel_1 = new JLabel("Current Action:");
		contentPane.add(lblNewLabel_1, "cell 0 1");

		contentPane.add(lblAction, "cell 1 1");

		this.setVisible(true);

		this.oMain = oMain;

		WorkerLocalUnzip oW = new WorkerLocalUnzip(oMain, this);
		oThread = new Thread(oW);
		oThread.setName("WorkerLocalUnzip");
		oThread.start();
	}

}
