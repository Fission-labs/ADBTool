package com.adbsimple.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.adbsimple.interfaces.Const;
import com.adbsimple.util.Utils;

public class UninstallPanel extends JPanel implements ComponentListener,
		ActionListener, Const {

	private PhonePanel phonePanel;
	private JPanel appsContainerPanel;
	private JComboBox<String> deviceList;
	private Runtime runTime;
	private Process process;
	private ArrayList<String> appsList;
	private HashMap<String, String> deviceSerial;
	private ButtonGroup packagesBG;
	private JScrollPane jScrollPane;
	private JButton unInstallBtn, clearDataBtn;

	public UninstallPanel() {
		setVisible(true);
		setLayout(null);
		addComponentListener(this);
		Border border = BorderFactory.createEtchedBorder();
		Border border1 = BorderFactory.createTitledBorder(border, "Uninstall",
				TitledBorder.CENTER, TitledBorder.CENTER);
		setBorder(border1);

		init();
		addComponents();

		deviceList.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					appsList.clear();
					appsContainerPanel.removeAll();
					appsContainerPanel.validate();
					appsContainerPanel.repaint();
					if (!e.getItem().toString().equalsIgnoreCase(NO_DEVICE)) {
						runPackageListCMD(deviceSerial.get(e.getItem()));
					}
				}
			}
		});

		unInstallBtn.addActionListener(this);
		clearDataBtn.addActionListener(this);
	}

	private void init() {
		unInstallBtn = new JButton("Uninstall");
		clearDataBtn = new JButton("Clear Data");
		appsList = new ArrayList<>();
		phonePanel = new PhonePanel();
		appsContainerPanel = new JPanel();
		appsContainerPanel.setLayout(null);
		jScrollPane = new JScrollPane(appsContainerPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		deviceList = new JComboBox<String>();
		deviceSerial = new HashMap<>();

		runTime = Runtime.getRuntime();
	}

	private void addComponents() {
		// appsContainerPanel.setPreferredSize(new Dimension(275, 370));
		add(deviceList).setBounds(20, 20, 200, 30);
		phonePanel.add(unInstallBtn).setBounds(40, 440, 100, 30);
		phonePanel.add(clearDataBtn).setBounds(170, 440, 100, 30);
		phonePanel.add(jScrollPane).setBounds(20, 50, 275, 370);
		add(phonePanel).setBounds(250, 50, 300, 500);
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
		appsList.clear();
		deviceSerial.clear();
		deviceList.removeAllItems();
		appsContainerPanel.removeAll();
		appsContainerPanel.setPreferredSize(new Dimension(100, 100));
		jScrollPane.validate();
		jScrollPane.repaint();
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

	private void runPackageListCMD(String deviceSerNumber) {
		String line = null;
		Utils.showProgressDialog();
		// run ADB command to list connected devices
		try {
			process = runTime.exec(ADB_PATH + "adb -s " + deviceSerNumber
					+ " shell pm list packages -3");
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((line = buf.readLine()) != null) {
				if (line.contains("package")) {
					String packageName = line
							.substring(line.indexOf("package") + 8);
					appsList.add(packageName);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		if (appsList.size() > 0) {
			showPackages();
		}
		Utils.dismisProgressDialog();
	}

	private void showPackages() {
		JRadioButton packagesJRB[] = new JRadioButton[appsList.size()];
		packagesBG = new ButtonGroup();
		int height = 0;

		for (int i = 1; i < packagesJRB.length - 1; i++) {
			packagesJRB[i] = new JRadioButton(appsList.get(i), false);

			packagesJRB[i].setActionCommand(appsList.get(i));

			appsContainerPanel.add(packagesJRB[i])
					.setBounds(0, height, 250, 30);
			packagesBG.add(packagesJRB[i]);

			height = height + 30;
		}
		appsContainerPanel.setPreferredSize(new Dimension(100, height));
		jScrollPane.validate();
		jScrollPane.repaint();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (deviceList.getSelectedItem().toString().equalsIgnoreCase(NO_DEVICE)) {
			JOptionPane.showMessageDialog(null, "Please select device");
		} else if (packagesBG.getSelection() == null) {
			JOptionPane.showMessageDialog(null, "Please select apk file");
		} else {
			if (e.getActionCommand().equalsIgnoreCase("Uninstall")) {
				unInstallApp();
			} else if (e.getActionCommand().equalsIgnoreCase("Clear Data")) {
				clearAppData();
			}
		}
	}

	private void unInstallApp() {
		Utils.showProgressDialog();
		Utils.setProgressText("Uninstalling...");
		String packageName = packagesBG.getSelection().getActionCommand();
		String uninstall_cmd = ADB_PATH + "adb -s "
				+ deviceSerial.get(deviceList.getSelectedItem())
				+ " uninstall " + packageName;

		try {
			String line = null;
			boolean isSuccess = false;
			process = runTime.exec(uninstall_cmd);
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((line = buf.readLine()) != null) {
				if (line.contains("Success")) {
					isSuccess = true;
				}
			}
			Utils.dismisProgressDialog();
			if (isSuccess) {
				JOptionPane.showMessageDialog(null,
						"App uninstall successfully");
			} else {
				JOptionPane.showMessageDialog(null, "Package Not found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void clearAppData() {
		Utils.showProgressDialog();
		Utils.setProgressText("Data clearing...");
		String packageName = packagesBG.getSelection().getActionCommand();
		String clearData_cmd = ADB_PATH + "adb -s "
				+ deviceSerial.get(deviceList.getSelectedItem())
				+ " shell pm clear " + packageName;

		try {
			String line = null;
			boolean isSuccess = false;
			process = runTime.exec(clearData_cmd);
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((line = buf.readLine()) != null) {
				if (line.contains("Success")) {
					isSuccess = true;
				}
			}
			Utils.dismisProgressDialog();
			if (isSuccess) {
				JOptionPane.showMessageDialog(null,
						"App data cleared successfully");
			} else {
				JOptionPane.showMessageDialog(null, "Package not found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
