/*******************************************************************************
 * Copyright (c) 2001-2013 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 *******************************************************************************/
package net.sf.robocode.ui.editor;


import javax.swing.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Flemming N. Larsen (original)
 *
 * @since 1.8.2.0
 */
@SuppressWarnings("serial")
public class FontChooserDialog extends JDialog {

	private static final boolean ONLY_MONOSPACED = true;
	
	private JButton okButton;
	private JButton cancelButton;

	private JComboBox fontNameComboBox;
	private JComboBox fontStyleComboBox;
	private JComboBox fontSizeComboBox;
	
	private final JLabel previewLabel = new JLabel("AaBbYyZz");

	private final EventHandler eventHandler = new EventHandler();

	public FontChooserDialog(JFrame owner) {
		super(owner, true);
		initialize();
	}

	private void initialize() {
		setTitle("Font Chooser");
		setLayout(new GridBagLayout());
		setResizable(false);

		GridBagConstraints gbc = new GridBagConstraints();

		JPanel comboBoxesPanel = new JPanel();
		comboBoxesPanel.add(getFontNamePanel());
		comboBoxesPanel.add(getFontStylePanel());
		comboBoxesPanel.add(getFontSizePanel());

		gbc.insets = new Insets(5, 5, 5, 5);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		add(comboBoxesPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(getPreviewPanel(), gbc);
	}

	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton("OK");
			okButton.setMnemonic('O');
			okButton.addActionListener(eventHandler);
		}
		return okButton;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton("Cancel");
			cancelButton.setMnemonic('C');
			cancelButton.addActionListener(eventHandler);
		}
		return cancelButton;
	}

	private JComboBox getFontNameComboBox() {
		if (fontNameComboBox == null) {
			String[] fontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			List<String> fontFamilyNameList = Arrays.asList(fontFamilyNames);

			List<String> fontNameList = new ArrayList<String>();

			if (ONLY_MONOSPACED) {
				for (String monospacedFontName : getMonospacedFontNames()) {
					if (fontFamilyNameList.contains(monospacedFontName)) {
						fontNameList.add(monospacedFontName);
					}
				}
			} else {
				fontNameList.addAll(fontFamilyNameList);
			}
			
			fontNameComboBox = new JComboBox(fontNameList.toArray());
			fontNameComboBox.setRenderer(new FontCellRenderer());
		}
		return fontNameComboBox;
	}
	
	private JComboBox getFontStyleComboBox() {
		if (fontStyleComboBox == null) {
			List<String> fontStyles = new ArrayList<String>();
			for (FontStyle fontStyle : FontStyle.values()) {
				fontStyles.add(fontStyle.getName());
			}
			fontStyleComboBox = new JComboBox(fontStyles.toArray());
			fontStyleComboBox.setRenderer(new FontStyleCellRenderer());
		}
		return fontStyleComboBox;
	}

	private JComboBox getFontSizeComboBox() {
		if (fontSizeComboBox == null) {
			String[] sizes = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"	};
			fontSizeComboBox = new JComboBox(sizes);
			fontSizeComboBox.setSelectedIndex(5);
		}
		return fontSizeComboBox;
	}

	private JPanel getFontNamePanel() {
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Font Name");

		panel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		panel.add(label, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL; 
		gbc.weightx = 1.0;
		panel.add(getFontNameComboBox(), gbc);

		return panel;
	}

	private JPanel getFontStylePanel() {
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Style");

		panel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		panel.add(label, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL; 
		gbc.weightx = 1.0;
		panel.add(getFontStyleComboBox(), gbc);

		return panel;
	}

	private JPanel getFontSizePanel() {
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Size");

		panel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		panel.add(label, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL; 
		gbc.weightx = 1.0;
		panel.add(getFontSizeComboBox(), gbc);

		return panel;
	}

	private JPanel getPreviewPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.setBackground(Color.WHITE);
		panel.setForeground(Color.BLACK);
		panel.setPreferredSize(new Dimension(400, 100));

		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setPreviewLabelFont(panel);
				panel.repaint();
			}
		};

		fontNameComboBox.addActionListener(actionListener);
		fontStyleComboBox.addActionListener(actionListener);
		fontSizeComboBox.addActionListener(actionListener);

		setPreviewLabelFont(panel);
		panel.add(previewLabel, SwingConstants.CENTER);
		
		return panel;
	}

	private void setPreviewLabelFont(JPanel panel) {
		String fontName = (String)fontNameComboBox.getSelectedItem();
		int fontStyleFlags = FontStyle.fromName((String)fontStyleComboBox.getSelectedItem()).getFontStyleFlags();
		int fontSize = Integer.parseInt((String)fontSizeComboBox.getSelectedItem());

		Font font = new Font(fontName, fontStyleFlags, fontSize);
		
		previewLabel.setFont(font);
		
		FontMetrics fontMetrics = panel.getFontMetrics(font);

		int width = fontMetrics.stringWidth(previewLabel.getText());
		int height = fontMetrics.getHeight();

		previewLabel.setPreferredSize(new Dimension(width, height));		
	}
	
	private class EventHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(getOkButton())) {
				// saveCompilerProperties();
				dispose();
			}
			if (e.getSource().equals(getCancelButton())) {
				dispose();
			}
		}
	}

	private List<String> getMonospacedFontNames() {
		List<String> monospacedFontNames = new ArrayList<String>();
		
		Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (Font font : allFonts) {
			if (isMonospaced(font)) {
				monospacedFontNames.add(font.getFontName());
			}
		}
		return monospacedFontNames;
	}
	
	private boolean isMonospaced(Font font) {
		FontMetrics fontMetrics = getFontMetrics(font);

		int lastCharWidth = -1;

		for (int charCode = 0; charCode < 128; charCode++) {
			if (Character.isValidCodePoint(charCode) && (Character.isLetter(charCode) || Character.isDigit(charCode))) {
				int charWidth = fontMetrics.charWidth((char)charCode);
				if (lastCharWidth >= 0 && charWidth != lastCharWidth) {
					return false; // font is not mono-spaced
				}
				lastCharWidth = charWidth;
			}
		}
		return true; // font is mono-spaced
	}

	private class FontCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	
			Font font = new Font((String) value, Font.PLAIN, 20);
			label.setFont(font);
			return label;
		}
	}

	private class FontStyleCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			FontStyle fontStyle = FontStyle.fromName((String) value);
			int styleFlags = (fontStyle == null) ? Font.PLAIN : fontStyle.getFontStyleFlags();
	
			Font font = new Font((String) value, styleFlags, 12);
			label.setFont(font);
			return label;
		}
	}
}
