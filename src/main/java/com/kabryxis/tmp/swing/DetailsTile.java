package com.kabryxis.tmp.swing;

import com.kabryxis.kabutils.Images;
import com.kabryxis.tmp.media.Show;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

public class DetailsTile extends JPanel {
	
	private static final int HEIGHT = 100;
	
	private final Set<Component> hoverComponents = new HashSet<>();
	
	private final MouseListener hoverListener;
	private final JTextArea title;
	
	public DetailsTile(Show show, Image image) {
		super(new FlowLayout(FlowLayout.LEFT, 10, 0));
		hoverComponents.add(this);
		setPreferredSize(new Dimension(1920, HEIGHT));
		//setBorder(null);
		setBackground(Color.DARK_GRAY);
		hoverListener = new BasicMouseListener() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				show.getMediaManager().getTMP().getUserManager().getSelectedUser().getShowTracker(show).getLastSeasonTracker().getLastEpisode().play();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				hoverComponents.forEach(component -> component.setBackground(Color.GRAY));
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				hoverComponents.forEach(component -> component.setBackground(Color.DARK_GRAY));
			}
			
		};
		addMouseListener(hoverListener);
		int currentWidth = 0;
		JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		imagePanel.setBounds(currentWidth, 0, 150, HEIGHT);
		imagePanel.setBackground(Color.DARK_GRAY);
		JImage jImage = new JImage(Images.reduce(image, 150, HEIGHT));
		jImage.addMouseListener(hoverListener);
		hoverComponents.add(jImage);
		imagePanel.add(jImage);
		add(imagePanel);
		currentWidth = jImage.getWidth();
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		titlePanel.setBounds(currentWidth, 0, 240, HEIGHT);
		titlePanel.setBackground(Color.DARK_GRAY);
		String titleString = show.getFriendlyName();
		String englishName = show.getData().get("english", String.class);
		title = new TextAreaBuilder().wrap(false).font(new Font("Segoe Print", Font.BOLD, 16))
				.backgroundColor(Color.DARK_GRAY).foregroundColor(Color.WHITE).mouseListener(hoverListener).build();
		hoverComponents.add(title);
		if(englishName != null) {
			boolean onlyEnglish = show.getMediaManager().getTMP().getUserManager().getSelectedUser().getData().getBoolean("only-english", false);
			if(onlyEnglish) titleString = englishName;
			else titleString += " - (" + englishName + ")";
			show.getMediaManager().getTMP().getUserManager().registerSelectedUserListener(user ->
					title.setText(user.getData().getBoolean("only-english", false) ? englishName : show.getFriendlyName() + " - (" + englishName + ")"));
		}
		title.setText(titleString);
		titlePanel.add(title);
		add(titlePanel);
		currentWidth += titlePanel.getWidth();
		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		textPanel.setBounds(currentWidth, 0, 450, HEIGHT);
		textPanel.setBackground(Color.DARK_GRAY);
		JTextArea text = new TextAreaBuilder().text(show.getDescription()).backgroundColor(Color.DARK_GRAY)
				.foregroundColor(Color.WHITE).size(900, HEIGHT).build();
		text.addMouseListener(hoverListener);
		hoverComponents.add(text);
		textPanel.add(text);
		add(textPanel);
		currentWidth += textPanel.getWidth();
		JPanel browsePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		browsePanel.setBounds(currentWidth, 0, 240, HEIGHT);
		browsePanel.setBackground(Color.DARK_GRAY);
		JTextArea browse = new TextAreaBuilder().wrap(false).text("Browse").font(new Font("Arial", Font.BOLD, 15)).backgroundColor(Color.DARK_GRAY)
				.foregroundColor(Color.BLUE).build();
		browse.addMouseListener((BasicMouseListener)e -> show.getMediaManager().getTMP().setCurrentlyVisibleMainPanel(show.getPagePanel()));
		hoverComponents.add(browse);
		browsePanel.add(browse);
		add(browsePanel);
		currentWidth += browsePanel.getWidth();
	}
	
	@Override
	public Component add(Component component) {
		hoverComponents.add(component);
		component.addMouseListener(hoverListener);
		return super.add(component);
	}
	
}
