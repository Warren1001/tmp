package com.kabryxis.tmp.swing;

import javax.swing.*;
import java.awt.*;

public class TextAreaBuilder extends ComponentBuilder<JTextArea> {
	
	public TextAreaBuilder(JTextArea area) {
		super(area);
		editable(false);
		wrap(true);
	}
	
	public TextAreaBuilder() {
		this(new JTextArea());
	}
	
	public TextAreaBuilder text(String text) {
		component.setText(text);
		return this;
	}
	
	public TextAreaBuilder font(Font font) {
		component.setFont(font);
		return this;
	}
	
	public TextAreaBuilder editable(boolean editable) {
		component.setFocusable(editable);
		component.setEditable(editable);
		return this;
	}
	
	public TextAreaBuilder wrap(boolean lineWrap) {
		component.setWrapStyleWord(lineWrap);
		component.setLineWrap(lineWrap);
		return this;
	}
	
}
