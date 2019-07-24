package com.kabryxis.tmp.media;

import com.kabryxis.kabutils.Images;
import com.kabryxis.kabutils.data.file.Files;
import com.kabryxis.kabutils.data.file.yaml.Config;
import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.tmp.swing.*;
import com.kabryxis.tmp.user.ShowTracker;
import com.kabryxis.tmp.user.User;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Show implements Media {
	
	protected final MediaManager manager;
	protected final File directory;
	protected final Config data;
	protected final String name;
	protected final long added;
	protected final Season[] seasons;
	protected BlockTile blockTilePanel;
	protected DetailsTile detailsTilePanel;
	protected Image showImage;
	protected final JPanel pagePanel;
	
	private boolean hasUILoaded = false;
	
	public Show(MediaManager manager, File directory, Config data) {
		this.manager = manager;
		this.directory = directory;
		this.data = data;
		name = directory.getName().replace(File.separator, "");
		added = data.computeLongIfAbsent("added", System.currentTimeMillis());
		data.save();
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
		pagePanel = new ComponentBuilder<>(new JPanel(null)).bounds(0, 30, 1920, 1080 - 30).visible(false).build();
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
	
	public long getAdded() {
		return added;
	}
	
	public String getFriendlyName() {
		return data.get("name");
	}
	
	public String getDescription() {
		return data.get("description", "no description, nani??");
	}
	
	public List<String> getGenres() {
		return data.getList("genres", String.class, Collections.emptyList());
	}
	
	private boolean test = true;
	private double testRating = (10 + (new Random().nextInt(30) + 1)) / 4.0;
	
	public double getAverageRating() {
		if(test) return testRating;
		Collection<User> users = manager.getTMP().getUserManager().getUsers();
		double totalRating = 0.0;
		int totalRaters = 0;
		for(User user : users) {
			double rating = user.getShowTracker(this).getRating();
			if(rating == 0.0) continue;
			totalRating += rating;
			totalRaters++;
		}
		return totalRaters == 0 ? 0.0 : totalRating / totalRaters;
	}
	
	@Override
	public JPanel getBlockTilePanel() {
		if(!hasUILoaded) loadUI();
		return blockTilePanel;
	}
	
	@Override
	public JPanel getDetailsTilePanel() {
		if(!hasUILoaded) loadUI();
		return detailsTilePanel;
	}
	
	@Override
	public JPanel getPagePanel() {
		return pagePanel;
	}
	
	public Image getImage() {
		return showImage;
	}
	
	private void loadUI() {
		hasUILoaded = true;
		File file = new File(directory, data.get("image", "image.png"));
		if(file.exists()) showImage = Images.read(file);
		if(showImage == null) showImage = Images.loadFromResource(getClass().getClassLoader(), "default.png");
		blockTilePanel = new BlockTile(this, showImage);
		detailsTilePanel = new DetailsTile(this, showImage);
		JPanel leftPanel = new ComponentBuilder<>(new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15))).bounds(0, 0, 450, 1080 - 30).build();
		JPanel rightPanel = new ComponentBuilder<>(new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15))).bounds(450, 0, 1920 - 450, 1080 - 30).build();
		leftPanel.add(new JImage(Images.resize(showImage, 450, 900)));
		JPanel pageTitlePanel = new ComponentBuilder<>(new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0))).preferredSize(1920 - 450, 125).build();
		pageTitlePanel.add(new TextAreaBuilder(getFriendlyName()).font(new Font("Segoe Print", Font.BOLD, 50)).wrap(false).build());
		rightPanel.add(pageTitlePanel);
		rightPanel.add(new TextAreaBuilder(getDescription()).font(new Font("Times New Roman", Font.PLAIN, 24)).preferredSize(1920 - 450, 150).build());
		JPanel seasonsPanel = new ComponentBuilder<>(new JPanel(new FlowLayout(FlowLayout.LEADING, 15, 5))).preferredSize(1920 - 450, 600).build();
		for(Season season : seasons) {
			season.loadUI();
			seasonsPanel.add(new TextAreaBuilder(String.format("Season %s", season.getNumber())).preferredSize(100, 30).mouseListener((BasicMouseListener)e -> {
				//manager.getTMP().playEpisode(manager.getTMP().getUserManager().getSelectedUser().getShowTracker(Show.this).getSeasonTracker(season.getNumber()).getLastEpisode());
				manager.getTMP().setCurrentlyVisibleMainPanel(season.getPagePanel());
			}).build());
		}
		rightPanel.add(seasonsPanel);
		pagePanel.add(leftPanel);
		pagePanel.add(rightPanel);
		manager.getTMP().getMainPanel().add(pagePanel);
	}
	
	public ShowTracker getTracker() {
		return manager.getTMP().getUserManager().getSelectedUser().getShowTracker(this);
	}
	
}
