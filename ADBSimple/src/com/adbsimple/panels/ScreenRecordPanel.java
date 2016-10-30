package com.adbsimple.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.adbsimple.interfaces.Const;
import com.adbsimple.util.Utils;

public class ScreenRecordPanel extends JPanel implements Const,
		ComponentListener, ActionListener {
	private JButton startRecordJB, stopRecordJB, saveJB;
	private JComboBox<String> deviceList;
	private HashMap<String, String> deviceSerial;
	private Runtime runTime;
	private Process process;
	private ExecutorService executorService;
	private String fileName;
	// private VideoPreviewPanel videoPreviewPanel;
	// private JButton playJB, pauseJB, stopJB;
	private File temp_file;
	private int videoWidth = 400, videoHeight = 580;

	public ScreenRecordPanel() {
		setVisible(true);
		setLayout(null);
		addComponentListener(this);
		Border border = BorderFactory.createEtchedBorder();
		Border border1 = BorderFactory
				.createTitledBorder(border, "Record Screen Video",
						TitledBorder.CENTER, TitledBorder.CENTER);
		setBorder(border1);

		init();
		addComponents();

		startRecordJB.addActionListener(this);
		stopRecordJB.addActionListener(this);
		saveJB.addActionListener(this);
		// playJB.addActionListener(this);
		// pauseJB.addActionListener(this);
		// stopJB.addActionListener(this);
	}

	private void init() {
		startRecordJB = new JButton("Start");
		stopRecordJB = new JButton("Stop");
		saveJB = new JButton("Save");

		/*
		 * playJB = new JButton(); pauseJB = new JButton(); stopJB = new
		 * JButton(); playJB.setActionCommand("play");
		 * pauseJB.setActionCommand("pause");
		 * stopJB.setActionCommand("stop_play");
		 */

		// setIcons();
		setEnable(false);

		deviceList = new JComboBox<String>();
		deviceSerial = new HashMap<>();
		// videoPreviewPanel = new VideoPreviewPanel(videoWidth, videoHeight);

		runTime = Runtime.getRuntime();
	}

	private void setEnable(boolean val) {
		/*
		 * playJB.setEnabled(val); pauseJB.setEnabled(val);
		 * stopJB.setEnabled(val);
		 */
		stopRecordJB.setEnabled(val);
		saveJB.setEnabled(val);
	}

	/*
	 * private void setIcons() { try { BufferedImage bufferedImage =
	 * ImageIO.read(getClass()
	 * .getResourceAsStream("/com/adbsimple/images/play_btn.png"));
	 * playJB.setIcon(new ImageIcon(bufferedImage)); bufferedImage =
	 * ImageIO.read(getClass().getResourceAsStream(
	 * "/com/adbsimple/images/pause_btn.png")); pauseJB.setIcon(new
	 * ImageIcon(bufferedImage)); bufferedImage =
	 * ImageIO.read(getClass().getResourceAsStream(
	 * "/com/adbsimple/images/stop_btn.png")); stopJB.setIcon(new
	 * ImageIcon(bufferedImage)); } catch (IOException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 */
	private void addComponents() {
		add(deviceList).setBounds(20, 20, 200, 30);
		add(startRecordJB).setBounds(20, 200, 100, 30);
		add(stopRecordJB).setBounds(130, 200, 100, 30);
		add(saveJB).setBounds(240, 200, 100, 30);

		/*
		 * add(playJB).setBounds(450, 10, 30, 30); add(pauseJB).setBounds(490,
		 * 10, 30, 30); add(stopJB).setBounds(530, 10, 30, 30);
		 */

		add(new JLabel("Note :")).setBounds(10, 300, 100, 30);
		add(
				new JLabel(
						"This feature works in devices running Android 4.4 and higher."))
				.setBounds(20, 330, 300, 30);
		add(new JLabel("Maximum recording time is 3 minutes.")).setBounds(20,
				360, 300, 30);
		add(new JLabel("Audio is not recorded with video file.")).setBounds(20,
				390, 300, 30);
		// add(videoPreviewPanel).setBounds(380, 50, videoWidth, videoHeight);

	}

	private void startScreenRecord(String deviceSerNumber) {
		// run ADB shell screenrecord command
		try {
			process = runTime
					.exec(ADB_PATH + "adb -s " + deviceSerNumber
							+ " shell screenrecord --size 1280x720 /sdcard/"
							+ fileName);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		BufferedReader buf = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		SeperateThread seperateThread = new SeperateThread(buf);
		executorService = Executors.newSingleThreadExecutor();

		executorService.execute(seperateThread);

	}

	public void stopScreenRecord() {
		startRecordJB.setEnabled(true);
		if (process != null) {
			if (executorService != null) {
				executorService.shutdownNow();
				process.destroy();
			}
		}
	}

	/*
	 * public void stopVideo() { videoPreviewPanel.stopVideo(); }
	 */

	public void deleteTempFile() {
		if (temp_file != null && temp_file.exists()) {
			temp_file.delete();
			temp_file = null;
		}
	}

	private void saveRcordedVideo(String deviceSerNumber, String destinationFile) {
		destinationFile = destinationFile + ".mp4";
		String pull_CMD = ADB_PATH + "adb -s " + deviceSerNumber
				+ " pull /sdcard/" + fileName + " " + destinationFile;
		Utils.showProgressDialog();
		Utils.setProgressText("Processing...");
		try {
			String line = null;
			process = runTime.exec(pull_CMD);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			while ((line = bufferedReader.readLine()) != null) {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Utils.dismisProgressDialog();
	}

	private void pullVideoToTempFile(String deviceSerNumber) {
		if (deviceList.getSelectedItem().toString().equalsIgnoreCase(NO_DEVICE)) {
			JOptionPane.showMessageDialog(null, SELECT_DEVICE);
		} else {
			deleteTempFile();

			try {
				temp_file = File.createTempFile("tmp", ".mp4");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			String pull_CMD = ADB_PATH + "adb -s " + deviceSerNumber
					+ " pull /sdcard/" + fileName + " " + temp_file.getPath();
			Utils.showProgressDialog();
			Utils.setProgressText("Processing...");
			try {
				String line = null;
				process = runTime.exec(pull_CMD);
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				while ((line = bufferedReader.readLine()) != null) {
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Utils.dismisProgressDialog();
		}
	}

	private class SeperateThread implements Runnable {
		String line;
		BufferedReader buf;

		public SeperateThread(BufferedReader buf) {
			this.buf = buf;
		}

		@Override
		public void run() {
			try {
				while ((line = buf.readLine()) != null) {
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Start")) {
			if (deviceList.getSelectedItem().toString()
					.equalsIgnoreCase(NO_DEVICE)) {
				JOptionPane.showMessageDialog(null, SELECT_DEVICE);
			} else {
				deleteTempFile();
				fileName = System.currentTimeMillis() + ".mp4";
				startScreenRecord(deviceSerial
						.get(deviceList.getSelectedItem()));
				startRecordJB.setEnabled(false);
				setEnable(false);
				stopRecordJB.setEnabled(true);
			}
		} else if (e.getActionCommand().equalsIgnoreCase("Stop")) {
			setEnable(true);
			stopScreenRecord();
		} else if (e.getActionCommand().equalsIgnoreCase("Save")) {
			if (deviceList.getSelectedItem().toString()
					.equalsIgnoreCase(NO_DEVICE)) {
				JOptionPane.showMessageDialog(null, SELECT_DEVICE);
			} else {
				JFileChooser fileChooser = new JFileChooser();
				int userSelection = fileChooser.showSaveDialog(this);
				if (userSelection == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					saveRcordedVideo(
							deviceSerial.get(deviceList.getSelectedItem()),
							file.getPath());
					JOptionPane.showMessageDialog(this, "File Saved");
				}
			}
		} /*
		 * else if (e.getActionCommand().equalsIgnoreCase("play")) { if
		 * (temp_file == null) { pullVideoToTempFile(deviceSerial.get(deviceList
		 * .getSelectedItem()));
		 * videoPreviewPanel.startPlay(temp_file.getPath()); }
		 * videoPreviewPanel.playVideo();
		 * 
		 * } else if (e.getActionCommand().equalsIgnoreCase("pause")) {
		 * videoPreviewPanel.pauseVideo();
		 * 
		 * } else if (e.getActionCommand().equalsIgnoreCase("stop_play")) {
		 * videoPreviewPanel.stopVideo(); }
		 */
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentShown(ComponentEvent e) {
		runDeviceListCMD();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		deviceList.removeAllItems();
		deviceSerial.clear();
		stopScreenRecord();
		// stopVideo();
		setEnable(false);
	}

	public void runDeviceListCMD() {

		deviceList.removeAllItems();
		deviceSerial.clear();
		deviceList.addItem(NO_DEVICE);

		String line = null;
		Utils.showProgressDialog();
		// run ADB command to list connected devices
		try {
			process = runTime.exec(ADB_PATH + "adb devices -l");
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((line = buf.readLine()) != null) {
				if (line.contains("model")) {
					// extract Device Name
					String deviceName = line
							.substring(line.indexOf("model") + 6);
					deviceList.addItem(deviceName);

					// extract Device Serial number
					int endIndex = line.indexOf(" ");
					String deviceSerNumber = line.substring(0, endIndex);
					deviceSerial.put(deviceName, deviceSerNumber);

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Utils.dismisProgressDialog();

	}
}
