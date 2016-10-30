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
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import com.adbsimple.interfaces.Const;
import com.adbsimple.util.Utils;

public class MonkeyTestPanel extends JPanel implements ComponentListener,
		ActionListener, Const {

	private final String NO_APPLICATION = "No Application Selected";
	private JComboBox<String> deviceList, appsList;
	private JTextPane logcatJTP;
	private JButton saveBtn, clearBtn;
	private JScrollPane jScrollPane;
	private Runtime runTime;
	private Process process;
	private HashMap<String, String> deviceSerial;
	private ExecutorService executorService;
	private StyledDocument styledDocument;
	private String appPackageName;
	private JTextField eventsJTF, delayJTF;
	private JButton runBtn;

	public MonkeyTestPanel() {
		setVisible(true);
		setLayout(null);
		addComponentListener(this);
		Border border = BorderFactory.createEtchedBorder();
		Border border1 = BorderFactory.createTitledBorder(border,
				"Monkey Test", TitledBorder.CENTER, TitledBorder.CENTER);
		setBorder(border1);

		init();

		addComponents();

		setItemListener();

		saveBtn.addActionListener(this);
		clearBtn.addActionListener(this);
		runBtn.addActionListener(this);
	}

	private void init() {
		runBtn = new JButton("Run Test");
		eventsJTF = new JTextField();
		delayJTF = new JTextField();
		eventsJTF.setToolTipText("Number of events");
		delayJTF.setToolTipText("Delay in milliseconds");

		deviceList = new JComboBox<String>();
		appsList = new JComboBox<String>();
		logcatJTP = new JTextPane();
		styledDocument = logcatJTP.getStyledDocument();
		saveBtn = new JButton("Save");
		clearBtn = new JButton("Clear");
		jScrollPane = new JScrollPane(logcatJTP);
		jScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		deviceSerial = new HashMap<>();

		logcatJTP.setEditable(false);

		runTime = Runtime.getRuntime();
	}

	private void addComponents() {
		add(jScrollPane).setBounds(10, 60, 550, 500);
		add(saveBtn).setBounds(410, 570, 70, 30);
		add(clearBtn).setBounds(490, 570, 70, 30);
		add(deviceList).setBounds(570, 60, 200, 30);
		add(appsList).setBounds(570, 200, 200, 30);
		add(eventsJTF).setBounds(570, 300, 200, 30);
		add(delayJTF).setBounds(570, 400, 200, 30);
		add(runBtn).setBounds(670, 460, 100, 30);
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
		appsList.removeAllItems();
		deviceSerial.clear();

	}

	public void runDeviceListCMD() {

		deviceList.removeAllItems();
		appsList.removeAllItems();
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

	private void runPackageListCMD(String deviceSerNumber) {
		String line = null;
		Utils.showProgressDialog();
		// run ADB command to list connected devices
		try {
			stopLogcat();
			process = runTime.exec(ADB_PATH + "adb -s " + deviceSerNumber
					+ " shell pm list packages -3");
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((line = buf.readLine()) != null) {
				if (line.contains("package")) {
					String packageName = line
							.substring(line.indexOf("package") + 8);
					appsList.addItem(packageName);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Utils.dismisProgressDialog();
	}

	private void runMonkeyTest(String deviceSerNumber, String packageName,
			int delay, int events) {
		// run ADB logcat command
		try {
			process = runTime.exec(ADB_PATH + "adb -s " + deviceSerNumber
					+ " shell monkey -p " + packageName + " --throttle "
					+ delay + " -v " + events);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		BufferedReader buf = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		SeperateThread1 seperateThread = new SeperateThread1(buf, packageName);
		executorService = Executors.newSingleThreadExecutor();

		executorService.execute(seperateThread);

	}

	public void stopLogcat() {
		if (process != null) {
			if (executorService != null) {
				executorService.shutdownNow();
				process.destroy();
			}
		}
		clearLogcat();
	}

	private void clearLogcat() {
		logcatJTP.setText("");
	}

	private class SeperateThread1 implements Runnable {
		String line;
		BufferedReader buf;
		String packageName;
		String logPID;

		public SeperateThread1(BufferedReader buf, String packageName) {
			this.buf = buf;
			this.packageName = packageName;
		}

		@Override
		public void run() {
			try {
				while ((line = buf.readLine()) != null) {
					try {
						styledDocument.insertString(styledDocument
								.getEndPosition().getOffset(), line + "\n\n",
								null);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Save")) {
			JFileChooser fileChooser = new JFileChooser();
			int userSelection = fileChooser.showSaveDialog(this);
			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				try {
					FileWriter fileWriter = new FileWriter(file, false);
					fileWriter.write(logcatJTP.getText());
					fileWriter.flush();
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
		} else if (e.getActionCommand().equalsIgnoreCase("Clear")) {
			clearLogcat();
		} else if (e.getActionCommand().equalsIgnoreCase("Run Test")) {
			if (!isNumbers(eventsJTF) || !isNumbers(delayJTF)) {
				JOptionPane.showMessageDialog(null,
						"Please enter events and delay");
			} else if (deviceList.getSelectedItem().toString()
					.equalsIgnoreCase(NO_DEVICE)
					|| appsList.getSelectedItem().toString()
							.equalsIgnoreCase(NO_APPLICATION)) {
				JOptionPane.showMessageDialog(null,
						"Please select device and application");
			} else {

				runMonkeyTest(deviceSerial.get(deviceList.getSelectedItem()),
						appPackageName, Integer.parseInt(delayJTF.getText()),
						Integer.parseInt(eventsJTF.getText()));

			}
		}
	}

	private boolean isNumbers(JTextField jtf) {
		if (jtf.getText() != null
				&& jtf.getText().toString().matches(".*[1-9].*")) {
			return true;
		}
		return false;
	}

	private void setItemListener() {
		deviceList.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					appsList.removeAllItems();
					appsList.addItem(NO_APPLICATION);
					if (!e.getItem().toString().equalsIgnoreCase(NO_DEVICE)) {
						stopLogcat();

						runPackageListCMD(deviceSerial.get(e.getItem()));
					}
				}
			}
		});

		appsList.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED
						&& !e.getItem().toString()
								.equalsIgnoreCase(NO_APPLICATION)) {
					stopLogcat();

					appPackageName = e.getItem().toString();

				}
			}

		});

	}
}
