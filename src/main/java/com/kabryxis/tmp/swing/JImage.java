package com.kabryxis.tmp.swing;

import javax.swing.*;
import java.awt.*;

public class JImage extends JLabel {
	
	public JImage(Image image) {
		super(new ImageIcon(image));
		setSize(image.getWidth(null), image.getHeight(null));
	}
	
}
