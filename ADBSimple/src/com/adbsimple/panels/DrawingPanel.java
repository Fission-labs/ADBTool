package com.adbsimple.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;

import com.adbsimple.interfaces.Const;

public class DrawingPanel extends JPanel implements Const {
	private Shape currentShape, shape;
	private BufferedImage image;
	private Point startPoint;
	private int width, height;
	private Rectangle2D.Float rectangle2DShape;
	private Line2D.Float line2DShape;
	private Color color;
	private Point lineStart;
	private boolean isTextBox = false;
	private JTextArea jTextArea;
	private Border border;
	private ArrayList<JTextArea> jTextAL;
	private Stack<Object> stackForUndo;

	public DrawingPanel(int width, int height) {
		setLayout(null);
		setVisible(true);
		this.width = width;
		this.height = height;

		stackForUndo = new Stack<Object>();

		jTextAL = new ArrayList<JTextArea>();
		rectangle2DShape = new Rectangle2D.Float();
		line2DShape = new Line2D.Float();

		addMouseListener(new MyMouseListener());
		addMouseMotionListener(new MyMouseListener());
	}

	public void showImagePreview(String path) {
		clearDrawingArea();

		Icon icon = new ImageIcon(path);
		// image = ImageIO.read(new File(path));
		image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		icon.paintIcon(null, g2d, 0, 0);
		g2d.dispose();

		image = getResizedImage(width, height, image);
		repaint();
	}

	private class MyMouseListener extends MouseInputAdapter {

		public void mousePressed(MouseEvent e) {
			startPoint = e.getPoint();
			lineStart = e.getPoint();
			if (isTextBox) {
				jTextArea = new JTextArea();
				border = BorderFactory.createLineBorder(color);
				jTextArea.setBorder(border);
				jTextArea.setForeground(color);
				jTextAL.add(jTextArea);
			} else {
				shape = currentShape;
			}
		}

		public void mouseDragged(MouseEvent e) {
			int x = Math.min(startPoint.x, e.getX());
			int y = Math.min(startPoint.y, e.getY());
			int width = Math.abs(startPoint.x - e.getX());
			int height = Math.abs(startPoint.y - e.getY());

			if (shape instanceof Rectangle2D.Float) {
				((Rectangle2D.Float) shape).setRect(x, y, width, height);
			} else if (shape instanceof Line2D.Float) {
				((Line2D.Float) shape).setLine(lineStart, e.getPoint());
				/*
				 * placeShape(shape); lineStart = e.getPoint();
				 */
			} else if (isTextBox) {
				add(jTextArea).setBounds(x, y, width, height);
			}

			repaint();
		}

		public void mouseReleased(MouseEvent e) {
			placeShape(shape);
			shape = null;
		}

	}

	public void placeShape(Shape shape) {
		// Draw the Shape onto the BufferedImage
		if (image != null) {
			if (isTextBox) {
				stackForUndo.push(jTextArea);
			} else {
				stackForUndo.push(copyImage(image));
			}
		}
		if (image != null && shape != null) {
			Graphics2D g2d = (Graphics2D) image.getGraphics();
			g2d.setColor(color);
			g2d.draw(shape);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Custom code to support painting from the BufferedImage

		if (image != null) {
			g.drawImage(image, 0, 0, null);
		}

		// Paint the Rectangle as the mouse is being dragged
		if (shape != null) {
			Graphics2D g2d = (Graphics2D) g;
			BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);

			g2d.setStroke(stroke);
			g2d.setColor(color);
			g2d.draw(shape);
		}
	}

	private BufferedImage getResizedImage(int w, int h, BufferedImage img) {

		BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = tmp.createGraphics();
		g2.drawImage(img, 0, 0, w, h, null);
		g2.dispose();
		return tmp;

	}

	public void saveImage(String file_path) {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		paint(g2);
		try {
			ImageIO.write(image, "png", new File(file_path + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clearDrawingArea() {
		image = null;
		for (int i = 0; i < jTextAL.size(); i++) {
			remove(jTextAL.get(i));
		}
		stackForUndo.clear();

		repaint();
	}

	public boolean isImageShowing() {
		return (image != null);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setShape(String str) {
		if (str.equalsIgnoreCase(RECTANGLE)) {
			currentShape = rectangle2DShape;
			isTextBox = false;
		} else if (str.equalsIgnoreCase(LINE)) {
			currentShape = line2DShape;
			isTextBox = false;
		} else if (str.equalsIgnoreCase(TEXT)) {
			isTextBox = true;
		}

	}

	public void undoLastDrawing() {
		if (stackForUndo.empty()) {
			return;
		}

		Object lastState = stackForUndo.pop();

		if (lastState instanceof BufferedImage) {
			image = (BufferedImage) lastState;
		} else if (lastState instanceof JTextArea) {
			remove((JTextArea) lastState);
		}

		shape = null;
		repaint();
	}

	private BufferedImage copyImage(BufferedImage source) {
		BufferedImage b = new BufferedImage(source.getWidth(),
				source.getHeight(), source.getType());
		Graphics g = b.getGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();
		return b;
	}

	public void resetValues() {
		currentShape = null;
		isTextBox = false;
		stackForUndo.clear();
	}

}
