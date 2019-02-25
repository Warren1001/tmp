package com.kabryxis.tmp.swing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

@FunctionalInterface
public interface BasicKeyListener extends KeyListener {
	
	@Override
	default void keyTyped(KeyEvent e) {}
	
	@Override
	default void keyReleased(KeyEvent e) {}
	
}
