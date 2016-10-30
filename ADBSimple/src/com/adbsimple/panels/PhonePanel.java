package com.adbsimple.panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class PhonePanel extends JPanel {

	private Image img;

	public PhonePanel() {
		setVisible(true);
		setLayout(null);

		setBackground(new Color(1, 1, 1, (float) 0.01));
		setOpaque(false);
		try {
			img = ImageIO.read(getClass().getResourceAsStream(
					"/com/adbsimple/images/phone.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, null);
		super.paintComponent(g);
	}

}
