package com.adbsimple.panels;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import com.adbsimple.interfaces.Const;

public class EditOptionsPanel extends JPanel implements ActionListener, Const {
	private JRadioButton drawRectJRB, drawLineJRB, drawTextJRB;
	private ButtonGroup btnGroup;
	private DrawingPanel drawingPanel;
	private JButton colorChooseJB, undoJB;

	public EditOptionsPanel(DrawingPanel drawingPanel) {
		setLayout(null);

		this.drawingPanel = drawingPanel;
		Border borderFactory = BorderFactory.createLoweredBevelBorder();
		setBorder(borderFactory);

		init();
		addComponents();

		drawRectJRB.addActionListener(this);
		drawLineJRB.addActionListener(this);
		drawTextJRB.addActionListener(this);
		colorChooseJB.addActionListener(this);
		undoJB.addActionListener(this);

		// Set initial color
		drawingPanel.setColor(Color.RED);
	}

	private void init() {
		drawRectJRB = new JRadioButton("Draw rectangle");
		drawLineJRB = new JRadioButton("Draw line");
		drawTextJRB = new JRadioButton("Draw text");
		btnGroup = new ButtonGroup();
		colorChooseJB = new JButton("Select Color");
		undoJB = new JButton("Undo");
	}

	private void addComponents() {
		add(drawRectJRB).setBounds(20, 10, 100, 30);
		add(drawLineJRB).setBounds(20, 50, 100, 30);
		add(drawTextJRB).setBounds(20, 90, 100, 30);
		add(colorChooseJB).setBounds(20, 140, 100, 30);
		add(undoJB).setBounds(20, 190, 100, 30);

		btnGroup.add(drawRectJRB);
		btnGroup.add(drawLineJRB);
		btnGroup.add(drawTextJRB);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Draw rectangle")) {
			drawingPanel.setShape(RECTANGLE);
		} else if (e.getActionCommand().equalsIgnoreCase("Draw line")) {
			drawingPanel.setShape(LINE);

		} else if (e.getActionCommand().equalsIgnoreCase("Draw text")) {
			drawingPanel.setShape(TEXT);
		} else if (e.getActionCommand().equalsIgnoreCase("Select Color")) {
			Color newColor = JColorChooser.showDialog(null, "Select a color",
					drawingPanel.getColor());
			if (newColor != null) {
				drawingPanel.setColor(newColor);
			}
		} else if (e.getActionCommand().equalsIgnoreCase("Undo")) {
			drawingPanel.undoLastDrawing();
		}

	}

	public void resetValues() {
		btnGroup.clearSelection();
	}
}
