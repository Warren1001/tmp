package com.kabryxis.tmp.swing;

import com.kabryxis.kabutils.data.Arrays;

import javax.swing.*;
import java.awt.*;

public class FrameBuilder extends ComponentBuilder<JFrame> {
	
	public FrameBuilder() {
		super(new JFrame());
	}
	
	public FrameBuilder(String title) {
		super(new JFrame(title));
		component.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
	public FrameBuilder layout(LayoutManager layoutManager) {
		component.setLayout(layoutManager);
		return this;
	}
	
	public FrameBuilder title(String title) {
		component.setTitle(title);
		return this;
	}
	
	public FrameBuilder icon(Image image) {
		component.setIconImage(image);
		return this;
	}
	
	/**
	 * @see javax.swing.WindowConstants
	 *
	 * @param operation A WindowConstants int
	 * @return The builder
	 */
	public FrameBuilder closeOperation(int operation) {
		component.setDefaultCloseOperation(operation);
		return this;
	}
	
	public FrameBuilder menuBar(JMenuBar menuBar) {
		component.setJMenuBar(menuBar);
		return this;
	}
	
	public FrameBuilder add(Component... components) {
		Arrays.forEach(components, component::add);
		return this;
	}
	
}
