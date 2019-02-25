package com.kabryxis.tmp.swing;

import com.kabryxis.kabutils.Images;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class FadingImage extends JImage {
	
	private JImage fadedImage;
	private MouseListener mouseListener;
	
	private boolean hasSetup = false;
	private boolean manual = false;
	
	public FadingImage(Image image) {
		super(image);
		addHierarchyListener(event -> {
			if(!hasSetup && event.getChangeFlags() == HierarchyEvent.PARENT_CHANGED) createFadedImage();
		});
	}
	
	protected void createFadedImage() {
		hasSetup = true;
		BufferedImage copy = Images.copy(((ImageIcon)getIcon()).getImage());
		for(int width = 0; width < copy.getWidth(); width++) {
			for(int height = 0; height < copy.getHeight(); height++) {
				Color color = new Color(copy.getRGB(width, height), true);
				if(color.getAlpha() == 0) color = getParent().getBackground();
				color = color.getRed() + color.getGreen() + color.getBlue() > 510 ? color.darker().darker() : color.brighter().brighter();
				copy.setRGB(width, height, color.getRGB());
			}
		}
		fadedImage = new JImage(copy);
		mouseListener = (BasicMouseHoverListener)(event, hover) -> setFaded(hover);
		if(!manual) addMouseListener(mouseListener);
		getParent().add(new ComponentBuilder<>(fadedImage).location(getLocation()).visible(false).mouseListeners(getMouseListeners()).build());
	}
	
	public FadingImage setManual(boolean manual) {
		this.manual = manual;
		if(manual) {
			removeMouseListener(mouseListener);
			fadedImage.removeMouseListener(mouseListener);
		}
		else {
			addMouseListener(mouseListener);
			fadedImage.addMouseListener(mouseListener);
		}
		return this;
	}
	
	public FadingImage setFaded(boolean faded) {
		setVisible(!faded);
		fadedImage.setVisible(faded);
		return this;
	}
	
}
