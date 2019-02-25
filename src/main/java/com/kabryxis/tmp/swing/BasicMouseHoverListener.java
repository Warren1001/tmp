package com.kabryxis.tmp.swing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@FunctionalInterface
public interface BasicMouseHoverListener extends MouseListener {
	
	@Override
	default void mouseEntered(MouseEvent e) {
		hover(e, true);
	}
	
	@Override
	default void mouseExited(MouseEvent e) {
		hover(e, false);
	}
	
	@Override
	default void mouseClicked(MouseEvent e) {}
	
	@Override
	default void mousePressed(MouseEvent e) {}
	
	@Override
	default void mouseReleased(MouseEvent e) {}
	
	void hover(MouseEvent event, boolean hover);

}
