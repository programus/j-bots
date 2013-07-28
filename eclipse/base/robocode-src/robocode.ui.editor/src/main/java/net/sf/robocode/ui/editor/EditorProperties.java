/*******************************************************************************
 * Copyright (c) 2001-2013 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 *******************************************************************************/
package net.sf.robocode.ui.editor;


import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


/**
 * Handles the editor properties.
 * 
 * @author Flemming N. Larsen (original)
 */
public class EditorProperties {
	public final static String FONT_NAME = "font.name";
	public final static String FONT_STYLE = "font.style";
	public final static String FONT_SIZE = "font.size";

	private String fontName;
	private int fontStyle;
	private int fontSize;

	private final Properties props = new Properties();

	public EditorProperties() {
		super();
	}

	public void setFont(Font font) {
		this.fontName = font.getFamily();
		this.fontStyle = font.getStyle();
		this.fontSize = font.getSize();
	}

	public Font getFont(Font font) {
		return new Font(fontName, fontStyle, fontSize);
	}
	
	public void load(InputStream is) throws IOException {
		props.load(is);

		this.fontName = props.getProperty(FONT_NAME);

		try {
			this.fontStyle = Integer.parseInt(props.getProperty(FONT_STYLE));
		} catch (NumberFormatException e) {
			this.fontStyle = Font.PLAIN; // default on error
		}

		try {
			this.fontSize = Integer.parseInt(props.getProperty(FONT_SIZE));
		} catch (NumberFormatException e) {
			this.fontSize = 14; // default on error
		}
	}

	public void store(OutputStream os, String header) throws IOException {
		props.store(os, header);
	}
}
