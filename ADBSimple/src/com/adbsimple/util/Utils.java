package com.adbsimple.util;

import java.awt.Dimension;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Utils {

	private static JPanel jPanel = new JPanel();
	public static JProgressBar jProgressBar;
	private static JLabel progressJL;
	private static ExecutorService executorService;

	public static void showProgressDialog() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		jProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
		progressJL = new JLabel();

		jPanel.setLayout(null);
		jPanel.setVisible(true);
		jPanel.setSize(400, 100);
		jProgressBar.setIndeterminate(true);

		jPanel.add(progressJL).setBounds(10, 10, 200, 30);
		jPanel.add(jProgressBar).setBounds(10, 50, 250, 20);
		executorService = Executors.newSingleThreadExecutor();

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				UIManager
						.put("OptionPane.minimumSize", new Dimension(450, 100));
				JOptionPane.showOptionDialog(null, jPanel, null,
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE, null, new Object[] {},
						null);

			}
		});
	}

	public static void setProgressText(String text) {
		progressJL.setText(text);
	}

	public static void dismisProgressDialog() {
		progressJL.setText("");
		executorService.shutdownNow();
	}

}
