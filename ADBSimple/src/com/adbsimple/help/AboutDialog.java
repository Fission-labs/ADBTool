package com.adbsimple.help;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class AboutDialog extends JDialog {
	private JLabel versionJL, authorJL, powerdbyJL;
	private JLabel versionNumJL, authorNameJL, powerdbynameJL;
	private final String VERSION = "1.1";
	private final String TITLE = "About";
	private final String fissionURI = "http://www.fissionlabs.com/";

	public AboutDialog(JFrame parent) {
		super(parent, true);
		if (parent != null) {
			setSize(300, 300);
			setLayout(null);
			setTitle(TITLE);
			setLocationRelativeTo(null);
			versionJL = new JLabel("Version");
			authorJL = new JLabel("Developer");
			powerdbyJL = new JLabel("Developed by");
			versionNumJL = new JLabel(VERSION);
			authorNameJL = new JLabel("Kiran Rajam");
			powerdbynameJL = new JLabel("<HTML>"
					+ "<FONT color=\"#000099\"><U> Fission Labs </U></FONT>"
					+ "</HTML>");

			powerdbynameJL.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent me) {
					powerdbynameJL.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}

				public void mouseExited(MouseEvent me) {
					powerdbynameJL.setCursor(Cursor.getDefaultCursor());
				}

				public void mouseClicked(MouseEvent me) {
					try {
						if (Desktop.isDesktopSupported()) {
							Desktop desktop = Desktop.getDesktop();
							URI uri = new URI(fissionURI);
							desktop.browse(uri);
						} else {
							JOptionPane
									.showMessageDialog(
											null,
											"Can not open website link from application, \n"
													+ "Copy this link and paste it on webbrowser  "
													+ fissionURI);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			add(versionJL).setBounds(20, 20, 100, 30);
			add(versionNumJL).setBounds(140, 20, 100, 30);
			add(authorJL).setBounds(20, 60, 100, 30);
			add(authorNameJL).setBounds(140, 60, 100, 30);
			add(powerdbyJL).setBounds(20, 100, 100, 30);
			add(powerdbynameJL).setBounds(140, 100, 100, 30);

			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}
	}

	public void showAboutDialog() {
		setVisible(true);
	}
}
