package com.kabryxis.tmp.media;

import com.kabryxis.kabutils.data.file.yaml.Config;
import com.kabryxis.tmp.TMP;
import com.kabryxis.tmp.user.ShowTracker;
import com.kabryxis.tmp.user.User;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MediaManager {
	
	private final File MEDIA_DIRECTORY = new File("C:\\MDB");
	private final Map<String, Show> shows = new HashMap<>();
	private final Map<String, Movie> movies = new HashMap<>();
	private final TMP tmp;
	private final JPanel mainPanel;
	
	public MediaManager(TMP tmp) {
		this.tmp = tmp;
		MEDIA_DIRECTORY.mkdirs();
		for(File mediaFolder : Objects.requireNonNull(MEDIA_DIRECTORY.listFiles(File::isDirectory))) {
			Config mediaData = new Config(new File(mediaFolder, "info.yml"), true);
			Show show = new Show(this, mediaFolder, mediaData);
			shows.put(show.getName(), show);
			/*String type = mediaData.get("type", String.class);
			if(type.equalsIgnoreCase("anime") || type.equalsIgnoreCase("show")) {
			
			}
			else if(type.equalsIgnoreCase("movie") || type.equalsIgnoreCase("movie")) {
				Movie movie;
				//movies.put(movie.getName(), movie); TODO
			}
			else {
				// TODO error
			}*/
		}
		mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		mainPanel.setBounds(0, 30, 1920, 1080 - 30);
		mainPanel.setBackground(Color.DARK_GRAY.darker());
	}
	
	public TMP getTMP() {
		return tmp;
	}
	
	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	public void loadUI() {
		Collection<Show> showsCollection = shows.values();
		showsCollection.forEach(show -> mainPanel.remove(show.getTilePanel()));
		showsCollection.stream().sorted((show1, show2) -> {
			User selectedUser = tmp.getUserManager().getSelectedUser();
			ShowTracker showTracker1 = selectedUser.getShowTracker(show1);
			ShowTracker showTracker2 = selectedUser.getShowTracker(show2);
			long lastSeen1 = showTracker1.getLastWatched();
			long lastSeen2 = showTracker2.getLastWatched();
			return lastSeen1 >= lastSeen2 ? (lastSeen1 == lastSeen2 ? String.CASE_INSENSITIVE_ORDER.compare(show1.getFriendlyName(), show2.getFriendlyName()) : -1) : 1;
		}).forEachOrdered(show -> mainPanel.add(show.getTilePanel()));
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
