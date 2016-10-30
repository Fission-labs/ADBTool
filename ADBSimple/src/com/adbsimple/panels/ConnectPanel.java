package com.adbsimple.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.adbsimple.interfaces.Const;
import com.adbsimple.util.Utils;

public class ConnectPanel extends JPanel implements ComponentListener,
		ActionListener, Const {
	private Runtime runTime;
	private Process process;
	private HashMap<String, String> deviceSerial;
	private JLabel deviceNames[];
	private JButton connectJBs[];
	private ArrayList<String> deviceList;
	private ArrayList<String> tcpDeviceList;

	public ConnectPanel() {
		setVisible(true);
		setLayout(null);
		Border border = BorderFactory.createEtchedBorder();
		Border border1 = BorderFactory.createTitledBorder(border,
				"Connect via WIFI", TitledBorder.CENTER, TitledBorder.CENTER);
		setBorder(border1);
		addComponentListener(this);
		init();
	}

	private void init() {
		deviceList = new ArrayList<>();
		tcpDeviceList = new ArrayList<>();
		deviceSerial = new HashMap<>();
		// connectJB = new JButton("Connect");
		runTime = Runtime.getRuntime();

		// Get list of connected devices
		// runDeviceListCMD();
	}

	private void startUsb(String deviceSerNumber, int position) {
		String line = null;
		Utils.showProgressDialog();
		// adb command to start usb
		try {
			process = runTime.exec(ADB_PATH + "adb -s " + deviceSerNumber
					+ " usb");
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((line = buf.readLine()) != null) {
				if (line.contains("restarting")) {
					connectJBs[position].setText("Connect");
				}
			}
			Utils.dismisProgressDialog();
			if (connectJBs[position].getText().equalsIgnoreCase("Connect")) {
				JOptionPane.showMessageDialog(null,
						"Device disconnected from tcp");
				reSetAll();
				validate();
				repaint();
				runDeviceListCMD();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void startTcp(String deviceSerNumber, String deviceIp) {
		String line = null;
		// adb command to start tcp
		try {
			process = runTime.exec(ADB_PATH + "adb -s " + deviceSerNumber
					+ " tcpip 5555");
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			process.waitFor();
			adbConnect(deviceSerNumber, deviceIp);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void adbConnect(String deviceSerNumber, String deviceIp) {

		String line = null;

		// adb command to start tcp
		try {
			process = runTime.exec(ADB_PATH + "adb -s " + deviceSerNumber
					+ " connect " + deviceIp);
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			process.waitFor();
			Utils.dismisProgressDialog();
			JOptionPane
					.showMessageDialog(null,
							"Device connected with tcp, now you can run with out usb cable");

		} catch (IOException e) {
			e.printStackTrace();
			Utils.dismisProgressDialog();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Utils.dismisProgressDialog();
		}

	}

	private String getIpOfDevice(String deviceSerNumber) {
		String line = null;
		Utils.showProgressDialog();
		// adb command to get ip address of device
		try {
			process = runTime.exec(ADB_PATH + "adb -s " + deviceSerNumber
					+ " shell ifconfig wlan0");
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String ipaddr = null;
			while ((line = buf.readLine()) != null) {
				if (line.contains("ip")) {
					int startIndex = line.indexOf("ip");
					int endIndex = line.indexOf("mask");
					ipaddr = line.substring(startIndex + 2, endIndex);
					ipaddr = ipaddr.trim();
				}

			}
			return ipaddr;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void runDeviceListCMD() {
		String line = null;

		Utils.showProgressDialog();
		Utils.setProgressText("Please Wait...");
		// run ADB command to list connected devices
		try {
			process = runTime.exec(ADB_PATH + "adb devices -l");
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((line = buf.readLine()) != null) {
				if (line.contains("model")) {
					deviceList.add(line);
				}
			}

			deviceNames = new JLabel[deviceList.size()];
			connectJBs = new JButton[deviceList.size()];

			int height = 100;

			for (int i = 0; i < deviceNames.length; i++) {
				// extract Device Name
				String deviceName = deviceList.get(i).substring(
						deviceList.get(i).indexOf("model") + 6);
				// extract Device Serial number
				int endIndex = deviceList.get(i).indexOf(" ");
				String deviceSerNumber = deviceList.get(i).substring(0,
						endIndex);

				if (deviceSerNumber.contains(":5555")) {
					tcpDeviceList.add(deviceName);
				}
				if (deviceSerial.get(deviceName) != null) {
					if (!deviceSerial.get(deviceName).contains(".5555")) {
						deviceSerial.put(deviceName, deviceSerNumber);
					}
				} else {
					deviceSerial.put(deviceName, deviceSerNumber);
					deviceNames[i] = new JLabel(deviceName);
					connectJBs[i] = new JButton("Connect");
					add(deviceNames[i]).setBounds(200, height, 200, 30);
					add(connectJBs[i]).setBounds(500, height, 100, 30);
					connectJBs[i].addActionListener(this);
					connectJBs[i].setActionCommand("" + i);

					height = height + 40;
				}

			}
			for (int i = 0; i < deviceNames.length; i++) {
				if (deviceNames[i] != null
						&& tcpDeviceList.contains(deviceNames[i].getText())) {
					connectJBs[i].setText("Disconnect");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		Utils.dismisProgressDialog();
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
		validate();
		repaint();
		runDeviceListCMD();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		reSetAll();
	}

	private void reSetAll() {
		deviceList.clear();
		deviceSerial.clear();
		tcpDeviceList.clear();
		removeAll();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int position = Integer.parseInt(e.getActionCommand());
		String deviceSerialNum = deviceSerial.get(deviceNames[position]
				.getText());

		if (connectJBs[position].getText().equalsIgnoreCase("Connect")) {
			String device_ip = getIpOfDevice(deviceSerialNum);
			if (device_ip != null) {
				// Starting tcp at port 5555
				startTcp(deviceSerialNum, device_ip);
			} else {
				Utils.dismisProgressDialog();
				device_ip = JOptionPane
						.showInputDialog(
								null,
								"Unable to find connected device IP address, please enter your Android device IP address.\n(Settings -> About device -> Status -> IP address)",
								" ", JOptionPane.INFORMATION_MESSAGE);
				if (device_ip != null && !device_ip.equalsIgnoreCase("")) {
					startTcp(deviceSerialNum, device_ip);
				}
			}
			connectJBs[position].setText("Disconnect");
		} else {
			startUsb(deviceSerialNum, position);
		}

	}
}
