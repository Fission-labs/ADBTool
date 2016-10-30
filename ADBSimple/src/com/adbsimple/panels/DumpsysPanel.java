package com.adbsimple.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
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

public class DumpsysPanel extends JPanel implements ActionListener,
		ComponentListener, Const {

	private Runtime runTime;
	private Process process;
	private JComboBox<String> deviceList;
	private HashMap<String, String> deviceSerial;
	private JButton dumpsysBtn;
	private String line;
	private BufferedReader bufferedReader;

	public DumpsysPanel() {
		setVisible(true);
		setLayout(null);
		addComponentListener(this);
		Border border = BorderFactory.createEtchedBorder();
		Border border1 = BorderFactory.createTitledBorder(border, "Dumpsys",
				TitledBorder.CENTER, TitledBorder.CENTER);
		setBorder(border1);

		init();
		addComponents();

		dumpsysBtn.addActionListener(this);

	}

	private void init() {
		dumpsysBtn = new JButton("Get Dumpsys");
		deviceList = new JComboBox<String>();
		deviceSerial = new HashMap<>();

		runTime = Runtime.getRuntime();

		runDeviceListCMD();

	}

	private void addComponents() {
		add(deviceList).setBounds(20, 20, 200, 30);
		add(dumpsysBtn).setBounds(20, 200, 200, 30);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Get Dumpsys")) {
			if (deviceList.getSelectedItem().toString()
					.equalsIgnoreCase(NO_DEVICE)) {
				JOptionPane.showMessageDialog(null, "Please select device");
			} else {
				getDumpSys();
			}
		}
	}

	private void getDumpSys() {
		JFileChooser fileChooser = new JFileChooser();
		int userSelection = fileChooser.showSaveDialog(this);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			runDumpsysCommand(file);
		}

	}

	private void runDumpsysCommand(final File file) {
		String deviceSerNumber = deviceSerial.get(deviceList.getSelectedItem());
		String dumpsysCMD = ADB_PATH + "adb -s " + deviceSerNumber
				+ " shell dumpsys";
		Utils.showProgressDialog();
		try {
			process = runTime.exec(dumpsysCMD);
			line = null;
			bufferedReader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			ExecutorService executorService = Executors
					.newSingleThreadExecutor();
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Utils.setProgressText("Getting and saving dumpsys into file...");
						FileWriter fileWriter = new FileWriter(file, false);
						while ((line = bufferedReader.readLine()) != null) {
							fileWriter.write(line);
							fileWriter.flush();
						}
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Utils.dismisProgressDialog();
					JOptionPane.showMessageDialog(null,
							"File Saved Successfully");
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

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
		reSet();
	}

	private void reSet() {
		deviceSerial.clear();
		deviceList.removeAllItems();
	}

	public void runDeviceListCMD() {
		deviceSerial.clear();
		deviceList.removeAllItems();
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
