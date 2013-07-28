package net.sf.robocode.ui.editor;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;


public class HighlightLinePainter implements Highlighter.HighlightPainter {

	private JTextComponent component;
	private Color color;
	private Rectangle lastView;

	public HighlightLinePainter(JTextComponent component) {
		this.component = component;

		color = new Color(0xff, 0xff, 0x00, 0x7f); // transparent yellow

		component.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				resetHighlight();
			}
		});

		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				resetHighlight();
			}
		});
		
		// Turn highlighting on by adding a dummy highlight
		try {
			component.getHighlighter().addHighlight(0, 0, this);
		} catch (BadLocationException ignore) {}
	}

	public HighlightLinePainter(JTextComponent component, Color color) {
		this(component);
		setColor(color);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
		try {
			Rectangle r = c.modelToView(c.getCaretPosition());

			g.setColor(color);
			g.fillRect(0, r.y, c.getWidth(), r.height);

			if (lastView == null) {
				lastView = r;
			}
		} catch (BadLocationException ignore) {}
	}

	private void resetHighlight() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					int pos = component.getCaretPosition();
					Rectangle currentView = component.modelToView(pos);

					if (currentView != null && lastView != null) {
						// Remove the highlighting from the previously highlighted line
						if (lastView.y != currentView.y) {
							component.repaint(0, lastView.y, component.getWidth(), lastView.height);
							lastView = currentView;
						}
					}
				} catch (BadLocationException ignore) {}
			}
		});
	}
}
