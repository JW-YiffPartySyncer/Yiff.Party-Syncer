package UI;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import Logic.Workers.WorkerBatchPNGConverter;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;

public class BatchConvertPNGs extends JFrame {

	private JPanel contentPane;

	public JLabel lblStatus = new JLabel("NULL");
	public JLabel lblAction = new JLabel("NULL");

	private Main oMain;

	/**
	 * Create the frame.
	 */
	public BatchConvertPNGs(Main oMain) {
		this.oMain = oMain;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][]", "[][]"));

		JLabel lblNewLabel = new JLabel("Status:");
		contentPane.add(lblNewLabel, "cell 0 0");

		contentPane.add(lblStatus, "cell 1 0");

		JLabel lblNewLabel_1 = new JLabel("Action:");
		contentPane.add(lblNewLabel_1, "cell 0 1");

		contentPane.add(lblAction, "cell 1 1");

		this.setVisible(true);
		
		go();
	}
	
	private void go() {
		WorkerBatchPNGConverter oW = new WorkerBatchPNGConverter(oMain, this);
		Thread oThread = new Thread(oW);
		oThread.setName("Batch PNG Converter");
		oThread.start();
	}

}
