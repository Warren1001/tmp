package com.kabryxis.tmp.swing;

import com.kabryxis.kabutils.data.Arrays;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

public class ComponentBuilder<T extends Component> {
	
	protected T component;
	
	public ComponentBuilder(T component) {
		this.component = component;
	}
	
	public ComponentBuilder<T> bounds(int x, int y, int width, int height) {
		component.setBounds(x, y, width, height);
		return this;
	}
	
	public ComponentBuilder<T> size(int width, int height) {
		component.setSize(width, height);
		return this;
	}
	
	public ComponentBuilder<T> location(int x, int y) {
		component.setLocation(x, y);
		return this;
	}
	
	public ComponentBuilder<T> location(Point loc) {
		component.setLocation(loc);
		return this;
	}
	
	public ComponentBuilder<T> foregroundColor(Color foregroundColor) {
		component.setForeground(foregroundColor);
		return this;
	}
	
	public ComponentBuilder<T> backgroundColor(Color backgroundColor) {
		component.setBackground(backgroundColor);
		return this;
	}
	
	public ComponentBuilder<T> keyListener(KeyListener keyListener) {
		component.addKeyListener(keyListener);
		return this;
	}
	
	public ComponentBuilder<T> mouseListener(MouseListener mouseListener) {
		component.addMouseListener(mouseListener);
		return this;
	}
	
	public ComponentBuilder<T> mouseListeners(MouseListener... mouseListeners) {
		Arrays.forEach(mouseListeners, component::addMouseListener);
		return this;
	}
	
	public ComponentBuilder<T> visible(boolean visible) {
		component.setVisible(visible);
		return this;
	}
	
	public T build() {
		return component;
	}
	
}
