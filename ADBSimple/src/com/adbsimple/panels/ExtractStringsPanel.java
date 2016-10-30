package com.adbsimple.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.adbsimple.interfaces.Const;
import com.adbsimple.util.Utils;

public class ExtractStringsPanel extends JPanel implements ActionListener,
		ComponentListener, Const {
	private final String NO_APPLICATION = "No Application Selected";
	private Runtime runTime;
	private Process process;
	private JComboBox<String> deviceList, appsList;
	private HashMap<String, String> deviceSerial, package_apk_map,
			string_key_map;
	private boolean isSuccess, isWaiting;
	private String line;
	private BufferedReader bufferedReader;
	private JButton getStringJB;
	private File temp_file;
	private JTextField stringValueJTF, stringIDJTF;
	private JLabel stringValueJL, stringIDJL;

	public ExtractStringsPanel() {
		setVisible(true);
		setLayout(null);
		Border border = BorderFactory.createEtchedBorder();
		Border border1 = BorderFactory.createTitledBorder(border,
				"Extract Strings", TitledBorder.CENTER, TitledBorder.CENTER);
		setBorder(border1);

		init();
		addComponents();

		addComponentListener(this);
		getStringJB.addActionListener(this);

		deviceList.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					appsList.removeAllItems();
					appsList.addItem(NO_APPLICATION);
					if (!e.getItem().toString().equalsIgnoreCase(NO_DEVICE)) {
						runPackageListCMD(deviceSerial.get(e.getItem()));
					}
				}
			}
		});
		appsList.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
		appsList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED
						&& !e.getItem().toString()
								.equalsIgnoreCase(NO_APPLICATION)) {
					string_key_map.clear();
					// Step 1: pull apk from device to temp file
					pullAPK(deviceSerial.get(deviceList.getSelectedItem()),
							package_apk_map.get(appsList.getSelectedItem()));
				}
			}
		});
		stringValueJTF.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void removeUpdate(DocumentEvent e) {
						if (appsList.getSelectedItem().toString()
								.equalsIgnoreCase(NO_APPLICATION)) {
							JOptionPane.showMessageDialog(null,
									"Please select package name");
						} else {
							String id = string_key_map.get(stringValueJTF
									.getText());
							stringIDJTF.setText(id);
						}
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						if (appsList.getSelectedItem().toString()
								.equalsIgnoreCase(NO_APPLICATION)) {
							JOptionPane.showMessageDialog(null,
									"Please select package name");
						} else {
							String id = string_key_map.get(stringValueJTF
									.getText());
							stringIDJTF.setText(id);
						}
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						// TODO
					}
				});

	}

	private void init() {
		deviceList = new JComboBox<String>();
		appsList = new JComboBox<String>();
		getStringJB = new JButton("Get Strings.xml");
		stringValueJL = new JLabel("String Value : ");
		stringIDJL = new JLabel("String ID : ");
		stringValueJTF = new JTextField();
		stringIDJTF = new JTextField();
		deviceSerial = new HashMap<>();
		package_apk_map = new HashMap<>();
		string_key_map = new HashMap<>();

		runTime = Runtime.getRuntime();

		runDeviceListCMD();

	}

	private void addComponents() {
		add(deviceList).setBounds(20, 20, 200, 30);
		add(appsList).setBounds(230, 20, 200, 30);
		add(getStringJB).setBounds(20, 120, 150, 30);
		add(stringValueJL).setBounds(20, 300, 100, 30);
		add(stringValueJTF).setBounds(140, 300, 250, 30);
		add(stringIDJL).setBounds(20, 350, 100, 30);
		add(stringIDJTF).setBounds(140, 350, 250, 30);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Get Strings.xml")) {
			if (appsList.getSelectedItem().toString()
					.equalsIgnoreCase(NO_APPLICATION)) {
				JOptionPane.showMessageDialog(null,
						"Please select package name");
			} else {
				writeStringsToFile();
			}
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
		package_apk_map.clear();
		string_key_map.clear();
		deviceList.removeAllItems();
		appsList.removeAllItems();
	}

	public void runDeviceListCMD() {
		string_key_map.clear();
		package_apk_map.clear();
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
					+ " shell pm list packages -f -3");
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((line = buf.readLine()) != null) {
				if (line.contains("package")) {
					String packageName = line.substring(8);
					String str[] = packageName.split("=");
					appsList.addItem(str[1]);
					package_apk_map.put(str[1], str[0]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Utils.dismisProgressDialog();
	}

	private void pullAPK(String deviceSerNumber, String apkPath) {
		deleteTempFile();

		try {
			temp_file = File.createTempFile("tmp", ".apk");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String pull_apk_CMD = ADB_PATH + "adb -s " + deviceSerNumber + " pull "
				+ apkPath + " " + temp_file;
		Utils.showProgressDialog();
		try {
			process = runTime.exec(pull_apk_CMD);
			line = null;
			bufferedReader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			ExecutorService executorService = Executors
					.newSingleThreadExecutor();
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Utils.setProgressText("Processing...");
						while ((line = bufferedReader.readLine()) != null) {
						}
						// Step 2: dump from apk using aapt
						getDumpFromAPK();
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

	private void getDumpFromAPK() {
		File buildTools = new File(System.getenv("ANDROID_HOME")
				+ File.separator + "build-tools");
		File listOfFiles[] = buildTools.listFiles();
		String aaptPath = listOfFiles[listOfFiles.length - 1].getPath();

		if (aaptPath != null) {
			String dump_from_apk_CMD = aaptPath + File.separator
					+ "aapt dump --values resources " + temp_file.getPath();
			try {
				process = runTime.exec(dump_from_apk_CMD);
				line = null;
				bufferedReader = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				boolean isConfigFound = false;
				boolean isKeyFound = false;
				String stringId = null, stringVal;
				while ((line = bufferedReader.readLine()) != null) {

					String currentLine = line.trim();

					if (currentLine.startsWith("config (default)")
							|| isConfigFound) {

						isConfigFound = true;
						if (currentLine.startsWith("resource")
								&& currentLine.contains("string/")) {
							int startIndex = currentLine.indexOf("string/");
							int endIndex = currentLine.indexOf(" ",
									startIndex + 7);
							stringId = currentLine.substring(startIndex + 7,
									endIndex - 1);
							isKeyFound = true;
						} else if (currentLine.startsWith("(string8)")
								&& isKeyFound) {
							int startIndex = currentLine.indexOf('"');
							stringVal = currentLine.substring(startIndex + 1,
									currentLine.length() - 1);
							string_key_map.put(stringVal, stringId);
							isKeyFound = false;
						} else if (currentLine.startsWith("config ")
								&& string_key_map.size() > 0) {
							break;
						}
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void writeStringsToFile() {

		JFileChooser fileChooser = new JFileChooser();
		int userSelection = fileChooser.showSaveDialog(this);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				FileWriter fileWriter = new FileWriter(file, false);
				ArrayList<String> values = new ArrayList<>(
						string_key_map.keySet());
				for (int i = 0; i < values.size(); i++) {
					String id = string_key_map.get(values.get(i));
					String value = values.get(i);
					String line = "<String name = " + '"' + id + '"' + ">"
							+ value + "</string>";
					fileWriter.write(line + "\n");
					fileWriter.flush();
				}
				fileWriter.close();
				JOptionPane.showMessageDialog(this, "File Saved");

			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				JOptionPane
						.showMessageDialog(this,
								"FileNotFound: Please save the file in correct location");
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this,
						"IOException: Please try again");
				e1.printStackTrace();
			}

		}

	}

	public void deleteTempFile() {
		if (temp_file != null && temp_file.exists()) {
			temp_file.delete();
		}
	}

}
