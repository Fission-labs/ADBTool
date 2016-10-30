package com.adbsimple.panels;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.adbsimple.interfaces.Const;
import com.adbsimple.util.Utils;

public class LogcatPanel extends JPanel implements ComponentListener,
		ActionListener, Const {

	private final String NO_APPLICATION = "No Application Selected";
	private final String FILTER_BY_APP = "Filter by Application";
	private final String FILTER_BY_TAG = "Filter by TAG";
	private JComboBox<String> deviceList, appsList;
	private JTextPane logcatJTP;
	private JButton saveBtn, clearBtn, filterBtn;
	private JScrollPane jScrollPane;
	private Runtime runTime;
	private Process process;
	private HashMap<String, String> deviceSerial;
	private ExecutorService executorService;
	private StyledDocument styledDocument;
	private SimpleAttributeSet errorAttributeSet, warningAttributeSet;
	private JTextField filterJTF, searchJTF;
	private JRadioButton filterByApp, filterByTAG;
	private ButtonGroup btnGroup;
	private JLabel searchIconJL;
	private String appPackageName;
	private String searchText = "";

	public LogcatPanel() {
		setVisible(true);
		setLayout(null);
		addComponentListener(this);
		Border border = BorderFactory.createEtchedBorder();
		Border border1 = BorderFactory.createTitledBorder(border, "Logcat",
				TitledBorder.CENTER, TitledBorder.CENTER);
		setBorder(border1);

		init();

		addComponents();

		try {
			BufferedImage bufferedImage = ImageIO.read(getClass()
					.getResourceAsStream("/com/adbsimple/images/search.png"));

			ImageIcon icon = new ImageIcon(new ImageIcon(bufferedImage)
					.getImage().getScaledInstance(searchIconJL.getWidth() - 20,
							searchIconJL.getHeight() - 10, Image.SCALE_SMOOTH));
			searchIconJL.setIcon(icon);

		} catch (IOException e) {
			e.printStackTrace();
		}

		setItemListener();
		setResDocumentListener();

		saveBtn.addActionListener(this);
		clearBtn.addActionListener(this);
		filterByApp.addActionListener(this);
		filterByTAG.addActionListener(this);
		filterBtn.addActionListener(this);
	}

	private void init() {
		btnGroup = new ButtonGroup();
		deviceList = new JComboBox<String>();
		filterByApp = new JRadioButton(FILTER_BY_APP, true);
		btnGroup.add(filterByApp);
		appsList = new JComboBox<String>();
		filterByTAG = new JRadioButton(FILTER_BY_TAG, false);
		btnGroup.add(filterByTAG);
		filterJTF = new JTextField();
		filterJTF.setEnabled(false);
		filterBtn = new JButton("Filter");
		filterBtn.setEnabled(false);
		searchIconJL = new JLabel();
		searchJTF = new JTextField();
		logcatJTP = new JTextPane();
		styledDocument = logcatJTP.getStyledDocument();
		saveBtn = new JButton("Save");
		clearBtn = new JButton("Clear");
		jScrollPane = new JScrollPane(logcatJTP);
		jScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		deviceSerial = new HashMap<>();
		errorAttributeSet = new SimpleAttributeSet();
		warningAttributeSet = new SimpleAttributeSet();

		StyleConstants.setForeground(errorAttributeSet, Color.RED);

		StyleConstants.setForeground(warningAttributeSet, Color.BLUE);

		logcatJTP.setEditable(false);

		runTime = Runtime.getRuntime();
	}

	private void addComponents() {
		add(searchIconJL).setBounds(10, 20, 50, 30);
		add(searchJTF).setBounds(60, 20, 500, 30);
		add(jScrollPane).setBounds(10, 60, 550, 500);
		add(saveBtn).setBounds(410, 570, 70, 30);
		add(clearBtn).setBounds(490, 570, 70, 30);
		add(deviceList).setBounds(570, 60, 200, 30);
		add(filterByApp).setBounds(570, 160, 200, 30);
		add(appsList).setBounds(570, 200, 200, 30);
		add(filterByTAG).setBounds(570, 260, 200, 30);
		add(filterJTF).setBounds(570, 300, 200, 30);
		add(filterBtn).setBounds(660, 340, 100, 30);
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

	private void runLogcatByApp(String deviceSerNumber, String packageName) {
		// run ADB logcat command
		try {
			process = runTime.exec(ADB_PATH + "adb -s " + deviceSerNumber
					+ " logcat -v process");

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		BufferedReader buf = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		SeperateThread1 seperateThread = new SeperateThread1(buf, packageName);
		executorService = Executors.newSingleThreadExecutor();

		executorService.execute(seperateThread);

	}

	private void runLogcatByTAG(String deviceSerNumber, String tag) {
		// run ADB logcat command
		try {
			process = runTime.exec(ADB_PATH + "adb -s " + deviceSerNumber
					+ " logcat -s " + tag);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		BufferedReader buf = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		SeperateThread2 seperateThread = new SeperateThread2(buf);
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

	private class SeperateThread2 implements Runnable {
		String line;
		BufferedReader buf;

		public SeperateThread2(BufferedReader buf) {
			this.buf = buf;
		}

		@Override
		public void run() {
			try {
				while ((line = buf.readLine()) != null) {
					if (line.toLowerCase().contains(searchText)) {
						try {
							if (line.startsWith("E")) {
								styledDocument.insertString(styledDocument
										.getEndPosition().getOffset(), line
										+ "\n\n", errorAttributeSet);
							} else if (line.startsWith("W")) {
								styledDocument.insertString(styledDocument
										.getEndPosition().getOffset(), line
										+ "\n\n", warningAttributeSet);
							} else {
								styledDocument.insertString(styledDocument
										.getEndPosition().getOffset(), line
										+ "\n\n", null);
							}
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}

				}
			} catch (IOException e) {

			}
		}
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
					if ((line.contains(packageName) && line.toLowerCase()
							.contains(searchText))
							|| (logPID != null && line.contains(logPID))) {
						if ((logPID == null || !line.contains(logPID))
								&& line.startsWith("E")) {
							int fromIndex = line.indexOf("(");
							int endIndex = line.indexOf(")", fromIndex);
							logPID = line.substring(fromIndex, endIndex);
						}
						try {
							if (line.startsWith("E")) {
								styledDocument.insertString(styledDocument
										.getEndPosition().getOffset(), line
										+ "\n\n", errorAttributeSet);
							} else if (line.startsWith("W")) {
								styledDocument.insertString(styledDocument
										.getEndPosition().getOffset(), line
										+ "\n\n", warningAttributeSet);
							} else {
								styledDocument.insertString(styledDocument
										.getEndPosition().getOffset(), line
										+ "\n\n", null);
							}
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}

				}
			} catch (IOException e) {

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
		} else if (e.getActionCommand().equalsIgnoreCase(FILTER_BY_APP)) {
			stopLogcat();

			appsList.setEnabled(true);
			filterJTF.setEnabled(false);
			filterBtn.setEnabled(false);
		} else if (e.getActionCommand().equalsIgnoreCase(FILTER_BY_TAG)) {
			stopLogcat();

			filterJTF.setEnabled(true);
			filterBtn.setEnabled(true);
			appsList.setSelectedIndex(0);
			appsList.setEnabled(false);
		} else if (e.getActionCommand().equalsIgnoreCase("Filter")) {
			if (deviceList.getSelectedItem().toString()
					.equalsIgnoreCase(NO_DEVICE)) {
				JOptionPane.showMessageDialog(null, SELECT_DEVICE);
			} else if (filterJTF.getText().equals("")) {
				JOptionPane.showMessageDialog(null, "Please enter text or tag");
			} else {
				stopLogcat();

				runLogcatByTAG(deviceSerial.get(deviceList.getSelectedItem()),
						filterJTF.getText());
			}
		}
	}

	private void setResDocumentListener() {
		searchJTF.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				stopLogcat();
				searchText = searchJTF.getText().toLowerCase();
				if (filterByApp.isSelected()) {
					runLogcatByApp(
							deviceSerial.get(deviceList.getSelectedItem()),
							appPackageName);
				} else {
					runLogcatByTAG(
							deviceSerial.get(deviceList.getSelectedItem()),
							filterJTF.getText());
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				stopLogcat();
				searchText = searchJTF.getText().toLowerCase();
				if (filterByApp.isSelected()) {
					runLogcatByApp(
							deviceSerial.get(deviceList.getSelectedItem()),
							appPackageName);
				} else {
					runLogcatByTAG(
							deviceSerial.get(deviceList.getSelectedItem()),
							filterJTF.getText());
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
			}
		});
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
		
		appsList.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
		appsList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED
						&& !e.getItem().toString()
								.equalsIgnoreCase(NO_APPLICATION)) {
					stopLogcat();

					appPackageName = e.getItem().toString();
					runLogcatByApp(
							deviceSerial.get(deviceList.getSelectedItem()),
							appPackageName);
				}
			}

		});

	}
}
