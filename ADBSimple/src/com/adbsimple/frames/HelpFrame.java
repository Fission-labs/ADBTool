package com.adbsimple.frames;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;

public class HelpFrame extends JFrame {

	private JTextPane helpJTP;
	private JScrollPane jScrollPane;
	private JLabel connectWIFIJL, installJL, uninstallJL, logcatJL,
			screenshotJL, screenrecordJL, monkeytestJL, dumpsysJL,
			extractStringsJL, refreshDevicesJL;

	public HelpFrame() {
		setSize(1000, 650);
		setLayout(null);
		setVisible(true);
		setLocationRelativeTo(null);
		setTitle("Help");
		try {
			setIconImage(ImageIO.read(getClass().getResourceAsStream(
					"/com/adbsimple/images/fission-logo.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		init();

		// Add mouse listener
		setMouseListener(connectWIFIJL);
		setMouseListener(installJL);
		setMouseListener(uninstallJL);
		setMouseListener(logcatJL);
		setMouseListener(screenshotJL);
		setMouseListener(screenrecordJL);
		setMouseListener(monkeytestJL);
		setMouseListener(dumpsysJL);
		setMouseListener(extractStringsJL);
		setMouseListener(refreshDevicesJL);

		// Add components to JFrame
		add(jScrollPane).setBounds(200, 20, 750, 550);
		add(connectWIFIJL).setBounds(10, 30, 100, 30);
		add(installJL).setBounds(10, 80, 100, 30);
		add(uninstallJL).setBounds(10, 130, 100, 30);
		add(logcatJL).setBounds(10, 180, 100, 30);
		add(screenshotJL).setBounds(10, 230, 100, 30);
		add(screenrecordJL).setBounds(10, 280, 100, 30);
		add(monkeytestJL).setBounds(10, 330, 100, 30);
		add(dumpsysJL).setBounds(10, 380, 100, 30);
		add(extractStringsJL).setBounds(10, 430, 100, 30);
		add(refreshDevicesJL).setBounds(10, 480, 100, 30);

		setHelpText();
	}

	private void init() {
		helpJTP = new JTextPane();
		/*
		 * Text on JLabel should be equal to the text in help.html file other
		 * wise index may not work perfectly
		 */
		connectWIFIJL = new JLabel("Connect using WIFI");
		installJL = new JLabel("Install APK");
		uninstallJL = new JLabel("Uninstall");
		logcatJL = new JLabel("Logcat");
		screenshotJL = new JLabel("Screen Shot");
		screenrecordJL = new JLabel("Screen Record");
		monkeytestJL = new JLabel("Monkey Test");
		dumpsysJL = new JLabel("Dumpsys");
		extractStringsJL = new JLabel("Extract Strings");
		refreshDevicesJL = new JLabel("Refresh Devices");
		jScrollPane = new JScrollPane(helpJTP);
		jScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		helpJTP.setEditable(false);

		connectWIFIJL.setForeground(Color.BLUE);
		installJL.setForeground(Color.BLUE);
		uninstallJL.setForeground(Color.BLUE);
		logcatJL.setForeground(Color.BLUE);
		screenshotJL.setForeground(Color.BLUE);
		screenrecordJL.setForeground(Color.BLUE);
		monkeytestJL.setForeground(Color.BLUE);
		dumpsysJL.setForeground(Color.BLUE);
		extractStringsJL.setForeground(Color.BLUE);
		refreshDevicesJL.setForeground(Color.BLUE);
	}

	private void setHelpText() {
		helpJTP.setContentType("text/html");
		URL url = getClass().getResource("/com/adbsimple/util/help.html");
		try {
			helpJTP.setPage(url);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void setMouseListener(final JLabel jLabel) {
		jLabel.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent me) {
				jLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent me) {
				jLabel.setCursor(Cursor.getDefaultCursor());
			}

			public void mouseClicked(MouseEvent me) {
				try {
					int textPosition = helpJTP.getDocument()
							.getText(0, helpJTP.getDocument().getLength())
							.indexOf(jLabel.getText());
					helpJTP.setCaretPosition(textPosition);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}

			}
		});
	}
}
