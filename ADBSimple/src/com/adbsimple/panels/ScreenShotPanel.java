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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.adbsimple.interfaces.Const;
import com.adbsimple.util.Utils;

public class ScreenShotPanel extends JPanel implements ComponentListener,
		ActionListener, Const {
	private JComboBox<String> deviceList;
	private HashMap<String, String> deviceSerial;
	private Runtime runTime;
	private Process process;
	private DrawingPanel drawingPanel;
	private EditOptionsPanel editPanel;
	private JButton captureJB, saveJB, editJB;
	private String line;
	private BufferedReader bufferedReader;
	private File temp_file;

	public ScreenShotPanel() {
		setVisible(true);
		setLayout(null);
		addComponentListener(this);
		Border border = BorderFactory.createEtchedBorder();
		Border border1 = BorderFactory.createTitledBorder(border,
				"Capture ScreenShot", TitledBorder.CENTER, TitledBorder.CENTER);
		setBorder(border1);

		init();
		addComponents();

		saveJB.addActionListener(this);
		captureJB.addActionListener(this);
		editJB.addActionListener(this);
	}

	private void init() {
		captureJB = new JButton("Capture");
		saveJB = new JButton("Save");
		editJB = new JButton("Edit");
		deviceList = new JComboBox<String>();
		deviceSerial = new HashMap<>();
		drawingPanel = new DrawingPanel(400, 600);
		editPanel = new EditOptionsPanel(drawingPanel);
		editPanel.setVisible(false);

		runTime = Runtime.getRuntime();
	}

	private void addComponents() {
		add(deviceList).setBounds(20, 20, 200, 30);
		add(saveJB).setBounds(20, 200, 100, 30);
		add(captureJB).setBounds(130, 200, 100, 30);
		add(editJB).setBounds(20, 280, 100, 30);
		add(editPanel).setBounds(20, 320, 230, 270);
		add(drawingPanel).setBounds(350, 20, 400, 600);
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
		drawingPanel.clearDrawingArea();
		drawingPanel.resetValues();
		editPanel.resetValues();
		editJB.setEnabled(false);
		editPanel.setVisible(false);
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Capture")) {
			if (deviceList.getSelectedItem().toString()
					.equalsIgnoreCase(NO_DEVICE)) {
				JOptionPane.showMessageDialog(null, SELECT_DEVICE);
			} else {
				takeScreenShot(deviceSerial.get(deviceList.getSelectedItem()));
			}
		} else if (e.getActionCommand().equalsIgnoreCase("Save")) {
			if (drawingPanel.isImageShowing()) {
				JFileChooser fileChooser = new JFileChooser();
				int userSelection = fileChooser.showSaveDialog(this);
				if (userSelection == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					drawingPanel.saveImage(file.getPath());
					JOptionPane.showMessageDialog(this, "File Saved");
				}
			} else {
				JOptionPane.showMessageDialog(null,
						"Please capture a image before save");
			}

		} else {
			// Edit button On Click
			editPanel.setVisible(true);
		}
	}

	private void takeScreenShot(final String deviceSerNumber) {
		final String fileName = String.valueOf(System.currentTimeMillis())
				+ ".raw";
		String screenCap_CMD = ADB_PATH + "adb -s " + deviceSerNumber
				+ " shell screencap -p /sdcard/" + fileName;
		Utils.showProgressDialog();
		try {
			process = runTime.exec(screenCap_CMD);
			line = null;
			bufferedReader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			ExecutorService executorService = Executors
					.newSingleThreadExecutor();
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						Utils.setProgressText("Capturing...");
						while ((line = bufferedReader.readLine()) != null) {
						}
						pullImageToPc(fileName, deviceSerNumber);

					} catch (IOException e) {
						e.printStackTrace();
					}
					Utils.dismisProgressDialog();
				}

			});

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void pullImageToPc(String fileName, String deviceSerNumber) {

		deleteTempFile();

		try {
			temp_file = File.createTempFile("tmp", ".raw");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String pull_CMD = ADB_PATH + "adb -s " + deviceSerNumber
				+ " pull /sdcard/" + fileName + " " + temp_file.getPath();
		try {
			line = null;
			process = runTime.exec(pull_CMD);
			bufferedReader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((line = bufferedReader.readLine()) != null) {
			}
			showPreview(temp_file.getPath());
			// takeScreenShot(deviceSerNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showPreview(String file_path) {
		editJB.setEnabled(true);
		drawingPanel.showImagePreview(file_path);
	}

	public void deleteTempFile() {
		if (temp_file != null && temp_file.exists()) {
			temp_file.delete();
		}
	}

}
