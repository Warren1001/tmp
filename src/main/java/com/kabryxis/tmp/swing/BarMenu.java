package com.kabryxis.tmp.swing;

import com.kabryxis.kabutils.data.NumberConversions;

import javax.swing.*;
import java.awt.*;

public class BarMenu {
	
	private final JPanel left, right;
	private final int height;
	
	public BarMenu(int width, int height, float lp, int hgap, int vgap, Color backgroundColor) {
		left = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
		right = new JPanel(new FlowLayout(FlowLayout.RIGHT, hgap, vgap));
		right.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		left.setBackground(backgroundColor);
		right.setBackground(backgroundColor);
		this.height = height;
		int lwidth = NumberConversions.floor(width * lp);
		left.setBounds(0, 0, lwidth, height);
		right.setBounds(lwidth, 0, width - lwidth, height);
	}
	
	public void addTo(Container container) {
		container.add(left);
		container.add(right);
	}
	
	public void addLeft(Component comp) {
		comp.setSize(comp.getWidth(), Math.min(comp.getHeight(), height));
		left.add(comp);
	}
	
	public void addLeft(Component comp, int index) {
		comp.setSize(comp.getWidth(), Math.min(comp.getHeight(), height));
		left.add(comp, index);
	}
	
	public void addRight(Component comp) {
		comp.setSize(comp.getWidth(), Math.min(comp.getHeight(), height));
		right.add(comp);
	}
	
	public void addRight(int index, Component comp) {
		comp.setSize(comp.getWidth(), Math.min(comp.getHeight(), height));
		right.add(comp, index);
	}
	
	public void setVisible(boolean visible) {
		left.setVisible(visible);
		right.setVisible(visible);
	}
	
}
