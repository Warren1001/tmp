package com.kabryxis.tmp.media;

import com.kabryxis.kabutils.data.Sets;
import com.kabryxis.kabutils.data.file.yaml.Config;
import com.kabryxis.tmp.DisplayOption;
import com.kabryxis.tmp.TMP;
import com.kabryxis.tmp.swing.TilePanel;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class MediaManager {
	
	private final File MEDIA_DIRECTORY = new File("C:\\MDB");
	private final Map<String, Show> shows = new HashMap<>();
	private final Map<String, Movie> movies = new HashMap<>();
	private final TMP tmp;
	private final TilePanel blockPanel, detailsPanel;
	
	private TilePanel currentTilePanel;
	private Comparator<Show> currentOrder;
	
	public MediaManager(TMP tmp) {
		this.tmp = tmp;
		MEDIA_DIRECTORY.mkdirs();
		for(File mediaFolder : Objects.requireNonNull(MEDIA_DIRECTORY.listFiles(File::isDirectory))) {
			Config mediaData = new Config(new File(mediaFolder, "info.yml"), true);
			Show show = new Show(this, mediaFolder, mediaData);
			shows.put(show.getName(), show);
			/*
			String type = mediaData.get("type", String.class);
			if(type.equalsIgnoreCase("anime") || type.equalsIgnoreCase("show")) {
			
			}
			else if(type.equalsIgnoreCase("movie") || type.equalsIgnoreCase("movie")) {
				Movie movie;
				//movies.put(movie.getName(), movie); TODO
			}
			else {
				// TODO error
			}
			*/
		}
		blockPanel = new TilePanel(this, Show::getBlockTilePanel);
		detailsPanel = new TilePanel(this, Show::getDetailsTilePanel);
	}
	
	public TMP getTMP() {
		return tmp;
	}
	
	public void initialize(DisplayOption displayOption, Comparator<Show> sortOption) {
		currentTilePanel = displayOption == DisplayOption.BLOCK ? blockPanel : detailsPanel;
		setOrder(sortOption);
		currentTilePanel.setVisible(true);
	}
	
	public void setDisplay(DisplayOption displayOption) {
		//boolean firstLoad = false;
		currentTilePanel.setVisible(false);
		//else firstLoad = true;
		currentTilePanel = displayOption == DisplayOption.BLOCK ? blockPanel : detailsPanel;
		sort();
		currentTilePanel.setVisible(true);
		//if(firstLoad) sort();
	}
	
	public void setOrder(Comparator<Show> order) {
		if(currentOrder != order) {
			currentOrder = order;
			sort();
		}
	}
	
	public Set<JPanel> getPanels() {
		return Sets.newHashSet(blockPanel, detailsPanel);
	}
	
	public TilePanel getMainPanel() {
		return currentTilePanel;
	}
	
	public void sort() {
		currentTilePanel.sort(currentOrder);
	}
	
	public Show getShow(String name) {
		return shows.get(name);
	}
	
	public Movie getMovie(String name) {
		return movies.get(name);
	}
	
	public Collection<Show> getShows() {
		return shows.values();
	}
	
}
