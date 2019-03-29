package com.kabryxis.tmp.swing;

import com.kabryxis.kabutils.Images;
import com.kabryxis.tmp.media.Show;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

public class BlockTile extends JPanel {
	
	private final Set<Component> hoverComponents = new HashSet<>();
	
	private final MouseListener hoverListener;
	private final JTextArea title;
	
	public BlockTile(Show show, Image image) {
		super(null);
		hoverComponents.add(this);
		setPreferredSize(new Dimension(450, 667));
		setBorder(null);
		setBackground(Color.DARK_GRAY);
		hoverListener = new MouseListener() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				show.getMediaManager().getTMP().getUserManager().getSelectedUser().getShowTracker(show).getLastSeasonTracker().getLastEpisode().play();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseReleased(MouseEvent e) {}
			
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
		int currentHeight = 0;
		JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		imagePanel.setBounds(0, currentHeight, 450, 300);
		imagePanel.setBackground(Color.DARK_GRAY);
		JImage jImage = new JImage(Images.reduce(image, 450, 300));
		jImage.addMouseListener(hoverListener);
		hoverComponents.add(jImage);
		imagePanel.add(jImage);
		add(imagePanel);
		currentHeight = jImage.getHeight();
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		titlePanel.setBounds(0, currentHeight, 450, 50);
		titlePanel.setBackground(Color.DARK_GRAY);
		String titleString = show.getFriendlyName();
		String englishName = show.getData().get("english", String.class);
		title = new TextAreaBuilder().wrap(false).font(new Font("Segoe Print", Font.BOLD, 21))
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
		currentHeight += titlePanel.getHeight();
		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		textPanel.setBounds(0, currentHeight, 450, 100);
		textPanel.setBackground(Color.DARK_GRAY);
		JTextArea text = new TextAreaBuilder().text(show.getDescription()).backgroundColor(Color.DARK_GRAY)
				.foregroundColor(Color.WHITE).size(444, 100).build();
		text.addMouseListener(hoverListener);
		hoverComponents.add(text);
		textPanel.add(text);
		add(textPanel);
		currentHeight += textPanel.getHeight();
		JPanel browsePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		browsePanel.setBounds(0, currentHeight, 450, 50);
		browsePanel.setBackground(Color.DARK_GRAY);
		JTextArea browse = new TextAreaBuilder().wrap(false).text("Browse").font(new Font("Arial", Font.BOLD, 15)).backgroundColor(Color.DARK_GRAY)
				.foregroundColor(Color.BLUE).build();
		browse.addMouseListener((BasicMouseListener)e -> show.getMediaManager().getTMP().setCurrentlyVisibleMainPanel(show.getPagePanel()));
		hoverComponents.add(browse);
		browsePanel.add(browse);
		add(browsePanel);
		currentHeight += browsePanel.getHeight();
	}
	
	@Override
	public Component add(Component component) {
		hoverComponents.add(component);
		component.addMouseListener(hoverListener);
		return super.add(component);
	}
	
}
