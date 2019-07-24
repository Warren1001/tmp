package com.kabryxis.tmp.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

public class TextAreaBuilder extends ComponentBuilder<JTextArea> {
	
	public TextAreaBuilder(JTextArea area) {
		super(area);
		fgColor(Color.WHITE);
		editable(false);
		wrap(true);
	}
	
	public TextAreaBuilder() {
		this(new JTextArea());
	}
	
	public TextAreaBuilder(String text) {
		this(new JTextArea(text));
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
	
	public TextAreaBuilder bounds(int x, int y, int width, int height) {
		super.bounds(x, y, width, height);
		return this;
	}
	
	public TextAreaBuilder size(int width, int height) {
		super.size(width, height);
		return this;
	}
	
	public TextAreaBuilder preferredSize(int width, int height) {
		super.preferredSize(width, height);
		return this;
	}
	
	public TextAreaBuilder minSize(int width, int height) {
		super.minSize(width, height);
		return this;
	}
	
	public TextAreaBuilder loc(int x, int y) {
		super.loc(x, y);
		return this;
	}
	
	public TextAreaBuilder loc(Point loc) {
		super.loc(loc);
		return this;
	}
	
	public TextAreaBuilder fgColor(Color foregroundColor) {
		super.fgColor(foregroundColor);
		return this;
	}
	
	public TextAreaBuilder bgColor(Color backgroundColor) {
		super.bgColor(backgroundColor);
		return this;
	}
	
	public TextAreaBuilder keyListener(KeyListener keyListener) {
		super.keyListener(keyListener);
		return this;
	}
	
	public TextAreaBuilder mouseListener(MouseListener mouseListener) {
		super.mouseListener(mouseListener);
		return this;
	}
	
	public TextAreaBuilder mouseListeners(MouseListener... mouseListeners) {
		super.mouseListeners(mouseListeners);
		return this;
	}
	
	public TextAreaBuilder visible(boolean visible) {
		super.visible(visible);
		return this;
	}
	
}
