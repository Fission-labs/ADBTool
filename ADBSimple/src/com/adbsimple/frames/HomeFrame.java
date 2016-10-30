package com.adbsimple.frames;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.adbsimple.help.AboutDialog;
import com.adbsimple.panels.ButtonsPanel;
import com.adbsimple.panels.ConnectPanel;
import com.adbsimple.panels.DumpsysPanel;
import com.adbsimple.panels.ExtractStringsPanel;
import com.adbsimple.panels.InstallPanel;
import com.adbsimple.panels.LogcatPanel;
import com.adbsimple.panels.MonkeyTestPanel;
import com.adbsimple.panels.ScreenRecordPanel;
import com.adbsimple.panels.ScreenShotPanel;
import com.adbsimple.panels.UninstallPanel;

public class HomeFrame extends JFrame implements
		ButtonsPanel.ButtonClickListener, ActionListener {

	private final String TITLE = "ADB Tool";
	private ButtonsPanel buttonsPanel;
	private JPanel placeholderPanel;
	private CardLayout cardLayout;
	private ConnectPanel connectPanel;
	private InstallPanel installPanel;
	private UninstallPanel uninstallPanel;
	private LogcatPanel logcatPanel;
	private ScreenShotPanel screenShotPanel;
	private ScreenRecordPanel screenRecordPanel;
	private ExtractStringsPanel extractStringsPanel;
	private MonkeyTestPanel monkeyTestPanel;
	private DumpsysPanel dumpsysPanel;
	private AboutDialog aboutDialog;
	private JButton refreshJB;
	private String currentCard;

	public HomeFrame() {
		setSize(1000, 700);
		setLocationRelativeTo(null);
		setLayout(null);
		setVisible(true);
		setTitle(TITLE);
		try {
			setIconImage(ImageIO.read(getClass().getResourceAsStream(
					"/com/adbsimple/images/fission-logo.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		if (!checkAndroidPath()) {
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}

		init();
		addPanelsToPlaceholder();

		JMenu helpMenu = new JMenu("Help");
		JMenu toolsMenu = new JMenu("Tools");
		JMenuBar menuBar = new JMenuBar();
		String helpItems[] = { "Help", "About" };
		String toolsItems[] = { "<html>Connect <br>using WIFI</html>",
				"Extract Strings" };

		for (int i = 0; i < helpItems.length; i++) {
			JMenuItem item = new JMenuItem(helpItems[i]);
			item.setPreferredSize(new Dimension(200, 30));
			item.addActionListener(this);
			helpMenu.add(item);
		}
		for (int i = 0; i < toolsItems.length; i++) {
			JMenuItem item = new JMenuItem(toolsItems[i]);
			item.setPreferredSize(new Dimension(200, 30));
			item.addActionListener(this);
			toolsMenu.add(item);
		}
		menuBar.add(toolsMenu);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);
		revalidate();
		refreshJB.addActionListener(this);
		buttonsPanel.setButtonClickListener(this);
	}

	private boolean checkAndroidPath() {
		/*
		 * File file = new File(getClass().getProtectionDomain().getCodeSource()
		 * .getLocation().getPath()); String path = file.getParent() +
		 * File.separator + "src" + File.separator + "com" + File.separator +
		 * "adbsimple" + File.separator + "config" + File.separator +
		 * "config.properties"; File configFile = new File(path);
		 * 
		 * Properties props = new Properties(); FileReader reader; try { reader
		 * = new FileReader(configFile); props.load(reader); if
		 * (props.getProperty("sdk_path") != null &&
		 * isValidPath(props.getProperty("sdk_path"))) { reader.close(); return
		 * true; } else { String sdkPath; while (true) { sdkPath = JOptionPane
		 * .showInputDialog("Please enter Android Sdk path"); if (sdkPath ==
		 * null) { return false; } if (isValidPath(sdkPath)) { break; } }
		 * 
		 * FileWriter writer = new FileWriter(configFile);
		 * props.setProperty("sdk_path", sdkPath); props.store(writer,
		 * "Android SDK Path"); writer.close(); return true; } } catch
		 * (FileNotFoundException e1) { e1.printStackTrace(); } catch
		 * (IOException e) { e.printStackTrace(); }
		 */
		if (System.getenv("ANDROID_HOME") != null
				&& isValidPath(System.getenv("ANDROID_HOME"))) {
			return true;
		}
		JOptionPane
				.showMessageDialog(this,
						"Please define Android SDK path in environment varaibles as ANDROID_HOME");
		return false;

	}

	private boolean isValidPath(String androidPath) {
		File file = new File(androidPath);
		File listFiles[] = file.listFiles();
		for (int i = 0; listFiles != null && i < listFiles.length; i++) {
			if (listFiles[i].getName().equalsIgnoreCase("platform-tools")) {
				return true;
			}
		}

		return false;
	}

	private void init() {
		refreshJB = new JButton("Refresh Devices");
		aboutDialog = new AboutDialog(this);
		buttonsPanel = new ButtonsPanel();
		placeholderPanel = new JPanel();
		cardLayout = new CardLayout();

		connectPanel = new ConnectPanel();
		installPanel = new InstallPanel();
		uninstallPanel = new UninstallPanel();
		logcatPanel = new LogcatPanel();
		screenShotPanel = new ScreenShotPanel();
		screenRecordPanel = new ScreenRecordPanel();
		extractStringsPanel = new ExtractStringsPanel();
		monkeyTestPanel = new MonkeyTestPanel();
		dumpsysPanel = new DumpsysPanel();

		add(buttonsPanel).setBounds(0, 0, 150, 700);
		add(refreshJB).setBounds(160, 2, 130, 16);
		add(placeholderPanel).setBounds(160, 20, 800, 650);

		currentCard = "Install APK";
	}

	private void addPanelsToPlaceholder() {

		placeholderPanel.setLayout(cardLayout);

		placeholderPanel.add("Install APK", installPanel);
		placeholderPanel.add("Uninstall", uninstallPanel);
		placeholderPanel.add("Logcat", logcatPanel);
		placeholderPanel.add("Screen Shot", screenShotPanel);
		placeholderPanel.add("Screen Record", screenRecordPanel);
		placeholderPanel.add("Monkey Test", monkeyTestPanel);
		placeholderPanel.add("Dumpsys", dumpsysPanel);
		placeholderPanel.add("<html>Connect <br>using WIFI</html>",
				connectPanel);
		placeholderPanel.add("Extract Strings", extractStringsPanel);
	}

	@Override
	public void onButtonClickListener(String name) {
		if (logcatPanel.isVisible()) {
			logcatPanel.stopLogcat();
		} else if (screenShotPanel.isVisible()) {
			screenShotPanel.deleteTempFile();
		} else if (screenRecordPanel.isVisible()) {
			screenRecordPanel.deleteTempFile();
		}
		currentCard = name;
		cardLayout.show(placeholderPanel, name);
	}

	public static void main(String args[]) {
		HomeFrame homeFrame = new HomeFrame();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Help")) {
			new HelpFrame();
		} else if (e.getActionCommand().equalsIgnoreCase("About")) {
			aboutDialog.showAboutDialog();
		} else if (e.getActionCommand().equalsIgnoreCase(
				"<html>Connect <br>using WIFI</html>")) {

			onButtonClickListener(e.getActionCommand());

		} else if (e.getActionCommand().equalsIgnoreCase("Extract Strings")) {

			onButtonClickListener(e.getActionCommand());

		} else if (e.getActionCommand().equalsIgnoreCase("Refresh Devices")) {
			if (currentCard.equalsIgnoreCase("Install APK")) {

				installPanel.runDeviceListCMD();

			} else if (currentCard.equalsIgnoreCase("Uninstall")) {

				uninstallPanel.runDeviceListCMD();

			} else if (currentCard.equalsIgnoreCase("Logcat")) {
				logcatPanel.stopLogcat();
				logcatPanel.runDeviceListCMD();

			} else if (currentCard.equalsIgnoreCase("Screen Shot")) {

				screenShotPanel.runDeviceListCMD();

			} else if (currentCard.equalsIgnoreCase("Screen Record")) {
				screenRecordPanel.runDeviceListCMD();
			} else if (currentCard.equalsIgnoreCase("Monkey Test")) {
				monkeyTestPanel.stopLogcat();
				monkeyTestPanel.runDeviceListCMD();
			} else if (currentCard.equalsIgnoreCase("Extract Strings")) {
				extractStringsPanel.runDeviceListCMD();
			} else if (currentCard.equalsIgnoreCase("Dumpsys")) {
				dumpsysPanel.runDeviceListCMD();
			}
		}

	}

}