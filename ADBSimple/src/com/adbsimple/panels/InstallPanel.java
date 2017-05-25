package com.adbsimple.panels;

import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.adbsimple.interfaces.Const;
import com.adbsimple.listeners.DragAndDropListener;
import com.adbsimple.util.Utils;

public class InstallPanel extends JPanel implements ActionListener,
		ComponentListener, Const {
	private PhonePanel phonePanel;
	private JLabel hintJL, apkFileNameJL;
	private JButton installJB, browseJB, unzipJB;
	private Runtime runTime;
	private Process process;
	private JComboBox<String> deviceList;
	private HashMap<String, String> deviceSerial;
	private String apkPath;
	private File apkFile;
	private boolean isSuccess, isWaiting;
	private String line;
	private BufferedReader bufferedReader;

	public InstallPanel() {
		setVisible(true);
		setLayout(null);
		addComponentListener(this);
		Border border = BorderFactory.createEtchedBorder();
		Border border1 = BorderFactory.createTitledBorder(border,
				"Install APK", TitledBorder.CENTER, TitledBorder.CENTER);
		setBorder(border1);

		init();
		addComponents();

		setDragAndDropListener();
		browseJB.addActionListener(this);
		installJB.addActionListener(this);
		unzipJB.addActionListener(this);

	}

	private void init() {
		phonePanel = new PhonePanel();
		hintJL = new JLabel(
				"<html>Drag and Drop .apk file here<br><br><center>or</html>");
		apkFileNameJL = new JLabel();
		browseJB = new JButton("Browse");
		installJB = new JButton("Install");
		unzipJB = new JButton("Extract APK");
		deviceList = new JComboBox<String>();
		deviceSerial = new HashMap<>();

		runTime = Runtime.getRuntime();

		runDeviceListCMD();

	}

	private void addComponents() {
		add(deviceList).setBounds(20, 20, 200, 30);
		phonePanel.add(hintJL).setBounds(100, 150, 200, 50);
		phonePanel.add(browseJB).setBounds(120, 210, 100, 30);
		phonePanel.add(apkFileNameJL).setBounds(100, 250, 150, 30);
		phonePanel.add(installJB).setBounds(40, 440, 100, 30);
		phonePanel.add(unzipJB).setBounds(160, 440, 120, 30);
		add(phonePanel).setBounds(250, 50, 300, 500);
	}

	public void setApkFile(File file) {
		apkFileNameJL.setText(file.getName());
		apkFile = file;
		apkPath = "\"" + file.getPath() + "\"";
	}

	private void setDragAndDropListener() {
		DragAndDropListener listener = new DragAndDropListener(this);
		new DropTarget(phonePanel, listener);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Browse")) {
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileFilter(new FileNameExtensionFilter(
					"apk files only", "apk"));
			int result = jFileChooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = jFileChooser.getSelectedFile();
				setApkFile(file);
			}

		} else {
			if (apkFileNameJL.getText().equalsIgnoreCase("")) {
				JOptionPane.showMessageDialog(null, "Please select apk file");
			} else if (e.getActionCommand().equalsIgnoreCase("Extract APK")) {
				unzipAPK(apkFile.getPath());
			} else if (deviceList.getSelectedItem().toString()
					.equalsIgnoreCase(NO_DEVICE)) {
				JOptionPane.showMessageDialog(null, "Please select device");
			} else if (e.getActionCommand().equalsIgnoreCase("Install")) {
				String deviceSerNumber = deviceSerial.get(deviceList
						.getSelectedItem());
				installApk(deviceSerNumber, apkPath);
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
		apkFileNameJL.setText("");
		deviceSerial.clear();
		deviceList.removeAllItems();
	}

	private void installApk(final String deviceSerNumber, final String apkPath) {
		String install_cmd = ADB_PATH + "adb -s " + deviceSerNumber
				+ " install -r " + apkPath;
		Utils.showProgressDialog();
		try {
			process = runTime.exec(install_cmd);
			line = null;
			bufferedReader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			isSuccess = true;
			isWaiting = false;
			ExecutorService executorService = Executors
					.newSingleThreadExecutor();
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						Utils.setProgressText("Installing");
						while ((line = bufferedReader.readLine()) != null) {
							if (line.contains("Failure")) {
								isSuccess = false;
							}
							if (line.contains("waiting")) {
								isWaiting = true;
								break;
							}

						}
						if (isWaiting) {
							JOptionPane.showMessageDialog(null,
									"Device Disconnected");
							return;
						}
						if (!isSuccess) {
							uninstallApp(deviceSerNumber, apkPath);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					Utils.dismisProgressDialog();
					JOptionPane.showMessageDialog(InstallPanel.this,
							"APK Installed Successfully.");
				}

			});

		} catch (IOException e) {
			e.printStackTrace();
		}
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

	private void uninstallApp(String deviceSerNumer, String apkPath) {
		Utils.setProgressText("Uninstalling old app");
		// Extract package name from selected apk
		/*
		 * File file = new File(getClass().getProtectionDomain().getCodeSource()
		 * .getLocation().getPath()); String path = file.getParent() +
		 * File.separator + "src" + File.separator + "com" + File.separator +
		 * "adbsimple" + File.separator + "config" + File.separator +
		 * "config.properties"; File configFile = new File(path);
		 * 
		 * Properties props = new Properties(); FileReader reader;
		 */
		File buildTools = new File(System.getenv("ANDROID_HOME")
				+ File.separator + "build-tools");
		File listOfFiles[] = buildTools.listFiles();
		String aaptPath = listOfFiles[listOfFiles.length - 1].getPath();
		String packageName = null;
		if (aaptPath != null) {
			try {
				String cmd = aaptPath + File.separator + "aapt dump badging "
						+ apkPath;
				process = runTime.exec(cmd);
				bufferedReader = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				line = null;
				while ((line = bufferedReader.readLine()) != null) {
					if (line.startsWith("package")) {
						int startindex = line.indexOf("\'");
						int endindex = line.indexOf("\'", startindex + 1);
						packageName = line.substring(startindex + 1, endindex);
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String uninstall_cmd = ADB_PATH + "adb -s " + deviceSerNumer
				+ " uninstall " + packageName;

		String install_cmd = ADB_PATH + "adb -s " + deviceSerNumer
				+ " install -r " + apkPath;
		try {
			// uninstall existing app
			process = runTime.exec(uninstall_cmd);
			String line = null;
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((line = buf.readLine()) != null) {

			}
			// install new
			Utils.setProgressText("Reinstalling");
			process = runTime.exec(install_cmd);
			buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((line = buf.readLine()) != null) {
			}
			process.destroy();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void unzipAPK(final String apkPath) {
		Utils.showProgressDialog();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				File file = new File(apkPath);
				File dir = new File(file.getParent()
						+ File.separator
						+ file.getName().substring(0,
								file.getName().lastIndexOf(".")));
				// create output directory if it doesn't exist
				if (!dir.exists())
					dir.mkdirs();
				// buffer for read and write data to file
				byte[] buffer = new byte[1024];
				try {
					FileInputStream fis = new FileInputStream(apkPath);
					ZipInputStream zis = new ZipInputStream(fis);
					ZipEntry ze = zis.getNextEntry();
					while (ze != null) {
						String fileName = ze.getName();
						File newFile = new File(dir.getPath() + File.separator
								+ fileName);
						Utils.setProgressText("Extracting to "
								+ newFile.getAbsolutePath());
						// create directories for sub directories in zip
						new File(newFile.getParent()).mkdirs();
						FileOutputStream fos = new FileOutputStream(newFile);
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						fos.close();
						// close this ZipEntry
						zis.closeEntry();
						ze = zis.getNextEntry();
					}
					Utils.dismisProgressDialog();
					// close last ZipEntry
					zis.closeEntry();
					zis.close();
					fis.close();
					JOptionPane.showMessageDialog(InstallPanel.this,
							"APK extracted to " + dir.getPath());
				} catch (IOException e) {
					Utils.dismisProgressDialog();
					JOptionPane.showMessageDialog(InstallPanel.this,
							"There may be some probleme with APK file");
					e.printStackTrace();
				}
			}

		});

	}

}
