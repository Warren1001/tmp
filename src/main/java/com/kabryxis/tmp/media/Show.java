package com.kabryxis.tmp.media;

import com.kabryxis.kabutils.Images;
import com.kabryxis.kabutils.data.file.Files;
import com.kabryxis.kabutils.data.file.yaml.Config;
import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.tmp.swing.NewShowTile;
import com.kabryxis.tmp.user.ShowTracker;
import com.kabryxis.tmp.user.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Show implements Media {
	
	protected final MediaManager manager;
	protected final File directory;
	protected final Config data;
	protected final String name;
	protected final Season[] seasons;
	protected NewShowTile tilePanel;
	protected final JPanel pagePanel;
	
	private boolean hasUILoaded = false;
	
	public Show(MediaManager manager, File directory, Config data) {
		this.manager = manager;
		this.directory = directory;
		this.data = data;
		name = directory.getName().replace(File.separator, "");
		ConfigSection section = data.get("seasons");
		File[] seasonsFolders = Files.getDirectories(directory);
		seasons = new Season[seasonsFolders.length];
		for(int i = 0; i < seasonsFolders.length; i++) {
			File seasonFolder = seasonsFolders[i];
			int seasonNumber = i + 1;
			if(section != null) {
				ConfigSection seasonSection = section.get(String.valueOf(seasonNumber));
				if(seasonSection != null) {
					seasons[i] = new Season(this, seasonNumber, seasonSection);
					continue;
				}
			}
			seasons[i] = new Season(this, seasonNumber, seasonFolder);
		}
		pagePanel = new JPanel();
		pagePanel.setLayout(null);
		pagePanel.setBounds(0, 0, 1560 - 40, 900);
		pagePanel.setBackground(Color.DARK_GRAY);
	}
	
	public MediaManager getMediaManager() {
		return manager;
	}
	
	public Config getData() {
		return data;
	}
	
	public Season[] getSeasons() {
		return seasons;
	}
	
	public int getSeasonsAmount() {
		return seasons.length;
	}
	
	public File getDirectory() {
		return directory;
	}
	
	public Season getSeason(int season) {
		return seasons[season - 1];
	}
	
	public String getName() {
		return name;
	}
	
	public String getFriendlyName() {
		return data.get("name");
	}
	
	public String getDescription() {
		return data.get("description", "no description, nani??");
	}
	
	public List<String> getGenres() {
		return data.getList("genres", String.class);
	}
	
	public int getRating(User user) {
		return 0; // TODO
	}
	
	@Override
	public JPanel getTilePanel() {
		if(!hasUILoaded) loadUI();
		return tilePanel;
	}
	
	@Override
	public JPanel getPagePanel() {
		return pagePanel;
	}
	
	private void loadUI() {
		hasUILoaded = true;
		String imagePath = data.get("image");
		try {
			tilePanel = new NewShowTile(this, imagePath == null ? ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("default.png"))) :
					ImageIO.read(new File(directory, imagePath)));
		} catch(IOException e) {
			e.printStackTrace();
		}
		ImageIcon pageIcon;
		try {
			pageIcon = new ImageIcon(Images.reduce(imagePath == null ?
					ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("default.png"))) :
					ImageIO.read(new File(directory, imagePath)), 450, 900));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		JLabel pageImageLabel = new JLabel(pageIcon);
		pageImageLabel.setSize(pageIcon.getImage().getWidth(null), pageIcon.getImage().getHeight(null));
		JPanel pageTitlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		pageTitlePanel.setBounds(470, 0, 1560 - 470, 125);
		pageTitlePanel.setBackground(Color.DARK_GRAY);
		String titleString = getFriendlyName();
		//if(this instanceof Anime && ((Anime)this).hasEnglishName()) titleString += " - (" + ((Anime)this).getEnglishName() + ")";
		JTextArea pageTitle = new JTextArea();
		pageTitle.setText(titleString);
		pageTitle.setFont(new Font("Segoe Print", Font.BOLD, 50));
		pageTitle.setBackground(Color.DARK_GRAY);
		pageTitle.setForeground(Color.WHITE);
		pageTitle.setEditable(false);
		pageTitle.setFocusable(false);
		JTextArea pageDescription = new JTextArea();
		pageDescription.setText(getDescription());
		pageDescription.setFont(new Font("Times New Roman", Font.PLAIN, 24));
		pageDescription.setWrapStyleWord(true);
		pageDescription.setLineWrap(true);
		pageDescription.setEditable(false);
		pageDescription.setFocusable(false);
		pageDescription.setBackground(Color.DARK_GRAY);
		pageDescription.setForeground(Color.WHITE);
		pageDescription.setBounds(470, 145, 1560 - 470, 150);
		JPanel seasonsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 15, 5));
		seasonsPanel.setBackground(Color.DARK_GRAY);
		seasonsPanel.setBounds(470, 305, 1560 - 470, 900 - 305);
		for(Season season : seasons) {
			season.loadUI();
			JTextArea seasonText = new JTextArea();
			seasonText.setText("Season " + season.getNumber());
			seasonText.setEditable(false);
			seasonText.setFocusable(false);
			seasonText.setBackground(Color.DARK_GRAY);
			seasonText.setForeground(Color.WHITE);
			seasonText.setSize(100, 30);
			seasonText.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseClicked(MouseEvent event) {
					manager.getTMP().playEpisode(manager.getTMP().getUserManager().getSelectedUser().getShowTracker(Show.this).getSeasonTracker(season.getNumber()).getLastEpisode());
					//manager.getMediaPlayer().setCurrentlyVisibleMainPanel(season.getPagePanel());
				}
				
				@Override
				public void mousePressed(MouseEvent e) {}
				
				@Override
				public void mouseReleased(MouseEvent e) {}
				
				@Override
				public void mouseEntered(MouseEvent e) {}
				
				@Override
				public void mouseExited(MouseEvent e) {}
				
			});
			seasonsPanel.add(seasonText);
		}
		pagePanel.add(seasonsPanel);
		pagePanel.add(pageImageLabel);
		pageTitlePanel.add(pageTitle);
		pagePanel.add(pageTitlePanel);
		pagePanel.add(pageDescription);
		pagePanel.setVisible(false);
		manager.getTMP().getMainPanel().add(pagePanel);
	}
	
	public ShowTracker getTracker() {
		return manager.getTMP().getUserManager().getSelectedUser().getShowTracker(this);
	}
	
}
