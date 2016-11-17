package com.adbsimple.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class ButtonsPanel extends JPanel implements ActionListener {
	JButton connectWithWifiBtn, installBtn, uninstallBtn, logcatBtn,
			screenshotBtn, screenrecordBtn, monkeyTestBtn, dumpBtn;
	private int btn_width = 120, btn_height = 50;

	private ButtonClickListener buttonClickListener;

	public ButtonsPanel() {
		setLayout(null);
		setVisible(true);
		Border border = BorderFactory.createRaisedBevelBorder();
		setBorder(border);

		init();
		addComponentsToPanel();
		addListeners();
	}

	private void init() {
		connectWithWifiBtn = new JButton("<html>Connect <br>using WIFI</html>");
		// connectWithWifiBtn.setBorderPainted(false);
		connectWithWifiBtn.setFocusPainted(false);
		connectWithWifiBtn.setContentAreaFilled(false);

		installBtn = new JButton("Install APK");
		installBtn.setFocusPainted(false);
		installBtn.setContentAreaFilled(false);

		uninstallBtn = new JButton("Uninstall");
		uninstallBtn.setFocusPainted(false);
		uninstallBtn.setContentAreaFilled(false);

		logcatBtn = new JButton("Logcat");
		logcatBtn.setFocusPainted(false);
		logcatBtn.setContentAreaFilled(false);

		screenshotBtn = new JButton("Screen Shot");
		screenshotBtn.setFocusPainted(false);
		screenshotBtn.setContentAreaFilled(false);

		screenrecordBtn = new JButton("Screen Record");
		screenrecordBtn.setFocusPainted(false);
		screenrecordBtn.setContentAreaFilled(false);

		monkeyTestBtn = new JButton("Monkey Test");
		monkeyTestBtn.setFocusPainted(false);
		monkeyTestBtn.setContentAreaFilled(false);

		dumpBtn = new JButton("Dump");
		dumpBtn.setFocusPainted(false);
		dumpBtn.setContentAreaFilled(false);
	}

	private void addComponentsToPanel() {
		add(installBtn).setBounds(10, 20, btn_width, btn_height);
		add(uninstallBtn).setBounds(10, 80, btn_width, btn_height);
		add(logcatBtn).setBounds(10, 140, btn_width, btn_height);
		add(screenshotBtn).setBounds(10, 200, btn_width, btn_height);
		add(screenrecordBtn).setBounds(10, 260, btn_width, btn_height);
		add(monkeyTestBtn).setBounds(10, 320, btn_width, btn_height);
		add(dumpBtn).setBounds(10, 380, btn_width, btn_height);
	}

	private void addListeners() {
		connectWithWifiBtn.addActionListener(this);
		installBtn.addActionListener(this);
		uninstallBtn.addActionListener(this);
		logcatBtn.addActionListener(this);
		screenshotBtn.addActionListener(this);
		screenrecordBtn.addActionListener(this);
		monkeyTestBtn.addActionListener(this);
		dumpBtn.addActionListener(this);
	}

	public void setButtonClickListener(ButtonClickListener listener) {
		buttonClickListener = listener;
	}

	public interface ButtonClickListener {
		void onButtonClickListener(String cmd);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		buttonClickListener.onButtonClickListener(e.getActionCommand());
	}

}
